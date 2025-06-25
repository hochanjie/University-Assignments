package database;

import remotes.IDatabaseServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class DatabaseServer extends UnicastRemoteObject implements IDatabaseServer {

    private static DatabaseServer instance = null;
    private static UserRegistry userRegistry = null;

    private DatabaseServer() throws RemoteException {
        super();
        userRegistry = UserRegistry.getInstance();
    }

    public static DatabaseServer getInstance() throws RemoteException {
        if (instance == null) {
            instance = new DatabaseServer();
        }
        return instance;
    }

    @Override
    public User getManager() throws RemoteException {
        return userRegistry.getManager();
    }

    @Override
    public ArrayList<User> getCurrentUsers() throws RemoteException {
        return userRegistry.getCurrentUsers();
    }

    @Override
    public UserRegistry.Status addUser(User newUser) throws RemoteException {
        return userRegistry.addUser(newUser);
    }

    @Override
    public User removeUser(String username) throws RemoteException {
        return userRegistry.removeUser(username);
    }

    public void runDatabase(String port) {
        try {
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));
            registry.bind("DatabaseServer", this);
            System.out.println("Bound the database server at localhost on port " + port);

        } catch (Exception e) {
            System.out.println("Binding of database server failed at localhost on port " + port);

            try {
                UnicastRemoteObject.unexportObject(this, false);
            } catch (Exception err) {
                System.out.println("Unexport of remote database failed");
            }
            System.exit(1);
        }
    }
}
