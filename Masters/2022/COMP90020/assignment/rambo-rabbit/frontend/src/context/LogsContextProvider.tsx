import React, { useContext, createContext, useReducer } from "react";

export interface logInterface {
    id: number | null
    event: string,
    message: string,
    timestamp: Date,
}

const IdContext = createContext<[number, () => void]>([0, () => null])
const LogsContext = createContext<[logInterface[], (log: logInterface) => void]>([[], e => null])
export function useLogs() {
    return useContext(LogsContext)
}
export function useId() {
    return useContext(IdContext)
}

export function LogsContextProvider({ children }: { children: React.ReactNode }): JSX.Element {
    // For saving the requests between server and client
    const [logId, incLogId] = useReducer((state: number) => state + 1, 0)
    const [logs, appendLog] = useReducer((state: logInterface[], log: logInterface) => { return [...state, log] }, [])

    return <IdContext.Provider value={[logId, incLogId]}>
        <LogsContext.Provider value={[logs, appendLog]}>
            {children}
        </LogsContext.Provider>
    </IdContext.Provider>
}
