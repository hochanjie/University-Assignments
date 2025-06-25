package client;

import gui.MyShape;
import gui.WhiteboardGUI.Drawing;
import remotes.ICanvasListener;
import database.User.Notification;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CanvasListener extends UnicastRemoteObject implements ICanvasListener, Serializable {

    private ClientFacade clientFacade;

    public CanvasListener(ClientFacade clientFacade) throws RemoteException {
        super();
        this.clientFacade = clientFacade;
    }

    @Override
    public void receiveImage(byte[] rawImage) throws RemoteException {
        try {
            BufferedImage bufferedImage = javax.imageio.ImageIO.read(new ByteArrayInputStream(rawImage));
            clientFacade.receiveImage(bufferedImage);
        }
        catch (IOException err) {
            clientFacade.showNotification(Notification.ERROR, "Error in receiving image. Please restart the client.");
        }
    }

    @Override
    public void updateCanvas(Drawing updateType, MyShape shape) throws RemoteException {

        switch (updateType) {
            case CLEAR:
                clearCanvas();
                break;
            case SHAPE:
            case TEXT:
                drawShape(shape);
                break;
            case UPDATING:
                updateShape(shape);
                break;
        }
    }

    private void clearCanvas() throws RemoteException {
        clientFacade.clearCanvas();
    }

    private void drawShape(MyShape shape) throws RemoteException {
        clientFacade.drawShape(shape);
    }

    private void updateShape(MyShape shape) throws RemoteException {
        clientFacade.updateShape(shape);
    }



}
