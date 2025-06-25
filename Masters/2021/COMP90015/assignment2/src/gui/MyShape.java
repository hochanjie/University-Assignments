package gui;

import java.awt.*;
import java.io.Serializable;

public class MyShape implements Serializable {

    public enum Type { PEN, LINE, CIRCLE, OVAL, RECTANGLE, TEXT, ERASER}

    private String artist;
    private Type type;
    private String text;
    private Shape shape = null;
    private Font font = null;
    private Color colour;
    private Point point = null;

    // Ordinary Shape
    public MyShape(String user, Type type, Shape shape, Color colour) {
        this.artist = user;
        this.type = type;
        this.shape = shape;
        this.colour = colour;
    }

    // Text
    public MyShape(String user, Type type, String text, Font font, Color colour, Point point) {
        this.artist = user;
        this.type = type;
        this.text = text;
        this.font = font;
        this.colour = colour;
        this.point = point;
    }


    public Type getType() {
        return type;
    }

    public String getArtist() {
        return artist;
    }

    public Color getColour() {
        return colour;
    }

    public Shape getShape() {
        return shape;
    }

    public String getText() {
        return text;
    }

    public Font getFont() {
        return font;
    }

    public Point getPoint() {
        return point;
    }




}
