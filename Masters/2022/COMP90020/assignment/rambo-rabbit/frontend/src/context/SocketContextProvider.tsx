import React, { createContext, useEffect, useContext, useReducer, useState } from "react";
import { io, Socket } from "socket.io-client";
import { useLogs } from "./LogsContextProvider"
import { ServerToClientEvents, ClientToServerEvents } from '../../../backend/src/ISocketInterfaces';

const SERVER_ADDRESSES: string[] = process.env.REACT_APP_PAXOS_SERVER?.split('|') ?? ["http://localhost:9989"]

// returns the current socket object
const socketContext = createContext<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null)
// returns current address, all possible addresses, and setAddressFunction
const serverAddressContext = createContext<[string, (index: number) => void,  string[]]>(["", () => null, [""]])

export function useSocket() {
    return useContext(socketContext)
}
export function useServerAddress() {
    return useContext(serverAddressContext)
}

// IMPORTANT!: Socket content provider depends on logs content provider
export function SocketContextProvider({ children }: { children: React.ReactNode }): JSX.Element {
    // For saving the requests between server and client
    const [_, appendLog] = useLogs()
    const [serverAddress, setServerAddress] = useReducer((state: string, i: number) => {
        if (i < 0 || i > SERVER_ADDRESSES.length) {
            console.log("index out of range")
            return state
        }
        return SERVER_ADDRESSES[i]
    }, SERVER_ADDRESSES[0])
    const [socket, setSocket] = useState<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);

    // open socket connection upon mount and disconnect when unmount
    useEffect(() => {
        // Start connection
        const newSocket = io(serverAddress, { transports: ['websocket', 'flashsocket'] });
        // remeber to socketId for debugging when the socket object is closed
        let socketId: string;
        appendLog({
            id: null, event: "emit connect", timestamp: new Date(),
            message: `Attempt to connect to ${serverAddress}`
        })
        // Only setSocket when we have established a connection
        newSocket.on("connect", () => {
            setSocket(newSocket)
            socketId = newSocket.id
            // log data
            appendLog({
                id: null, event: "on connect", timestamp: new Date(),
                message: `Obtained connection ${newSocket.id} from ${serverAddress}`
            })
            console.log(`Obtained ${newSocket.id} from server at ${serverAddress} on ${new Date().toLocaleString()}`)
        });
        newSocket.on('reconnect', (attempt) => {
            appendLog({
                id: null, event: "on reconnect", timestamp: new Date(),
                message: `reconnected ${newSocket.id} after ${attempt} attempts`
            })
            console.log(`reconnect successful on ${attempt}th attempt`)
        })
        newSocket.on('disconnect', (reason, details) => {
            setSocket(null)
            appendLog({
                id: null, event: "on disconnect", timestamp: new Date(),
                message: `Disconnected ${socketId} beause ${reason}`
            })
            console.log('disconnected', reason, details)
        })
        newSocket.on('error', (error) => {
            appendLog({
                id: null, event: "on error", timestamp: new Date(),
                message: error.name + " " + error.message
            })
            console.error(error)
        })
        // Close connection
        return () => {
            appendLog({
                id: null, event: "emit close", timestamp: new Date(),
                message: `closed ${socketId}`
            })
            newSocket.close()
        }
    }, [serverAddress, appendLog])

    return <socketContext.Provider value={socket}>
        <serverAddressContext.Provider value={[serverAddress, setServerAddress, SERVER_ADDRESSES]}>
            {children}
        </serverAddressContext.Provider>
    </socketContext.Provider>
}
