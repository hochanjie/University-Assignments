import { Pixel, MessageType, AcceptMessage, AcceptedMessage, PromiseMessage, PrepareMessage, ErrorMessage } from './index'
import { PaxosDriver } from './paxos'
import { io, Socket } from "socket.io-client";

export interface State {
    roundNumber: number
    decidedBallot?: number 
    decidedVote: Pixel
}

const initialState: State = { roundNumber: 0, decidedBallot: 0, decidedVote: null }

export class Acceptor {

    state: State;
    node: PaxosDriver;

    // paxos logic stuff
    defaultBallot: number;
    decidedVote?: Pixel;
    decidedBallot?: number;

    constructor(node: PaxosDriver) {
        
        // Initialise variables
        this.node = node;
        this.defaultBallot = 0;
        this.decidedVote = null;
        this.decidedBallot = null;
    }

    handleAccept(instance: number, ballot: number, vote: Pixel, socket: Socket) {
        console.log("Accept message received, instance: " + instance + " ballot: " + ballot);
        let inst: Pixel = this.node.instanceSpace[instance];
        // if round >= currRound && promisedRound != round
        // set promisedRound and currRound to round and currVal to vote

        if (inst == undefined) {
            if (ballot >= this.defaultBallot) {
                //this.node.learn(instance, ballot, vote);
                socket.emit("accepted", instance, ballot, vote);
            }
        } else if (ballot >= inst.ballot) {
            //this.node.learn(instance, ballot, vote);
            socket.emit("accepted", instance, ballot, vote);
        }
        // else ignore request
    }

    handleOwnAccept(instance: number, ballot: number, vote: Pixel) {
        let inst: Pixel = this.node.instanceSpace[instance];

        if (inst == undefined) {
            if (ballot >= this.defaultBallot) {
                this.node.handleAccepted(instance, ballot, vote);
            }
        } else if (ballot >= inst.ballot) {
            this.node.handleAccepted(instance, ballot, vote);
        }
        // else ignore request
    }
}
