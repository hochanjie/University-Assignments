import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import { PaxosDriver } from './paxos'
import { io, Socket } from "socket.io-client";

export class Acceptor {
    port: number;
    node: PaxosDriver;

    // paxos logic stuff
    defaultBallot: number;
    decidedVote?: Pixel;
    decidedBallot?: number;
    preCommittedState: string[][]

    constructor(node: PaxosDriver, initialState: string[][]) {
        
        // Initialise variables
        this.node = node;
        this.defaultBallot = 0;
        this.decidedVote = null;
        this.decidedBallot = null;
        this.preCommittedState = JSON.parse(JSON.stringify(initialState));
    }

    handlePreAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string, socket: Socket) {
        let inst: Pixel = this.node.instanceSpace[leaderId][instance];
        let req: string = this.preCommittedState[vote.x][vote.y];

        if (inst != undefined && ballot < inst.ballot) {
            // this is a stale preaccept, should ignore
            console.log("Received stale preAccept, ignoring");
            return;
        } else {
            // check dependency and fill in instanceSpace
            this.node.instanceSpace[leaderId][instance] = vote;
            if (req == prevColor) {
                // dependency is correct, should store this until commit is received
                console.log("Responding ok to preAccept from node " + leaderId + " with instance: " + instance);
                socket.emit("preAcceptOk", instance, vote, prevColor);
                this.preCommittedState[vote.x][vote.y] = vote.color;
            } else {
                // dependency is wrong
                console.log("Dependency incorrect for preAccept from node " + leaderId + " with instance: " + instance);
                socket.emit("preAcceptReply", instance, ballot, vote, req);
            }
        }
    }

    handleOwnPreAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) {
        let inst: Pixel = this.node.instanceSpace[leaderId][instance];
        let req: string = this.preCommittedState[vote.x][vote.y];
        let stateColor: string = this.node.state[vote.x][vote.y];

        // check dependency and fill in instanceSpace
        if (inst != undefined && ballot < inst.ballot) {
            // this is a stale preaccept, should ignore
            console.log("Received stale preAccept, ignoring");
            return;
        } else {
            this.node.instanceSpace[leaderId][instance] = vote;
            if (req == prevColor || stateColor == prevColor) {
                // dependency is correct, should store this until commit is received
                this.node.handlePreAcceptOk(instance, vote, prevColor);
                this.preCommittedState[vote.x][vote.y] = vote.color;
            } else {
                // dependency is wrong
                this.node.handlePreAcceptReply(instance, ballot, vote, req);
            }
        }
    }

    handleAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string, socket: Socket) {
        let req: string = this.node.state[vote.x][vote.y];

        if (req == prevColor) {
            // store this new color for concurrent requests until commit is made
            console.log("Acknowledging accept message");
            socket.emit("acceptOk", instance, vote, prevColor);
            this.preCommittedState[vote.x][vote.y] = vote.color;
        } 
    }

    handleOwnAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) {
        // should this be state or precommit state
        let req: string = this.node.state[vote.x][vote.y];

        if (req == prevColor) {
            this.node.handleAcceptOk(instance, vote, prevColor);
            this.preCommittedState[vote.x][vote.y] = vote.color;
        } 
    }
}
