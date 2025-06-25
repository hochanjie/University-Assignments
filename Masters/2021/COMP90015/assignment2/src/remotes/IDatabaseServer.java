package remotes;

import database.UserRegistry;
import database.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IDatabaseServer extends Remote {

    User getManager() throws RemoteException;
    ArrayList<User> getCurrentUsers() throws RemoteException;
    UserRegistry.Status addUser(User newUser) throws RemoteException;
    User removeUser(String username) throws RemoteException;

}
