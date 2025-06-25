import { Acceptor } from './acceptorClass';
import { Proposer } from './proposerClass';
import { Server } from "socket.io";
import { io, Socket } from "socket.io-client";
import { Pixel, HeartBeat } from './index'
import { ServerToClientEvents, ClientToServerEvents, AcceptorToProposerEvents, ProposerToAcceptorEvents } from './strongLeaderSocketInterface';

// Current design : single front end server - application clients connect to
// Single backend server socket - other nodes connect to
// list of nodes in cluster that we maintain connections with as clients (only used for listening/responding)

console.log("Strong leader multi-paxos node initialising");
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
    
    // leader variables
    lastLeader: number;
    lastLeaderMsg: HeartBeat;

    // state should be maintained here or in learner module
    constructor(paxosPort: number, numberOfNodes: number) {
        this.paxosPort = paxosPort;
        this.acceptor = new Acceptor(this);
        this.proposer = new Proposer(this, paxosPort, numberOfNodes);
        this.state = getDummyGrid();
        this.instanceSpace = new Array<Pixel>(1024*128);
        this.currInstance = 0;
        // default leader?
        this.lastLeader = 9990;
        setInterval(this.sendLeaderAlive.bind(this), 1000);
        setInterval(this.checkLeaderAlive.bind(this), 3000);
    }

    sendLeaderAlive() {
        if (this.isLeader()) {
            console.log("sending heartbeat");
            cluster.emit("leaderAlive", this.currInstance, this.paxosPort);
        }
    }

    checkLeaderAlive() {
        // if leader and not instance_received? -> get highest instance of everyone else
        let leader: boolean = this.isLeader();
        if (this.lastLeaderMsg == undefined) {
            if (!leader) {
                console.log("No leader, electing self; last: " + this.lastLeader + " port: " + this.paxosPort);
                this.lastLeader = this.paxosPort;
                // request highest instance of everyone else
                cluster.emit("leaderAlive", this.currInstance, this.paxosPort);
            }
        } else if (Date.now() - +this.lastLeaderMsg.time > 3000) {
            if (!leader) {
                console.log("Leader timeout, electing self");
                this.lastLeader = this.paxosPort;
                // request highest instance of everyone else
                cluster.emit("leaderAlive", this.currInstance, this.paxosPort);
            }
        } else {
            if (this.paxosPort > this.lastLeaderMsg.id) {
                if (!leader) {
                    console.log(this.paxosPort, this.lastLeader);
                    console.log("Higher id than leader, electing self");
                    this.lastLeader = this.paxosPort;
                    // request highest instance of everyone else
                    cluster.emit("leaderAlive", this.currInstance, this.paxosPort);
                }
            } else if (this.lastLeaderMsg) {
                this.lastLeader = this.lastLeaderMsg.id;
            }
        }
    }

    forwardToLeader(vote: Pixel) {
        // turn Sockets into a map or something
        sockets[this.lastLeader].emit("forward", vote);
    }

    receiveHeartbeat(instance: number, id: number) {
        let date: Date = new Date();
        this.lastLeaderMsg = {time: date, id: id};
    }

    isLeader() {
        return this.paxosPort == this.lastLeader;
    }

    propose(x: number, y: number, colour: string) {
        this.proposer.propose(x, y, colour, cluster);
    }

    handleAccept(instance: number, ballot: number, vote: Pixel, socket: Socket) {
        this.acceptor.handleAccept(instance, ballot, vote, socket);
        // this broadcast doesn't work if it started second (means that first server is not connecting properly?)
    }

    handleAccepted(instance: number, ballot: number, vote: Pixel) {
        this.proposer.handleAccepted(instance, ballot, vote, cluster);
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
    });

    // Client to server events
    socket.on("emitUpdate", (x: number, y: number, color: string) => {
        console.log(x, y, color);
        // TODO: Handle emitted update from client i.e. start round of Paxos
        pd.propose(x, y, color)
    });
});

cluster.on("connection", (socket) => {
    socket.emit("recoverState", pd.currInstance, pd.state);

    socket.on("accepted", (instance: number, ballot: number, vote: Pixel) => {
        pd.handleAccepted(instance, ballot, vote);
    });

    socket.on("forward", (vote: Pixel) => {
        pd.propose(vote.x, vote.y, vote.color);
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

            socket.on("accept", (instance: number, ballot: number, vote: Pixel) => {
                pd.handleAccept(instance, ballot, vote, socket);
                // if accept works, broadcast on cluster.emit
            });

            socket.on("commit", (instance: number, ballot: number, vote: Pixel) => {
                pd.learn(instance, ballot, vote);
            });
            
            socket.on("leaderAlive", (instance: number, id: number) => {
                pd.receiveHeartbeat(instance, id);
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

            sockets[i] = socket;
        } else {
            frontend.listen(arr[i]-1);
            cluster.listen(arr[i]);
            console.log('cluster node listening on port ' + arr[i]);
        }
    }
});
