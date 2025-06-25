import { Acceptor } from './acceptorClass';
import { Proposer } from './proposerClass';
import { Server } from "socket.io";
import { io, Socket } from "socket.io-client";
import { Pixel } from './index'
import { ServerToClientEvents, ClientToServerEvents, AcceptorToProposerEvents, ProposerToAcceptorEvents } from './weakLeaderSocketInterfaces';

// Current design : single front end server - application clients connect to
// Single backend server socket - other nodes connect to
// list of nodes in cluster that we maintain connections with as clients (only used for listening/responding)

console.log("Weak leader multi-paxos node initialising");
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
    paxosPort: number;
    state: string[][];
    instanceSpace: Array<Pixel>;
    currInstance: number;

    // state should be maintained here or in learner module
    constructor(paxosPort: number, numberOfNodes: number) {
        this.paxosPort = paxosPort;
        this.acceptor = new Acceptor(this, paxosPort);
        this.proposer = new Proposer(this, paxosPort, numberOfNodes);
        this.state = getDummyGrid();
        this.instanceSpace = new Array<Pixel>(1024*128);
        this.currInstance = 0;
    }

    propose(x: number, y: number, colour: string) {
        this.proposer.propose(x, y, colour, cluster);
    }

    handleAccept(instance: number, ballot: number, vote: Pixel) {
        console.log("Accept message received with instance: " + instance + " ballot " + ballot);
        this.acceptor.handleAccept(instance, ballot, vote, cluster);
        // this broadcast doesn't work if it started second (means that first server is not connecting properly?)
    }

    handlePromise(instance: number, ballot: number, decidedRound?: number, decidedVote?: Pixel) {
        this.proposer.handlePromise(instance, ballot, cluster, decidedRound, decidedVote);
    }

    // Pixel is nullable here
    handlePrepare(instance: number, ballot: number, socket: Socket) {
        console.log("Prepare message received with instance: " + instance + " ballot: " + ballot);
        this.acceptor.handlePrepare(instance, ballot, socket);
    }

    learn(instance: number, ballot: number, vote: Pixel) {
        // update our own state or add to replicated log
        if (instance == this.currInstance) {
            console.log("accepted " + JSON.stringify(vote) + " with instance: " + instance + " and ballot: " + ballot);
            this.instanceSpace[this.currInstance] = vote;
            this.currInstance++;
            this.state[vote.x][vote.y] = vote.color;
            frontend.emit("emitDelta", vote.x, vote.y, vote.color);
            // tell proposer to stop retrying
            this.proposer.clearVote(this.currInstance);
        } else if (instance > this.currInstance) {
            // TODO: Rerequest current state from nodes if we missed some things
            console.log("Receiving value at instance higher than expected, need to query missing instances");
            this.state[vote.x][vote.y] = vote.color;
            frontend.emit("emitDelta", vote.x, vote.y, vote.color);
        } else {
            // receiving broadcast on a instance previously committed, check if ballot is higher and if so emit to clients
            if (this.instanceSpace[instance] && ballot > this.instanceSpace[instance].ballot) {
                console.log("Receiving value at lower instance than expected, potential missed value");
                this.instanceSpace[instance] = vote;
                this.state[vote.x][vote.y] = vote.color;
                frontend.emit("emitDelta", vote.x, vote.y, vote.color);
            }
        }
        
        // multicast delta update to clients
    }
}


frontend.on("connection", (socket) => {

    console.log("New connection: " + socket.id);
    socket.on("getState", () => {
        socket.emit("emitState", pd.state);
    })
    // Client to server events
    socket.on("emitUpdate", (x: number, y: number, color: string) => {
        console.log(x, y, color);
        // TODO: Handle emitted update from client i.e. start round of Paxos
        pd.propose(x, y, color)
    });
});


cluster.on("connection", (socket) => {
    socket.emit("recoverState", pd.currInstance, pd.state);
    socket.on("promise", (instance: number, ballot: number, decidedRound?: number, decidedVote?: Pixel) => {
        // update log here?
        pd.handlePromise(instance, ballot, decidedRound, decidedVote);
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
            socket.on("prepare", (instance: number, ballot: number) => {
                // when receive a prepare, call acceptor.handlePrepare, return a promise or a nack
                pd.handlePrepare(instance, ballot, socket);
            });
            socket.on("accept", (instance: number, ballot: number, vote: Pixel) => {
                pd.handleAccept(instance, ballot, vote);
                // if accept works, broadcast on cluster.emit
            });
            socket.on("accepted", (instance: number, ballot: number, vote: Pixel) => {
                pd.learn(instance, ballot, vote);
            });
            socket.on("recoverState", (instance: number, state: string[][]) => {
                // TODO: don't start client until server knows its up to date
                if (instance > pd.currInstance) {
                    console.log("updating state based off other nodes: instance " + instance);
                    // update state and instance
                    pd.currInstance = instance;
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

