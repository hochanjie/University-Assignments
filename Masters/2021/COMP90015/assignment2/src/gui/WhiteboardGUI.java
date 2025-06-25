package gui;

import client.ClientFacade;
import database.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class WhiteboardGUI {

    public enum Drawing {UPDATING, SHAPE, TEXT, CLEAR}

    private String filePath = "";
    private String fileFormat;
    private Color currentColour;

    private ClientFacade clientFacade;
    private UsersGUI usersGUI;
    private ChatGUI chatGUI;
    private Canvas canvas;

    private JFrame frame;
    private JPanel mainPanel;
    private JTabbedPane chatTab;
    private JTabbedPane userTab;

    private JPanel managerToolsPanel;
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton saveAsButton;
    private JButton closeButton;

    private JPanel userToolsPanel;

    private JPanel toolsPanel;
    private JButton penButton;
    private JButton lineButton;
    private JButton circleButton;
    private JButton ovalButton;
    private JButton rectangleButton;
    private JButton textButton;
    private JButton eraseButton;

    private JPanel colourPanel;
    private JLabel colourLabel;
    private JButton colour1Button;
    private JButton colour2Button;
    private JButton colour3Button;
    private JButton colour4Button;
    private JButton colour5Button;
    private JButton colour6Button;
    private JButton colour7Button;
    private JButton colour8Button;
    private JButton colour9Button;
    private JButton colour10Button;
    private JButton colour11Button;
    private JButton colour12Button;
    private JButton colour13Button;
    private JButton colour14Button;
    private JButton colour15Button;
    private JButton colour16Button;
    private JButton customColourButton;
    private JTabbedPane canvasTab;
    private JTextField inputText;

    private JButton[] COLOUR_BUTTONS = { this.colour1Button, this.colour2Button,
                                         this.colour3Button, this.colour4Button,
                                         this.colour5Button, this.colour6Button,
                                         this.colour7Button, this.colour8Button,
                                         this.colour9Button, this.colour10Button,
                                         this.colour11Button, this.colour12Button,
                                         this.colour13Button, this.colour14Button,
                                         this.colour15Button, this.colour16Button };

    private static Color[] DEFAULT_COLOURS = { Color.BLACK, Color.WHITE,
                                               Color.DARK_GRAY, Color.GRAY,
                                               Color.LIGHT_GRAY, Color.BLUE,
                                               new Color(0, 128, 255), Color.CYAN,
                                               Color.GREEN, new Color(102, 51, 0),
                                               Color.YELLOW, Color.ORANGE,
                                               Color.RED, Color.MAGENTA,
                                               Color.PINK, new Color(102, 0, 204) };

    public WhiteboardGUI(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
        usersGUI = new UsersGUI(clientFacade);
        chatGUI = new ChatGUI(clientFacade);
        canvas = new Canvas(clientFacade);

        frame = new JFrame("Distributed Whiteboard");
        chatTab.addTab("Chat", chatGUI.getMainPanel());
        userTab.addTab("Users", usersGUI.getMainPanel());
        canvasTab.addTab("Canvas", canvas);

        frame.setContentPane(mainPanel);
        frame.addWindowListener(windowListener);
        frame.pack();
        frame.setVisible(true);

        penButton.addActionListener(userToolsListener);
        lineButton.addActionListener(userToolsListener);
        circleButton.addActionListener(userToolsListener);
        ovalButton.addActionListener(userToolsListener);
        rectangleButton.addActionListener(userToolsListener);
        textButton.addActionListener(userToolsListener);
        eraseButton.addActionListener(userToolsListener);
        inputText.addFocusListener(textListener);

        if (clientFacade.isManager()) {
            managerToolsPanel.setVisible(true);
            newButton.addActionListener(managerToolsListener);
            openButton.addActionListener(managerToolsListener);
            saveButton.addActionListener(managerToolsListener);
            saveAsButton.addActionListener(managerToolsListener);
            closeButton.addActionListener(managerToolsListener);
        } else {
            managerToolsPanel.setVisible(false);
        }

        setDefaultColours();
        currentColour = Color.BLACK;
        customColourButton.addActionListener(colourListener);

    }

    public void showChat(String message) {
        chatGUI.showChat(message);
    }

    public void showClear(String message) {
        showJOption(message, "Board Cleared");
    }

    public void updateUserList(ArrayList<User> users) {
        usersGUI.updateUserList(users);
    }

    private void setDefaultColours() {

        int i = 0;

        for (JButton button : COLOUR_BUTTONS) {

            button.setOpaque(true);
            button.addActionListener(colourListener);
            button.setForeground(DEFAULT_COLOURS[i]);
            button.setBackground(DEFAULT_COLOURS[i]);
            i++;
        }
    }

    public void setVisible(boolean visibility) {
        frame.setVisible(visibility);
    }

    WindowAdapter windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
            closeWhiteboard();
        }
    };

    ActionListener userToolsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == penButton) {
                canvas.setTool(MyShape.Type.PEN);
            }
            else if (e.getSource() == lineButton) {
                canvas.setTool(MyShape.Type.LINE);
            }
            else if (e.getSource() == circleButton) {
                canvas.setTool(MyShape.Type.CIRCLE);
            }
            else if (e.getSource() == ovalButton) {
                canvas.setTool(MyShape.Type.OVAL);
            }
            else if (e.getSource() == rectangleButton) {
                canvas.setTool(MyShape.Type.RECTANGLE);
            }
            else if (e.getSource() == textButton) {
                canvas.setTool(MyShape.Type.TEXT);

            }
            else if (e.getSource() == eraseButton) {
                canvas.setTool(MyShape.Type.ERASER);
            }
        }
    };

    FocusListener textListener = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            String text = inputText.getText();
            canvas.setText(text);
        }

        @Override
        public void focusLost(FocusEvent e) {
            String text = inputText.getText();
            canvas.setText(text);
        }
    };

    ActionListener managerToolsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == newButton) {

                String message = "Are you sure you want to clear the canvas? This cannot be undone.";
                int response = JOptionPane.showConfirmDialog(null, message, "Clear Canvas", JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    try {
                        clientFacade.broadcastUpdate(Drawing.CLEAR, null);
                    }
                    catch (Exception err) {
                        message = "Error in clearing the canvas";
                        showJOption(message, "Error");
                    }
                }
            }
            else if (e.getSource() == openButton) {
                openFile();
            }
            else if (e.getSource() == saveButton) {

                File file;

                if (filePath.isEmpty()) {
                    file = saveAsFile();
                }
                else {
                    file = new File(filePath);
                }

                if (file != null) {
                    canvas.saveCanvas(file, fileFormat);
                }
            }
            else if (e.getSource() == saveAsButton) {

                File file = saveAsFile();

                if (file != null) {
                    canvas.saveCanvas(file, fileFormat);
                }
            }
            else if (e.getSource() == closeButton) {
                closeWhiteboard();
            }
        }
    };

    ActionListener colourListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int i = 0;

            for (JButton button : COLOUR_BUTTONS) {
                if (e.getSource() == button) {
                    currentColour = DEFAULT_COLOURS[i];
                    canvas.setColour(DEFAULT_COLOURS[i]);
                    break;
                }
                i++;
            }
            if (e.getSource() == customColourButton) {
                canvas.setColour(JColorChooser.showDialog(null, "Choose a Colour", currentColour));
            }
        }
    };

    public void clearCanvas() {
        canvas.clear();
    }

    public void drawShape(MyShape shape) {
        canvas.drawShape(shape);
    }

    public void updateImage(BufferedImage image) {
        canvas.updateImage(image);
    }

    public void updateShape(MyShape shape) {
        canvas.updateShape(shape);
    }

    private void closeWhiteboard() {
        String message = "Are you sure you want to close the whiteboard?";
        if (clientFacade.isManager()) {
            message += " The whiteboard will be terminated for all users.";
        }
        int result = JOptionPane.showConfirmDialog(frame, message, "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        try {
            if (result == JOptionPane.YES_OPTION) {
                if (clientFacade.isManager()) {
                    clientFacade.end();
                }
                else {
                    clientFacade.exit();
                    clientFacade.launchLobby();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                }
            }
            else {
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        }
        catch (Exception e) {
            clientFacade.showNotification(User.Notification.ERROR, "Error when exiting. Please restart the client.");
        }
    }

    private void openFile() {

        JFileChooser fileChooser = new JFileChooser();
        int response = fileChooser.showOpenDialog(frame);

        if (response == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            canvas.openFile(file);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                javax.imageio.ImageIO.write(ImageIO.read(file), "png", output);
                output.flush();
                output.close();
            }
            catch (IOException err) {
                clientFacade.showNotification(User.Notification.ERROR, "Error writing file. Please try again later");
            }

            try {
                clientFacade.broadcastImage(output.toByteArray());
            }
            catch (RemoteException ex) {
                clientFacade.showNotification(User.Notification.ERROR, "Connection with server lost. Please restart the client.");
            }
        }
    }

    private File saveAsFile() {

        JFileChooser fileChooser = new JFileChooser();
        FileFilter png = new FileNameExtensionFilter("PNG", "png");
        FileFilter jpg = new FileNameExtensionFilter("JPG", "jpg");

        fileChooser.addChoosableFileFilter(png);
        fileChooser.addChoosableFileFilter(jpg);

        fileChooser.setFileFilter(png);

        int response = fileChooser.showSaveDialog(frame);

        if (response == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();
            filePath = file.getAbsolutePath();
            fileFormat = fileChooser.getFileFilter().getDescription();

            if (fileFormat.equals("JPG")) {
                if (!filePath.toLowerCase().endsWith(".jpg")) {
                    filePath = filePath + ".jpg";
                    file = new File(file + ".jpg");
                }
                return file;
            }
            else if (fileFormat.equals("PNG")) {
                if (!filePath.toLowerCase().endsWith(".png")) {
                    filePath = filePath + ".png";
                    file = new File(file + ".png");
                }
                return file;
            }
        }
        return null;
    }

    private void showJOption(String message, String title) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
