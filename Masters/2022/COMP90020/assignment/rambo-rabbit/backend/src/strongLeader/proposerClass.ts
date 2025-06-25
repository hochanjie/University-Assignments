import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import readline from 'readline';
import { AcceptorToProposerEvents, ProposerToAcceptorEvents } from './strongLeaderSocketInterface';
import { PaxosDriver } from './paxos'
import { Server } from "socket.io";

export class Proposer {
    
    nodeId: number;
    currBallot: number;
    node: PaxosDriver;
    numAccepted: Array<number>;
    majorityNodes: number;

    highestBallotCommittedTo: number;
    highestVoteCommittedTo?: Pixel;
    currentVote?: Pixel;
    nextProposeInstance: number;


    constructor(node: PaxosDriver, id: number, numberOfNodes: number) {
        this.node = node;
        this.nodeId = id;
        this.numAccepted = new Array<number>(128*1024).fill(0);
        // determine based on cluster size
        this.majorityNodes = Math.floor(numberOfNodes/2)+1;

        this.highestBallotCommittedTo = 0;
        this.highestVoteCommittedTo = null;
        this.currentVote = null;
        this.nextProposeInstance = 0;
    }

    clearPaxosVariables() {
        this.highestBallotCommittedTo = 0;
        this.highestVoteCommittedTo = null;
    }

    clearVote(newInstance: number) {
        if (this.nextProposeInstance < newInstance) {
            this.currentVote = null;
        }
    }

    propose(x: number, y: number, colour: string, cluster: Server) {
        // Round number should start at base + id
        this.currBallot = parseFloat("1." + this.nodeId);
        this.currentVote = {x: x, y: y, color: colour, ballot: this.currBallot, proposer: this.nodeId};
        let instance = Math.max(this.node.currInstance, this.nextProposeInstance); 
        this.nextProposeInstance = instance + 1;
        this.numAccepted[instance] = 0;
        if (this.node.isLeader()) {
            // do we just skip straight to the accept phase here?
            console.log("Broadcasting accept: " + x + " " + y + " " + colour);
            this.node.acceptor.handleOwnAccept(this.node.currInstance, this.currBallot, this.currentVote);
            cluster.emit("accept", instance, this.currBallot, this.currentVote);
        } else {
            // TODO: Forward to leader
            console.log("Not the leader, forwarding update to leader... ");
            this.node.forwardToLeader(this.currentVote);
        }
    }

    handleAccepted(instance: number, ballot: number, vote: Pixel, cluster: Server) {
        this.numAccepted[instance]++;
        console.log("Accepted message received for instance: " + instance + " " + this.numAccepted[instance] + "/" + this.majorityNodes);
        // include some checks on inputs?

        if (this.numAccepted[instance] == this.majorityNodes) {
            console.log("Achieved majority accepted, broadcasting commit");

            this.node.learn(instance, ballot, vote);
            cluster.emit("commit", instance, ballot, vote);
        }
    }
}
