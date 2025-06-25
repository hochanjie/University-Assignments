package remotes;

import database.User;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IUserListener extends Remote, Serializable {

    boolean askApproval(User newUser) throws RemoteException;
    void joinBoard() throws RemoteException;
    void launchManager() throws RemoteException;
    void notifyUser(User.Notification type, String message) throws RemoteException;
    void updateUserList(ArrayList<User> users) throws RemoteException;

}
