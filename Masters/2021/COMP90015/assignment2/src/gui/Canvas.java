package gui;

import client.ClientFacade;
import database.User.Notification;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

public class Canvas extends JPanel implements MouseMotionListener, MouseListener, Serializable {

    private String username;
    private ClientFacade clientFacade;

    private final static int WIDTH = 750;
    private final static int HEIGHT = 500;

    private final static int TEXT_SIZE = 40;

    private final static Stroke PEN_STROKE = new BasicStroke(5);
    private final static Stroke ERASER_STROKE = new BasicStroke(20);

    private BufferedImage image;
    private Graphics2D g2;

    private Shape shape;
    private MyShape.Type tool;
    private Color colour;
    private String text = "Text here.";

    private Point startPoint;
    private Point previousPoint;
    private Point currentPoint;

    public Canvas(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
        username = clientFacade.getUsername();
        setBackground(Color.WHITE);
        setDoubleBuffered(false);
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        shape = null;
        colour = Color.BLACK;
        tool = MyShape.Type.PEN;

        addMouseListener(this);
        addMouseMotionListener(this);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }

        if (shape != null) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(tool != MyShape.Type.ERASER ? colour : Color.WHITE);
            g2.setStroke(tool != MyShape.Type.ERASER ? PEN_STROKE : ERASER_STROKE);
            g2.draw(shape);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        startPoint = e.getPoint();

        switch (tool) {

            case TEXT:
                g2.setColor(colour);
                g2.setFont(new Font("TimesRoman", Font.PLAIN, TEXT_SIZE));
                g2.drawString(text, startPoint.x, startPoint.y);
                try {
                    MyShape textShape = new MyShape(username, MyShape.Type.TEXT, text, g2.getFont(), colour, startPoint);
                    clientFacade.broadcastUpdate(WhiteboardGUI.Drawing.TEXT, textShape);
                } catch (RemoteException ex) {
                    clientFacade.showNotification(Notification.ERROR, "Connection with server lost. Please restart the client.");
                }
                repaintCanvas();
                break;
            default:
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        startPoint = previousPoint = e.getPoint();

        switch (tool) {

            case PEN:
            case ERASER:
            case LINE:
                shape = new Line2D.Double();
                break;

            case CIRCLE:
            case OVAL:
                shape = new Ellipse2D.Double();
                break;

            case RECTANGLE:
                shape = new Rectangle2D.Double();
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        switch (tool) {
            case PEN:
            case ERASER:
                if (startPoint.equals(previousPoint)) {
                    ((Line2D) shape).setLine(startPoint, startPoint);
                    g2.setColor(tool != MyShape.Type.ERASER ? colour : Color.WHITE);
                    g2.setStroke(tool != MyShape.Type.ERASER ? PEN_STROKE : ERASER_STROKE);
                    g2.draw(shape);
                }
                break;

            case OVAL:
            case RECTANGLE:
            case CIRCLE:
            case LINE:
                g2.setColor(colour);
                // g2.fill(drawing);
                g2.setStroke(PEN_STROKE);
                g2.draw(shape);
                break;
        }
        g2.setColor(colour);

        repaintCanvas();

        try {
            if (tool != MyShape.Type.TEXT) {
                MyShape myShape = new MyShape(username, tool, shape, colour);
                clientFacade.broadcastUpdate(WhiteboardGUI.Drawing.SHAPE, myShape);
            }

        } catch (RemoteException ex) {
            clientFacade.showNotification(Notification.ERROR, "Connection with server lost. Please restart the client.");
        }

        shape = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        currentPoint = e.getPoint();
        int x = Math.min(startPoint.x, e.getX());
        int y = Math.min(startPoint.y, e.getY());
        int width = Math.abs(startPoint.x - e.getX());
        int height = Math.abs(startPoint.y - e.getY());

        switch (tool) {

            case PEN:
            case ERASER:

                ((Line2D) shape).setLine(currentPoint, previousPoint);
                g2.setColor(tool != MyShape.Type.ERASER ? colour : Color.WHITE);
                g2.setStroke(tool != MyShape.Type.ERASER ? PEN_STROKE : ERASER_STROKE);
                g2.draw(shape);
                previousPoint = currentPoint;
                break;

            case LINE:

                ((Line2D) shape).setLine(startPoint, currentPoint);
                break;

            case CIRCLE:

                double radius = Math.sqrt(width * width + height * height);
                ((Ellipse2D) shape).setFrame(startPoint.getX() - radius, startPoint.getY() - radius, 2 * radius, 2 * radius);
                break;

            case OVAL:

                ((Ellipse2D) shape).setFrame(x, y, width, height);
                break;

            case RECTANGLE:

                ((Rectangle2D) shape).setFrame(x, y, width, height);
                break;
        }

        g2.setStroke(PEN_STROKE);
        repaintCanvas();

        try {
            if (tool != MyShape.Type.TEXT){

                MyShape myShape = new MyShape(username, tool, shape, colour);
                clientFacade.broadcastUpdate(WhiteboardGUI.Drawing.UPDATING, myShape);

            }

        } catch (RemoteException ex) {
            clientFacade.showNotification(Notification.ERROR, "Connection with server lost. Please restart the client.");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) { }

    public void setTool(MyShape.Type tool) {
        this.tool = tool;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void clear() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        repaintCanvas();
        clientFacade.showNotification(Notification.CLEAR, "The manager has cleared the board.");
    }

    public void drawShape(MyShape shape) {
        g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        repaintCanvas();
    }

    public void updateImage(BufferedImage bufferedImage) {
        image = bufferedImage;
        repaintCanvas();
    }

    public void updateShape(MyShape shape) {
        g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (shape.getType()) {
            case PEN:
            case ERASER:
                g2.setColor(shape.getType() != MyShape.Type.ERASER ? shape.getColour() : Color.WHITE);
                g2.setStroke(shape.getType() != MyShape.Type.ERASER ? PEN_STROKE : ERASER_STROKE);
                g2.draw(shape.getShape());
                break;
        }
        repaintCanvas();
    }

    public void openFile(File file) {
        try {
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            clientFacade.showNotification(Notification.ERROR, "Error opening file. Please try again later.");
        }
        repaintCanvas();
    }

    public void repaintCanvas() {
        g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        repaint();
    }

    public void saveCanvas(File file, String fileFormat) {
        BufferedImage imageSaved;

        if (fileFormat.equals("PNG")) {
            imageSaved = image;

        }
        else {
            imageSaved = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            imageSaved.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
        }

        try {
            ImageIO.write(imageSaved, fileFormat, file);
        }
        catch (IOException e) {
            clientFacade.showNotification(Notification.ERROR, "Error saving " + fileFormat);
        }
    }


}
