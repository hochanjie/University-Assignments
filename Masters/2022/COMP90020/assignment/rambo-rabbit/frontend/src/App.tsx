import Canvas from './Canvas';
import React, { useReducer } from 'react';
import { useSocket } from "./context/SocketContextProvider"
import DevConsole from './DevConsole';

// Stores the state of socket and handles the 1. error, 2. connect, 3. disconnect, 4. reconnect events
// Renders the Canvas ONLY when the initial websocket connection is established
function App() {
    const socket = useSocket()
    const [devMode, toggleDevMode] = useReducer((state: boolean) => !state, false)

    return (
        <div className="app" >
            <div className="content">
                <br/>
                <div className='header'> Pixel Board by Rambo Rabbit </div>
                <br/>
                {socket == null ? "waiting to be connected" : <Canvas socket={socket} />}
            </div>
            <br/>
            <div className="bottom-tray">
                <div className="dev-button-toggle" onClick={toggleDevMode}>
                    {devMode ? "Hide" : "Show"}
                </div>
                {devMode ? <DevConsole /> : null}
            </div>
        </div>
    );
}

export default App;
