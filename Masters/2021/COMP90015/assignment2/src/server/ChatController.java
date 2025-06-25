package server;

import database.User;
import database.User.Notification;

import java.io.Serializable;
import java.rmi.RemoteException;

public class ChatController implements Serializable {
    private ServerFacade serverFacade;

    public ChatController(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void broadcastMessage(User user, Notification type, String chat) throws RemoteException {
        String message = user.getUserName();
        switch (type) {
            case KICKED:
                message += " has been kicked from the board! :o";
                break;
            case JOINED:
                message += " has joined the board! Say hi! :)";
                break;
            case LEFT:
                message += " has left the board! :(";
                break;
            default:
                message +=  ": " + chat;
        }

        for (User currentUsers : serverFacade.getCurrentUsers()) {
            currentUsers.getChatListener().broadcastMessage(message);
        }
    }
}
