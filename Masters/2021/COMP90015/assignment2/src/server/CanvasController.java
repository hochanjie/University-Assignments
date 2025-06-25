package server;

import gui.WhiteboardGUI.Drawing;
import gui.MyShape;
import database.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

public class CanvasController implements Serializable {

    private final static int WIDTH = 750;
    private final static int HEIGHT = 500;
    private final static Stroke PEN_STROKE = new BasicStroke(5);
    private final static Stroke ERASER_STROKE = new BasicStroke(20);

    private ServerFacade serverFacade;
    private BufferedImage bufferedImage;
    private Graphics2D g2;


    public CanvasController(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;

        this.bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void broadcastImage(User user, byte[] image) throws RemoteException {
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            g2 = (Graphics2D) bufferedImage.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(bufferedImage, null, 0, 0);
        }
        catch (IOException err) {
            user.getUserListener().notifyUser(User.Notification.ERROR, "Error in updating the latest canvas. Please restart the client.");
        }

        try {
            for (User currentUser: serverFacade.getCurrentUsers()) {
                if (!currentUser.getUserName().equals(user.getUserName())) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage, "png", output);
                        output.flush();
                        currentUser.getCanvasListener().receiveImage(output.toByteArray());
                    }
                    catch (IOException err) {
                        user.getUserListener().notifyUser(User.Notification.ERROR, "Error in updating the latest canvas. Please restart the client.");
                    }
                    output.close();
                }
            }
        }
        catch (IOException err) {
            user.getUserListener().notifyUser(User.Notification.ERROR, "Error in updating the latest canvas. Please restart the client.");
        }
    }

    public void broadcastUpdate(Drawing updateType, MyShape shape) throws RemoteException {
        switch (updateType) {
            case CLEAR:
                broadcastClear();
                break;
            case UPDATING:
            case SHAPE:
            case TEXT:
                broadcastShape(updateType, shape);
                break;
        }
    }

    public void showCurrentBoard(User user) throws RemoteException {

        g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(bufferedImage, null, 0, 0);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                ImageIO.write(bufferedImage, "png", output);
                output.flush();
                user.getCanvasListener().receiveImage(output.toByteArray());
            }
            catch (IOException err) {
                user.getUserListener().notifyUser(User.Notification.ERROR, "Error in broadcasting the latest canvas. Please restart the client.");
            }
            output.close();
        }
        catch (IOException err) {
            user.getUserListener().notifyUser(User.Notification.ERROR, "Error in broadcasting the latest canvas. Please restart the client.");
        }
    }

    private void broadcastClear() throws RemoteException {

        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (User currentUser: serverFacade.getCurrentUsers()) {
            currentUser.getCanvasListener().updateCanvas(Drawing.CLEAR, null);
        }
    }

    private void broadcastShape(Drawing updateType, MyShape shape) throws RemoteException {

        g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (updateType.equals(Drawing.UPDATING)) {
            switch (shape.getType()) {
                case PEN:
                case ERASER:
                    g2.setColor(shape.getType() != MyShape.Type.ERASER ? shape.getColour() : Color.WHITE);
                    g2.setStroke(shape.getType() != MyShape.Type.ERASER ? PEN_STROKE : ERASER_STROKE);
                    g2.draw(shape.getShape());
                    break;
            }
        }
        else {
            switch (shape.getType()) {
                case OVAL:
                case RECTANGLE:
                case CIRCLE:
                case PEN:
                case LINE:
                    g2.setColor(shape.getColour());
                    g2.setStroke(PEN_STROKE);
                    g2.draw(shape.getShape());
                    break;
                case TEXT:
                    g2.setColor(shape.getColour());
                    g2.setFont(shape.getFont());
                    g2.drawString(shape.getText(), shape.getPoint().x, shape.getPoint().y);
                    break;
            }
        }

        g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (User currentUser: serverFacade.getCurrentUsers()) {
            if (!currentUser.getUserName().equals(shape.getArtist())) {
                currentUser.getCanvasListener().updateCanvas(updateType, shape);
            }
        }
    }
}
