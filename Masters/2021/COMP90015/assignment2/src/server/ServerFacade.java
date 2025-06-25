package server;

import database.UserRegistry.Status;
import gui.MyShape;
import gui.WhiteboardGUI;
import remotes.IDatabaseServer;
import remotes.IServerFacade;
import database.User;
import database.User.Notification;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ServerFacade extends UnicastRemoteObject implements IServerFacade, Serializable {

    private static ServerFacade instance = null;

    private static IDatabaseServer databaseServer;
    private static UserController userController;
    private static ChatController chatController;
    private static CanvasController canvasController;

    private ServerFacade() throws RemoteException {
        userController = new UserController(this);
        chatController = new ChatController(this);
        canvasController = new CanvasController(this);
    }

    public static ServerFacade getInstance() throws RemoteException {
        if (instance == null) {
            instance = new ServerFacade();
        }
        return instance;
    }

    public void runServer(String ip, int port) {

        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            registry.rebind("ServerFacade", this);
            System.out.println("Bound the server at " + ip + " on port " + port);
        }
        catch (Exception e) {
            System.out.println("Rebinding of server failed at localhost on port " + ip);
            System.exit(1);
        }
    }

    public static void connectDatabase(String ip, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            databaseServer = (IDatabaseServer) registry.lookup("DatabaseServer");
            System.out.println("Connected to database at " + ip + " at port " + port);
        }
        catch (Exception e) {
            System.out.println("Failed to connect to database at " + ip + " at port " + port);
            System.exit(1);
        }
    }

    // Methods to be used by other clients
    @Override
    public void broadcastUpdate(WhiteboardGUI.Drawing updateType, MyShape shape) throws RemoteException {
        canvasController.broadcastUpdate(updateType, shape);
    }

    @Override
    public void showCurrentBoard(User user) throws RemoteException {
        canvasController.showCurrentBoard(user);
    }

    @Override
    public void broadcastMessage(User user, Notification type, String chat) throws RemoteException {
        chatController.broadcastMessage(user, type, chat);
    }

    @Override
    public void exit(User user) throws RemoteException {
        userController.exit(user);
    }

    @Override
    public void join(User user) throws RemoteException {
        userController.join(user);
    }

    @Override
    public void broadcastImage(User user,byte[] image) throws RemoteException {
        canvasController.broadcastImage(user, image);
    }

    @Override
    public void end() throws RemoteException {
        userController.end();
    }

    @Override
    public void kick(String username) throws RemoteException {
        userController.kick(username);
    }

    // Methods used by controllers on the server
    public ArrayList<User> getCurrentUsers() throws RemoteException {
        return databaseServer.getCurrentUsers();
    }

    public User getManager() throws RemoteException {
        return databaseServer.getManager();
    }

    public Status addUser(User newUser) throws RemoteException {
        return databaseServer.addUser(newUser);
    }

    public User removeUser(String username) throws RemoteException {
        return databaseServer.removeUser(username);
    }
}
