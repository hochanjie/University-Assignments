import React from "react";
import { useLogs } from "./context/LogsContextProvider";

function printTime(time: Date): string {
    return `${time.getMinutes()}:${time.getSeconds()}.${time.getMilliseconds()}`
}

export default function LogTable(): JSX.Element {
    const [logs, _] = useLogs()
    return <div className="log-table">
        <div className="log-table-header">
            <div className="col-time"><b>Time</b></div> <div className="col-event"><b>Event</b></div> <div className="col-message"><b>Message</b></div>
        </div>
        {logs.map(({ timestamp, event, message }, i) => <div className="row" key={`${i}`} >
            <div className="col-time">{printTime(timestamp)}</div> <div className="col-event">{event}</div> <div className="col-message">{message}</div>
        </div>)}
    </div >
}
