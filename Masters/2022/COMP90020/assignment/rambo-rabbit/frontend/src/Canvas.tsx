import React, { useEffect, useState, useRef } from 'react';
import Cell from './Cell';
import ColorPicker from './ColorPicker';
import { Socket } from "socket.io-client";
import { ServerToClientEvents, ClientToServerEvents } from '../../backend/src/ISocketInterfaces';
import { useLogs } from "./context/LogsContextProvider"
import InfoBar from './InfoBar';

// CONSTANTS
const GRID_SIZE: number = 16;
const INITIAL_COLOR: string = "#FFFFFF"

// Initialise the n*n array to particular numbers
function initializeGrid(): string[][] {
    console.log("Initialising grid");
    return new Array(GRID_SIZE).fill(new Array(GRID_SIZE).fill(INITIAL_COLOR));
}

interface GridInterface {
    grid: string[][]
    setSelectedXY: (selectedXY: [number, number]) => void;
    selectedXY: [number, number]
}
function Grid({ grid, setSelectedXY, selectedXY }: GridInterface): JSX.Element {
    return <div className="grid"> {
        grid.map((row, rowI) => <div className="row" key={`${rowI}`}>
            {row.map((color, colI) => <Cell key={`${colI}-${color}`} rowI={rowI} colI={colI} selectedXY={selectedXY}
                color={color} setSelectedXY={setSelectedXY} />)}
        </div>)
    } </div>
}

// Instead of using null, we use an special coordinate to denote not selecting anything
export const NO_SELECTION: [number, number] = [-1, -1]
interface CanvasInterface {
    socket: Socket<ServerToClientEvents, ClientToServerEvents>
}
function Canvas({ socket }: CanvasInterface) {
    const [grid, setGrid] = useState<string[][]>(initializeGrid);
    const [selectedXY, setSelectedXY] = useState<[number, number]>(NO_SELECTION);
    const focused = NO_SELECTION[0] != selectedXY[0] && NO_SELECTION[1] != selectedXY[1]
    const [_, appendLog] = useLogs()
    const canvasRef = useRef<HTMLDivElement>(null)

    // Cancel operation when clicked outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent):void {
            if (!canvasRef.current) return
            if (!event.target) return
            const target = (event.target as HTMLElement)
            if (!canvasRef.current.contains(target)) setSelectedXY(NO_SELECTION)
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside, true);
        };
    }, [canvasRef]);

    const updateCellColor = (color: string) => {
        if (selectedXY != null) {
            console.log("send emitUpdate");
            appendLog({
                id: null, event: "emit emitUpdate", timestamp: new Date(),
                message: `(${selectedXY[0]},${selectedXY[1]}) ${color}`
            })
            socket.emit("emitUpdate", selectedXY[0], selectedXY[1], color);
            setSelectedXY(NO_SELECTION);
        }
    }

    // On initial page load, setup socket connection with server
    // and retrieve the current state of the canvas
    useEffect(() => {
        // Add handlers
        socket.on("emitState", (state: string[][]) => {
            console.log("received emitState");
            appendLog({
                id: null, event: "received emitUpdate", timestamp: new Date(),
                message: ""
            })
            setGrid(state);
        })
        socket.on("emitDelta", (x: number, y: number, color: string) => {
            console.log("received emitDelta");
            appendLog({
                id: null, event: "received emitDelta", timestamp: new Date(),
                message: `(${x}, ${y}) ${color}`
            })
            setGrid((grid) => {
                const newGrid = JSON.parse(JSON.stringify(grid))
                newGrid[x][y] = color;
                return newGrid
            })
        })
        // request for the latest state
        socket.emit("getState")
        appendLog({
            id: null, event: "emit getState", timestamp: new Date(),
            message: ""
        })

        // turn off handlers when unounted
        return () => { socket.off("emitDelta"); socket.off("emitState") }

    }, [socket, appendLog])

    return (
        <div className="canvas" ref={canvasRef}>
            <div className='canvas-top'>
                {focused ? <InfoBar selectedXY={selectedXY} /> : null}
            </div>
            <div className='canvas-mid'>
                <Grid grid={grid} setSelectedXY={setSelectedXY} selectedXY={selectedXY} />
            </div>
            <div className='canvas-bot'>
                {focused ? <ColorPicker updateCellColor={updateCellColor} /> : null}
            </div>
        </div >
    );
}

export default Canvas;
