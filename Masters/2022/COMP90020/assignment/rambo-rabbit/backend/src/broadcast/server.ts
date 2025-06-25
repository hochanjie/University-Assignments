import { Server } from "socket.io";
import { io, Socket } from "socket.io-client";
import { ServerToClientEvents, ClientToServerEvents, AcceptorToProposerEvents, ProposerToAcceptorEvents } from './basicSocketInterface';

// Current design : single front end server - application clients connect to
// Single backend server socket - other nodes connect to
// list of nodes in cluster that we maintain connections with as clients (only used for listening/responding)

console.log("Basic broadcast node initialising");
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

    nodeId: number;
    state: string[][];
    instance: number;

    // state should be maintained here or in learner module
    constructor(nodeId: number, clusterSize: number) {
        this.nodeId = nodeId;
        this.state = getDummyGrid();
        this.instance = -1;
    }

    propose(x: number, y: number, colour: string) {
        console.log("Received new delta: " + x + "," + y + " " + colour + " broadcasting to cluster");
        this.state[x][y] = colour;
        this.instance++;
        frontend.emit("emitDelta", x, y, colour);
        cluster.emit("commit", this.nodeId, x, y, colour, this.instance);
    }

    learn(leaderId: number, x: number, y: number, color: string, instance: number) {
        // Accept all messages and forward to client
        setTimeout(() => {
            if (instance >= this.instance) {
                this.state[x][y] = color;
                frontend.emit("emitDelta", x, y, color);
                this.instance = instance;
            } 
        }, 50);
    }
}


frontend.on("connection", (socket) => {

    console.log("New connection: " + socket.id);
    socket.on("getState", () => {
        socket.emit("emitState", pd.state);
    });

    // Client to server events
    socket.on("emitUpdate", (x: number, y: number, color: string) => {
        pd.propose(x, y, color)
    });
});


cluster.on("connection", (socket) => {
    // TODO: Fix recovery
    socket.emit("recoverState", pd.state);
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

            socket.on("commit", (leaderId: number, x: number, y: number, color: string, instance: number) => {
                pd.learn(leaderId, x, y, color, instance);
            });

            socket.on("recoverState", (state: string[][]) => {
                // TODO: don't start frontend port until server knows its up to date
                console.log("updating state based off other node");
                // update state and instance
                pd.state = state;
                // forward state to client
                frontend.emit("emitState", state);
            });

            sockets.push(socket);
        } else {
            frontend.listen(arr[i]-1);
            cluster.listen(arr[i]);
            console.log('cluster node listening on port ' + arr[i]);
        }
    }
});
