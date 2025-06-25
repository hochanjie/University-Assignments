package remotes;

import gui.MyShape;
import gui.WhiteboardGUI.Drawing;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICanvasListener extends Remote, Serializable {

    void receiveImage(byte[] rawImage) throws RemoteException;
    void updateCanvas(Drawing updateType, MyShape shape) throws RemoteException;

}
