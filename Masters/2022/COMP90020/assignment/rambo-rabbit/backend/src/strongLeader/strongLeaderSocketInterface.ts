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
    leaderAlive: (instance: number, id: number) => void;
    recoverState: (instance: number, state: string[][]) => void;
    commit: (instance: number, ballot: number, vote: Pixel) => void;
}

export interface AcceptorToProposerEvents {
    promise: (instance: number, ballot: number, decidedBallot?: number, decidedVote?: Pixel) => void;
    // could include a nack?
    accepted: (instance: number, ballot: number, vote: Pixel) => void;
    forward: (vote: Pixel) => void;
}
