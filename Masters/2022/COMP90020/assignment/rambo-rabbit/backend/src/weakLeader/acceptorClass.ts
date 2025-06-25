import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import { PaxosDriver } from './paxos'
import { io, Socket } from "socket.io-client";
import { Server } from "socket.io";

export interface State {
    roundNumber: number
    decidedBallot?: number 
    decidedVote: Pixel
}

const initialState: State = { roundNumber: 0, decidedBallot: 0, decidedVote: null }

export class Acceptor {

    port: number;
    state: State;
    node: PaxosDriver;

    // paxos logic stuff
    defaultBallot: number;
    decidedVote?: Pixel;
    decidedBallot?: number;

    constructor(node: PaxosDriver, port: number) {
        
        // Initialise variables
        this.node = node;
        this.port = port;
        this.defaultBallot = 0;
        this.decidedVote = null;
        this.decidedBallot = null;
    }

    handlePrepare(instance: number, ballot: number, socket: Socket) {
        let inst: Pixel = this.node.instanceSpace[instance];
        if (inst == undefined) {
            this.node.instanceSpace[instance] = inst = {x: 0, y: 0, color: "", ballot: ballot, proposer: this.node.proposer.nodeId};
            if (ballot > this.defaultBallot) {
                console.log("Promising to instance: " + instance + " ballot: " + ballot);
                socket.emit("promise", instance, ballot);
            } else {
                // nack
                console.log("Responding with nack");
                socket.emit("promise", instance, -1);
            }
        } else {
            if (ballot > inst.ballot) {
                console.log("Promising to instance: " + instance + " ballot: " + ballot);
                socket.emit("promise", instance, ballot, inst.ballot, inst);
            } else {
                // nack
                console.log("Responding with nack");
                socket.emit("promise", instance, -1);
            }
        }
    }

    // handles the case where a node's proposer sends to its own acceptor internally
    handleOwnPrepare(instance: number, ballot: number) {
        let inst: Pixel = this.node.instanceSpace[instance];
        if (inst == undefined) {
            this.node.instanceSpace[instance] = inst = {x: 0, y: 0, color: "", ballot: ballot, proposer: this.node.proposer.nodeId};
            if (ballot > this.defaultBallot) {
                this.node.handlePromise(instance, ballot);
            } else {
                // nack
                this.node.handlePromise(instance, -1);
            }
        } else {
            console.log("preparing ballot: " + ballot + " current promise is " + inst.ballot);
            if (ballot > inst.ballot) {
                this.node.handlePromise(instance, ballot, inst.ballot, inst);
            } else {
                this.node.handlePromise(instance, -1);
            }
        }
    }

    handleAccept(instance: number, ballot: number, vote: Pixel, cluster: Server) {
        let inst: Pixel = this.node.instanceSpace[instance];
        // if round >= currRound && promisedRound != round
        // set promisedRound and currRound to round and currVal to vote

        if (inst == undefined) {
            if (ballot >= this.defaultBallot) {
                this.node.learn(instance, ballot, vote);
                cluster.emit("accepted", instance, ballot, vote);
            }
        } else if (ballot >= inst.ballot) {
            this.node.learn(instance, ballot, vote);
            cluster.emit("accepted", instance, ballot, vote);
        }
        // else ignore request 
    }
}
