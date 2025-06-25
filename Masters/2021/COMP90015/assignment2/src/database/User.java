package database;

import remotes.IChatListener;
import remotes.ICanvasListener;
import remotes.IUserListener;

import java.io.Serializable;

public class User implements Serializable {

    public enum Notification {KICKED, DENIED, FAILURE, ENDED, ERROR, CHAT, JOINED, LEFT, CLEAR}

    private String username;
    private boolean isManager;

    private ICanvasListener canvasListener;

    private IChatListener chatListener;
    private IUserListener userListener;

    public User(String username, ICanvasListener canvasListener, IChatListener chatListener, IUserListener userListener) {
        this.username = username;
        this.canvasListener = canvasListener;
        this.chatListener = chatListener;
        this.userListener = userListener;
        this.isManager = false;
    }

    public String getUserName() {
        return this.username;
    }

    public ICanvasListener getCanvasListener() {
        return canvasListener;
    }

    public IChatListener getChatListener() {
        return chatListener;
    }

    public IUserListener getUserListener() {
        return userListener;
    }

    public boolean isManager() {
        return isManager;
    }

    public void makeManager() {
        isManager = true;
    }

}
