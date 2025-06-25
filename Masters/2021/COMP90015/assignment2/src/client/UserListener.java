package client;

import remotes.IUserListener;
import database.User;
import database.User.Notification;

import javax.swing.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class UserListener extends UnicastRemoteObject implements IUserListener, Serializable {

    private ClientFacade clientFacade;

    public UserListener (ClientFacade clientFacade) throws RemoteException {
        super();
        this.clientFacade = clientFacade;
    }

    @Override
    public boolean askApproval(User newUser) throws RemoteException {
        String message = newUser.getUserName() + " wants to join the whiteboard! Do you wish to accept?";

        int response = JOptionPane.showConfirmDialog(null, message,"New User", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    @Override
    public void joinBoard() throws RemoteException {
        clientFacade.joinBoard();
    }

    @Override
    public void launchManager() throws RemoteException {
        clientFacade.launchManager();
    }

    @Override
    public void notifyUser(Notification type, String message) throws RemoteException {
        clientFacade.showNotification(type, message);
    }

    @Override
    public void updateUserList(ArrayList<User> users) throws RemoteException {
        clientFacade.updateUserList(users);
    }

}
