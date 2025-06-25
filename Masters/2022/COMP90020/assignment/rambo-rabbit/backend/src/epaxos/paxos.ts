import { Acceptor } from './acceptorClass';
import { Proposer } from './proposerClass';
import { Server } from "socket.io";
import { io, Socket } from "socket.io-client";
import { Pixel } from './index'
import { ServerToClientEvents, ClientToServerEvents, AcceptorToProposerEvents, ProposerToAcceptorEvents } from './epaxosSocketInterfaces';

// Current design : single front end server - application clients connect to
// Single backend server socket - other nodes connect to
// list of nodes in cluster that we maintain connections with as clients (only used for listening/responding)

console.log("Egalitarian paxos node initialising");
const gridSize = 16

function getDummyGrid(): string[][] {
    const row = []
    for (let i=0; i<gridSize; i++) {
        const col = []
        for (let j=0; j<gridSize; j++) {
            col.push("#FFFFFF");
        }
        row.push(col);
    }
    return row;
}
const frontend = new Server<ClientToServerEvents, ServerToClientEvents>();
const cluster = new Server<AcceptorToProposerEvents, ProposerToAcceptorEvents>();

// Paxos Driver
export class PaxosDriver {

    acceptor: Acceptor;
    proposer: Proposer;
    nodeId: number;
    state: string[][];
    instanceSpace: Array<Array<Pixel>>;
    currInstance: Array<number>;

    // state should be maintained here or in learner module
    constructor(nodeId: number, clusterSize: number) {
        this.nodeId = nodeId;
        this.state = getDummyGrid();
        this.acceptor = new Acceptor(this, this.state);
        this.proposer = new Proposer(this, nodeId, clusterSize);
        this.instanceSpace = new Array<Array<Pixel>>(clusterSize);
        for (let i = 0; i < clusterSize; i++) {
            this.instanceSpace[i] = new Array<Pixel>(1024*128);
        }
        this.currInstance = new Array<number>(clusterSize).fill(0);
    }

    propose(x: number, y: number, colour: string) {
        this.proposer.propose(x, y, colour, cluster);
    }

    handlePreAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string, socket: Socket) {
        console.log("Received preAccept from node " + leaderId + " with instance: " + instance + " val: " + vote.color + " prev: " + prevColor);
        this.acceptor.handlePreAccept(leaderId, instance, ballot, vote, prevColor, socket);
    }

    handlePreAcceptReply(instance: number, ballot: number, vote: Pixel, prevColor: string) {
        setTimeout(() => {
            this.proposer.handlePreAcceptReply(instance, ballot, vote, prevColor, cluster);
        }, 10);
    }

    handlePreAcceptOk(instance: number, vote: Pixel, prevColor: string) {
        this.proposer.handlePreAcceptOk(instance, vote, prevColor, cluster);
    }

    handleEAccept(leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string, socket: Socket) {
        console.log("Received accept from node " + leaderId + " with instance: " + instance + " val: " + vote.color + " prev: " + prevColor);
        this.acceptor.handleAccept(leaderId, instance, ballot, vote, prevColor, socket);
    }

    handleAcceptOk(instance: number, vote: Pixel, prevColor: string) {
        console.log("Received AcceptOk with instance: " + instance);
        this.proposer.handleAcceptOk(instance, vote, prevColor, cluster);
    }

    learn(leaderId: number, instance: number, vote: Pixel, prevColor: string) {
        // update our own state or add to replicated log
        let inst = this.instanceSpace[leaderId][instance];
        if (instance >= this.currInstance[leaderId]) {
            console.log("committing " + JSON.stringify(vote) + " with instance: " + instance);
            this.instanceSpace[leaderId][instance] = vote;
            this.currInstance[leaderId] = instance + 1;
            this.state[vote.x][vote.y] = vote.color;
            this.acceptor.preCommittedState[vote.x][vote.y] = vote.color;
            frontend.emit("emitDelta", vote.x, vote.y, vote.color);
        } else if (this.instanceSpace[leaderId][instance] && vote.ballot > this.instanceSpace[leaderId][instance].ballot) {
            console.log("Receiving value at lower instance than expected, potential missed value");
            this.instanceSpace[leaderId][instance] = vote;
            this.state[vote.x][vote.y] = vote.color;
            this.acceptor.preCommittedState[vote.x][vote.y] = vote.color;
            frontend.emit("emitDelta", vote.x, vote.y, vote.color);
        }
    }

    updateState(state: string[][]) {
        this.state = state;
        this.acceptor.preCommittedState = state;
    }
}


frontend.on("connection", (socket) => {

    console.log("New connection: " + socket.id);
    socket.on("getState", () => {
        console.log("Sending current state to " + socket.id);
        socket.emit("emitState", pd.state);
    });

    // Client to server events
    socket.on("emitUpdate", (x: number, y: number, color: string) => {
        console.log(x, y, color);
        // TODO: Handle emitted update from client i.e. start round of Paxos
        pd.propose(x, y, color)
    });
});


cluster.on("connection", (socket) => {
    // TODO: Fix recovery
    socket.emit("recoverState", pd.currInstance, pd.state);
    socket.on("preAcceptReply", (instance: number, ballot: number, vote: Pixel, prevState: string) => {
        // update log here?
        pd.handlePreAcceptReply(instance, ballot, vote, prevState);
    });

    socket.on("preAcceptOk", (instance: number, vote: Pixel, prevState: string) => {
        // update log here?
        pd.handlePreAcceptOk(instance, vote, prevState);
    });

    socket.on("acceptOk", (instance: number, vote: Pixel, prevState: string) => {
        // update log here?
        pd.handleAcceptOk(instance, vote, prevState);
    });
});

cluster.on("disconnect", (socket) => {
    console.log("Socket " + socket.id + " has disconnected");
});


// only allow clusters of size 3
const config: string = 'cluster3.config';
const args = process.argv.slice(2);
const nodeId: number = parseInt(args[0]);
const clusterSize: number = 3;
const pd = new PaxosDriver(nodeId, clusterSize);

const sockets: Array<Socket> = new Array<Socket>(clusterSize);

const fs = require('fs');
fs.readFile(config, function(err: Error, data: any) {
    if (err) throw err;
    const arr = data.toString().replace(/\r\n/g,'\n').split('\n');
    for (let i = 0; i < clusterSize; i++) {
        if (i != nodeId) {
            const socket: Socket<ProposerToAcceptorEvents, AcceptorToProposerEvents> = io("http://localhost:" + arr[i]);

            socket.on("preAccept", (leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) => {
                pd.handlePreAccept(leaderId, instance, ballot, vote, prevColor, socket);
            });

            socket.on("eAccept", (leaderId: number, instance: number, ballot: number, vote: Pixel, prevColor: string) => {
                pd.handleEAccept(leaderId, instance, ballot, vote, prevColor, socket);
            });

            socket.on("commit", (leaderId: number, instance: number, vote: Pixel, prevColor: string) => {
                pd.learn(leaderId, instance, vote, prevColor);
            });

            socket.on("recoverState", (instances: Array<number>, state: string[][]) => {
                // TODO: don't start frontend port until server knows its up to date
                let sum: number = 0;
                for (let i = 0; i < instances.length; i++) {
                    sum = sum + (pd.currInstance[i] - instances[i]);
                }
                if (sum < 0) {
                    console.log("updating state based off other node");
                    // update state and instance
                    pd.currInstance = instances;
                    pd.state = state;
                    // forward state to client
                    frontend.emit("emitState", state);
                }
            });

            sockets.push(socket);
        } else {
            frontend.listen(arr[i]-1);
            cluster.listen(arr[i]);
            console.log('cluster node listening on port ' + arr[i]);
        }
    }
});
