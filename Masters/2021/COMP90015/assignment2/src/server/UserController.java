package server;

import database.UserRegistry.Status;
import database.User;
import database.User.Notification;

import java.io.Serializable;
import java.rmi.RemoteException;

public class UserController implements Serializable {

    private ServerFacade serverFacade;

    public UserController(ServerFacade serverFacade) throws RemoteException {
        this.serverFacade = serverFacade;
    }

    public void end() throws RemoteException {

        for (User user : serverFacade.getCurrentUsers()) {
            serverFacade.removeUser(user.getUserName());
            user.getUserListener().notifyUser(Notification.ENDED, "");
        }
    }

    public void exit(User user) throws RemoteException {

        serverFacade.removeUser(user.getUserName());

        if (!serverFacade.getCurrentUsers().isEmpty()) {
            serverFacade.broadcastMessage(user, Notification.LEFT, "");

            for (User currentUsers : serverFacade.getCurrentUsers()) {
                currentUsers.getUserListener().updateUserList(serverFacade.getCurrentUsers());
            }
        }
    }
    public void join(User newUser) throws RemoteException {

        Status response = serverFacade.addUser(newUser);

        try {
            switch (response) {

                case MANAGER:
                    newUser.getUserListener().launchManager();
                    serverFacade.broadcastMessage(newUser, Notification.JOINED, "");

                    for (User currentUsers : serverFacade.getCurrentUsers()) {
                        currentUsers.getUserListener().updateUserList(serverFacade.getCurrentUsers());
                    }

                    break;


                case SUCCESS:

                    User manager = serverFacade.getManager();

                    boolean approval = manager.getUserListener().askApproval(newUser);

                    if (approval) {
                        newUser.getUserListener().joinBoard();
                        serverFacade.broadcastMessage(newUser, Notification.JOINED, "");

                        for(User currentUsers : serverFacade.getCurrentUsers()) {
                            currentUsers.getUserListener().updateUserList(serverFacade.getCurrentUsers());
                        }
                        break;
                    }

                    newUser.getUserListener().notifyUser(Notification.DENIED, "");
                    serverFacade.removeUser(newUser.getUserName());
                    break;


                case FAILURE:
                    String errorMessage = "Username already taken. Please try another.";
                    newUser.getUserListener().notifyUser(Notification.FAILURE, errorMessage);
                    break;
            }
        }
        catch (Exception e) {
            String errorMessage = "Data server remote registry set up failed";
            newUser.getUserListener().notifyUser(Notification.ERROR, errorMessage);

        }
    }

    public void kick(String username) throws RemoteException {

        User kickedUser = serverFacade.removeUser(username);

        if (kickedUser != null) {
            kickedUser.getUserListener().notifyUser(Notification.KICKED, "");

            if (!serverFacade.getCurrentUsers().isEmpty()) {
                serverFacade.broadcastMessage(kickedUser, Notification.KICKED, "");

                for (User currentUsers : serverFacade.getCurrentUsers()) {
                    currentUsers.getUserListener().updateUserList(serverFacade.getCurrentUsers());
                }
            }
        }
    }
}
