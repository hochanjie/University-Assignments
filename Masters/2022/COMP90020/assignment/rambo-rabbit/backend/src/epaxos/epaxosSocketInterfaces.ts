// The common interfaces between the client and server are defined here
import { Pixel } from './index'

export interface ServerToClientEvents {
    emitDelta: (x: number, y: number, color: string) => void;
    emitState: (state: string[][]) => void;
}

export interface ClientToServerEvents {
    emitUpdate: (x: number, y: number, color: string) => void;
    getState: () => void;
}

export interface ProposerToAcceptorEvents {
    prepare: (instance: number, ballot: number) => void;
    accept: (instance: number, ballot: number, vote: Pixel) => void;
    // this is a acceptor to learner event but technically server->client
    accepted: (instance: number, ballot: number, vote: Pixel) => void;
    recoverState: (instances: Array<number>, state: string[][]) => void;
    preAccept: (leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) => void;
    // TODO: add count to accept request?
    eAccept: (leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) => void;
    commit: (leaderId: number, instance: number, vote: Pixel, prevColor: string) => void;
}

export interface AcceptorToProposerEvents {
    promise: (instance: number, ballot: number, decidedBallot?: number, decidedVote?: Pixel) => void;
    // could include a nack?
    preAcceptReply: (instance: number, ballot: number, vote: Pixel, prevState: string) => void;
    preAcceptOk: (instance: number, vote: Pixel, prevState: string) => void;
    acceptOk: (instance: number, vote: Pixel, prevState: string) => void;
}
