import React from "react";

import LogTable from './LogTable';
import { useSocket, useServerAddress } from "./context/SocketContextProvider";

export default function DevConsole(): JSX.Element {
    const socket = useSocket()
    const [serverAddress, setServerAddress, SERVER_ADDRESSES] = useServerAddress()

    return <div className='dev-console'>
        <div className="connection-id">Connection ID: {socket ? socket.id : "pending"}</div>
        <fieldset className="sever-selector">
            <legend>Choose Server</legend>
            {SERVER_ADDRESSES.map((address, i) => <div key={address}>
                <input type="radio" id={address} name="server_address" value={address}
                    onChange={() => setServerAddress(i)}
                    checked={address === serverAddress} />
                <label htmlFor={address}>{address}</label>
            </div>)}
        </fieldset>
        <LogTable />
    </div>
}
