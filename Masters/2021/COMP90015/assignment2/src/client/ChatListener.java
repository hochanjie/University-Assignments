package client;

import remotes.IChatListener;
import database.User.Notification;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ChatListener extends UnicastRemoteObject implements IChatListener, Serializable {
    private ClientFacade clientFacade;

    public ChatListener(ClientFacade clientFacade) throws RemoteException {
        super();
        this.clientFacade = clientFacade;
    }

    @Override
    public void broadcastMessage(String message) throws RemoteException {
        clientFacade.showNotification(Notification.CHAT, message);
    }
}
