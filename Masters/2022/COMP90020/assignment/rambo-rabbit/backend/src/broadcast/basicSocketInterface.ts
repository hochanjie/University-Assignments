// The common interfaces between the client and server are defined here

export interface ServerToClientEvents {
    emitDelta: (x: number, y: number, color: string) => void;
    emitState: (state: string[][]) => void;
}

export interface ClientToServerEvents {
    emitUpdate: (x: number, y: number, color: string) => void;
    getState: () => void;
}

export interface ProposerToAcceptorEvents {
    commit: (nodeId: number, x: number, y: number, color: string, instance: number) => void;
    recoverState: (state: string[][]) => void;
}

export interface AcceptorToProposerEvents {
}
