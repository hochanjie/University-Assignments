import { ServerToClientEvents, ClientToServerEvents } from "./ISocketInterfaces";
import { io, Socket } from "socket.io-client";

const args = process.argv.slice(2);
const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io("http://localhost:" + args[0]);
const socket2: Socket<ServerToClientEvents, ClientToServerEvents> = io("http://localhost:" + args[1]);

let x = 0;
const colors: string[] = ["#ff4500", "#ffa800", "#ffd635", "#00a268", "#7eed56", "#2450a4", "#3690ea", "#51e9f4", "#811e9f", "#b44ac0", "#ff99aa", "#9c6926", "#000000", "#898d90", "#d4d7d9", "#ffffff"]
socket.on("connect", () => {
    console.log("Connected to localhost:" + args[0]);
    socket.emit("getState");

    setInterval(() => {
        let color = colors[Math.floor(Math.random() * colors.length)];
        let color2 = colors[Math.floor(Math.random() * colors.length)];
        socket.emit("emitUpdate", 8, 8, color);
        socket2.emit("emitUpdate", 8, 8, color2);
    }, 200)
    
    /*
    setInterval(() => {
        x++;
        if (x < 10) {
            socket.emit("emitUpdate", 1, 1, "#000000");
            socket2.emit("emitUpdate", 1, 1, "#111111");
        }
    }, 1)
   setInterval(() => {
        let i = Math.floor(Math.random() * 16);
        let j = Math.floor(Math.random() * 16);
        let s = Math.round(Math.random());
        let color = colors[Math.floor(Math.random()*colors.length)];
        if (s) socket.emit("emitUpdate", i, j, color);
        else socket2.emit("emitUpdate", i, j, color);
   }, 50)
   */
});

let currState: string[][];
socket.on("emitState", (state: string[][]) => {
    currState = state;
});
socket.on("emitDelta", (x: number, y: number, color: string) => {
    currState[x][y] = color;
});


function generateRandomNumber(min: number, max: number) {
    return Math.floor(min + Math.random()*(max + 1 - min));
}

// Emit update to server
