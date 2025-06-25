import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css'
import { LogsContextProvider } from "./context/LogsContextProvider"
import { SocketContextProvider } from './context/SocketContextProvider'

const root = ReactDOM.createRoot(document.getElementById('root')!);
root.render(
    <LogsContextProvider>
        <SocketContextProvider>
            <App />
        </SocketContextProvider>
    </LogsContextProvider>
);
