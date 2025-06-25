export interface Pixel {
    color: string
    x: number
    y: number
    ballot: number
    proposer: number
}

export enum MessageType {
    PrepareMessage = 0, 
    PromiseMessage, 
    AcceptMessage, 
    AcceptedMessage,
    ErrorMessage
}
export interface AcceptMessage {
    type: MessageType
    roundNumber: number
    vote: Pixel
}

export interface AcceptedMessage {
    type: MessageType
    roundNumber: number
    vote: Pixel
}

export interface PromiseMessage {
    type: MessageType
    roundNumber: number
    decidedRoundNumber: number
    decidedVote: Pixel
}

export interface ErrorMessage {
    type: MessageType,
    reason: string
}

export interface PrepareMessage {
    type: MessageType
    roundNumber: number 
}

