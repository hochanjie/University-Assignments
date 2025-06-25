package remotes;

import gui.MyShape;
import gui.WhiteboardGUI;
import database.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServerFacade extends Remote {

    void broadcastMessage(User user, User.Notification type, String chat) throws RemoteException;
    void showCurrentBoard(User user) throws RemoteException;
    void join(User user) throws RemoteException;
    void exit(User user) throws RemoteException;
    void broadcastImage(User user,byte[] image) throws RemoteException;
    void broadcastUpdate(WhiteboardGUI.Drawing updateType, MyShape shape) throws RemoteException;
    void end() throws RemoteException;
    void kick(String username) throws RemoteException;

}
