import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import { AcceptorToProposerEvents, ProposerToAcceptorEvents } from './weakLeaderSocketInterfaces';
import { PaxosDriver } from './paxos'
import { Server } from "socket.io";

export class Proposer {
    
    nodeId: number;
    currBallot: number;
    node: PaxosDriver;
    numPromised: number;
    majorityNodes: number;

    highestBallotCommittedTo: number;
    highestVoteCommittedTo?: Pixel;
    currentVote?: Pixel;
    proposedInstance: number;


    constructor(node: PaxosDriver, id: number, numberOfNodes: number) {
        this.node = node;
        this.nodeId = id;
        this.numPromised = 0;
        // determine based on cluster size
        this.majorityNodes = Math.floor(numberOfNodes/2)+1;

        this.highestBallotCommittedTo = 0;
        this.highestVoteCommittedTo = null;
        this.currentVote = null;
        this.proposedInstance = -1;
    }

    clearPaxosVariables() {
        this.highestBallotCommittedTo = 0;
        this.highestVoteCommittedTo = null;
    }

    clearVote(newInstance: number) {
        if (this.proposedInstance < newInstance) {
            this.currentVote = null;
        }
    }

    propose(x: number, y: number, colour: string, cluster: Server) {
        // Round number should start at base + id
        // TODO: multi paxos
        let instance = this.node.currInstance;
        if (this.proposedInstance < instance) {
            this.proposedInstance = instance;
            this.currBallot = parseFloat("1." + this.nodeId);
            this.currentVote = {x: x, y: y, color: colour, ballot: this.currBallot, proposer: this.nodeId};
            
            this.numPromised = 0;

            // TODO: delete the first if statement branch if you do not want to run a weak leader
            // If the proposer of the last instance is the same as the proposer of the current
            // instance, then skip the prepare phase.
            const lastInstance = this.node.instanceSpace[this.node.currInstance-1]; 
            if (lastInstance && lastInstance.proposer == this.nodeId) {

                console.log(`Proposing: current instance ${this.node.currInstance} has the same leader as last instance`);
                this.handleMajorityNodesPromised(this.node.currInstance, this.currBallot, cluster);

            } else {
                // broadcast prepare
                console.log("Broadcasting prepare with instance: " + this.node.currInstance + " ballot: " + this.currBallot);
                cluster.emit("prepare", this.node.currInstance, this.currBallot);
                // send to our own acceptor as well
                this.node.acceptor.handleOwnPrepare(this.node.currInstance, this.currBallot);

                let delay = 100;
                setTimeout(this.retryProposal.bind(this), delay, x, y, colour, cluster, delay);
            }
        }
    }

    retryProposal(x: number, y: number, colour: string, cluster: Server, delay: number) {
        // check if our previous proposal has not been accepted
        if (this.currentVote) {
            this.currBallot++;
            // check we're not accepting promises from previous proposals with lower ballots
            this.numPromised = 0;

            console.log("Retrying prepare with ballot " + this.currBallot);
            cluster.emit("prepare", this.proposedInstance, this.currBallot);
            // send to our own acceptor as well
            this.node.acceptor.handleOwnPrepare(this.proposedInstance, this.currBallot);

            // make exponential delay
            delay = delay * delay
            setTimeout(this.retryProposal.bind(this), delay, x, y, colour, cluster, delay);
        }
    }

    handlePromise(instance: number, ballot: number, cluster: Server, decidedBallot?: number, decidedVote?: Pixel) {
        if (ballot == this.currBallot && instance == this.node.currInstance) {

            // logic to check if there is already consensus
            this.numPromised++;
            console.log("Promise message received: instance: " + instance + " ballot " + ballot + " " + this.numPromised + "/" + this.majorityNodes);

            if (decidedBallot && decidedBallot > this.highestBallotCommittedTo) {
                this.highestBallotCommittedTo = decidedBallot;
                this.highestVoteCommittedTo = decidedVote;
            }
    
            if (this.numPromised == this.majorityNodes) {
                // does accept only go to those that promised?
                // node.cluster.emit("accept") or node.emitAccept()
                console.log("Achieved majority promises, moving to propose phase");
                
                this.handleMajorityNodesPromised(instance, ballot, cluster);
            }
        }
    }

    handleMajorityNodesPromised(instance: number, ballot: number, cluster: Server) {
        if (this.highestVoteCommittedTo) {
            this.currentVote = this.highestVoteCommittedTo;
        }

        cluster.emit("accept", instance, ballot, this.currentVote);
        // send to our own acceptor as well
        this.node.handleAccept(instance, ballot, this.currentVote);
        this.clearPaxosVariables();
    }
}

