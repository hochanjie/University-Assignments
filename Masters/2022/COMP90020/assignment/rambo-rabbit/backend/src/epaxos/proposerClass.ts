import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import readline from 'readline';
import { AcceptorToProposerEvents, ProposerToAcceptorEvents } from "./epaxosSocketInterfaces";
import { PaxosDriver } from './paxos'
import { Server } from "socket.io";

export class Proposer {
    
    nodeId: number;
    currBallot: number;
    node: PaxosDriver;
    nextProposeInstance: number;
    majorityNodes: number;

    currentVote?: Pixel;
    proposedInstance: number;
    okCounts: Array<number>;
    acceptCounts: Array<number>;

    constructor(node: PaxosDriver, id: number, numberOfNodes: number) {
        this.node = node;
        this.nodeId = id;
        this.acceptCounts = new Array<number>(1024*128);
        // determine based on cluster size
        this.majorityNodes = Math.floor(numberOfNodes/2)+1;

        this.currentVote = null;
        this.nextProposeInstance = 0;
        this.okCounts = new Array<number>(1024*128);
    }


    clearVote(newInstance: number) {
        if (this.nextProposeInstance < newInstance) {
            this.currentVote = null;
        }
    }

    propose(x: number, y: number, colour: string, cluster: Server) {
        // Round number should start at base + id
        // TODO: multi paxos
        let instance = Math.max(this.node.currInstance[this.nodeId], this.nextProposeInstance);

        this.nextProposeInstance = instance + 1;
        this.currBallot = parseFloat("1." + this.nodeId);
        this.currentVote = {x: x, y: y, color: colour, ballot: this.currBallot, proposer: this.nodeId};
        let prevColor = this.node.state[x][y]
        
        this.okCounts[instance] = 0;
        // broadcast prepare
        console.log("Broadcasting preAccept " + JSON.stringify(this.currentVote) + " prev: " + prevColor + " with instance: " + instance);
        cluster.emit("preAccept", this.nodeId, instance, this.currBallot, this.currentVote, prevColor);
        // TODO: check we still send to ourselves
        this.node.acceptor.handleOwnPreAccept(this.nodeId, instance, this.currBallot, this.currentVote, prevColor);
    }

    /*
    retryProposal(x: number, y: number, colour: string, cluster: Server, delay: number) {
        // check if our previous proposal has not been accepted
        if (this.currentVote) {
            this.currBallot++;
            this.okCounts[this.proposedInstance];

            console.log("Retrying preAccept with ballot " + this.currBallot);
            cluster.emit("preAccept", this.nodeId, instance, this.currBallot, this.currentVote, prevColor);
            this.node.acceptor.handleOwnPrepare(this.nodeId, instance, this.currBallot, this.currentVote, prevColor);

            // increase delay exponentially
            delay = delay * delay
            setTimeout(this.retryProposal.bind(this), delay, x, y, colour, cluster, delay);
        }
    }
    */

    handlePreAcceptReply(instance: number, ballot: number, vote: Pixel, prevColor: string, cluster: Server) {
        // upon receiving this, should move to phase 2 (slow path)
        console.log("Received preAcceptReply with instance: " + instance + " val: " + vote.color + " prev: " + prevColor);
        if (this.okCounts[instance] < this.majorityNodes) {
            this.okCounts[instance] += 1 + this.majorityNodes;
            console.log("Phase 1 declined for instance: " + instance + ", broadcasting accept for instance: " + instance + " val: " + vote.color + " prev: " + prevColor);
            this.acceptCounts[instance] = 0;
            this.node.acceptor.handleOwnAccept(this.nodeId, instance, ballot, vote, prevColor);
            cluster.emit("eAccept", this.nodeId, instance, ballot, vote, prevColor);
        }
    }

    handlePreAcceptOk(instance: number, vote: Pixel, prevColor: string, cluster: Server) {
        this.okCounts[instance]++;
        if (this.okCounts[instance] <= this.majorityNodes) {
            console.log("Received preAcceptOk for instance " + instance + ": " + this.okCounts[instance] + " / " + this.majorityNodes)
        }

        if (this.okCounts[instance] == this.majorityNodes) {
            console.log("Achieved majority preAcceptOk, moving to commit phase");
            // commit to self then broadcast commit
            this.node.learn(this.nodeId, instance, vote, prevColor);
            cluster.emit("commit", this.nodeId, instance, vote, prevColor);
        }
    }

    handleAcceptOk(instance: number, vote: Pixel, prevColor: string, cluster: Server) {
        this.acceptCounts[instance]++;
        if (this.acceptCounts[instance] == this.majorityNodes) {
            // if achieve majority acks, can commit this new value
            console.log("Received majority acks, moving to commit phase");
            this.node.learn(this.nodeId, instance, vote, prevColor);
            cluster.emit("commit", this.nodeId, instance, vote, prevColor);
        }
    }
}
