package client;

import gui.Lobby;
import gui.MyShape;
import gui.WhiteboardGUI;
import gui.WhiteboardGUI.Drawing;
import remotes.IServerFacade;

import database.User;
import database.User.Notification;

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ClientFacade is a class tha
 */
public class ClientFacade {

    private User user;
    private Lobby lobby;
    private WhiteboardGUI whiteboard;

    private CanvasListener canvasListener;
    private ChatListener chatListener;
    private UserListener userListener;

    private IServerFacade serverFacade;

    public ClientFacade() throws RemoteException {
        this.chatListener = new ChatListener(this);
        this.userListener = new UserListener(this);
        this.canvasListener = new CanvasListener(this);
    }

    public void setServerFacade(IServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return user.getUserName();
    }

    public boolean isManager() {
        return user.isManager();
    }

    public void launchLobby() {
        this.lobby = new Lobby(this);
    }

    public void launchManager() {
        user.makeManager();
        this.whiteboard = new WhiteboardGUI(this);
        lobby.setVisible(false);
    }

    public void joinBoard() throws RemoteException {
        this.whiteboard = new WhiteboardGUI(this);
        serverFacade.showCurrentBoard(user);
        lobby.setVisible(false);
    }

    /**
     * Explanation of the method
     * @param username explain what the argument takes in (type), give meaning
     * @param ip
     * @param port
     * @return kjfabsfkj
     */
    public void joinServer(String username, String ip, int port) {

        ClientFacade clientFacade = this;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            IServerFacade serverFacade = (IServerFacade) registry.lookup("ServerFacade");
            clientFacade.setServerFacade(serverFacade);
            return "";
        });

        try {
            future.get(3, TimeUnit.SECONDS);
            user = new User(username, canvasListener, chatListener, userListener);
            serverFacade.join(user);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            showNotification(Notification.ERROR, "Server timed out. Please try another ip or port.");
        }
        catch (Exception e) {
            String errorMessage = "Lookup of whiteboard server failed at " + ip + " on port " + port;
            showNotification(Notification.ERROR, errorMessage);
        }
    }

    /**
     *
     * @param type
     * @param message
     */
    public void showNotification(Notification type, String message) {
        switch (type) {
            case ENDED:
                lobby.showEnded();
                launchLobby();
                whiteboard.setVisible(false);
                break;
            case DENIED:
                lobby.showDenied();
                break;
            case FAILURE:
                lobby.showFailure(message);
                break;
            case KICKED:
                lobby.showKicked();
                launchLobby();
                whiteboard.setVisible(false);
                break;
            case ERROR:
                lobby.showError(message);
                break;
            case CHAT:
                whiteboard.showChat(message);
                break;
            case CLEAR:
                whiteboard.showClear(message);
                break;
        }
    }

    public void clearCanvas() {
        whiteboard.clearCanvas();
    }

    public void drawShape(MyShape shape) {
        whiteboard.drawShape(shape);
    }

    public void updateShape(MyShape shape) {
        whiteboard.updateShape(shape);
    }

    public void updateUserList(ArrayList<User> users) {
        whiteboard.updateUserList(users);
    }

    public void receiveImage(BufferedImage bufferedImage) {
        whiteboard.updateImage(bufferedImage);
    }

    public void broadcastImage(byte[] image) throws RemoteException {
        serverFacade.broadcastImage(user, image);
    }

    public void broadcastUpdate(Drawing updateType, MyShape shape) throws RemoteException {
        serverFacade.broadcastUpdate(updateType, shape);
    }

    public void broadcastMessage(String message) throws RemoteException {
        serverFacade.broadcastMessage(user, Notification.CHAT, message);
    }

    public void end() throws RemoteException {
        serverFacade.end();
    }

    public void exit() throws RemoteException {
        serverFacade.exit(user);
    }

    public void kickUser(String username) throws RemoteException {
        serverFacade.kick(username);
    }



}