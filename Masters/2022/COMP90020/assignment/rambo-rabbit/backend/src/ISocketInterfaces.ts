// The common interfaces between the client and server are defined here
export interface ServerToClientEvents {
    emitDelta: (x: number, y: number, color: string) => void;
    emitState: (state: string[][]) => void;
}

export interface ClientToServerEvents {
    emitUpdate: (x: number, y: number, color: string) => void;
    getState: () => void;
}
