package gui;

import client.ClientFacade;
import util.inputParser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Lobby {
    private JPanel mainPanel;
    private JButton joinButton;

    private JLabel titleLabel;
    private JPanel titlePanel;

    private JPanel inputPanel;

    private JLabel ipLabel;
    private JLabel ipErrorLabel;
    private JTextField ipTextField;

    private JLabel portLabel;
    private JLabel portErrorLabel;
    private JTextField portTextField;

    private JLabel usernameLabel;
    private JLabel usernameErrorLabel;
    private JTextField usernameTextField;

    private boolean correctUsername = false;
    private boolean correctPort = false;
    private boolean correctIP = false;

    private ClientFacade clientFacade;
    JFrame frame;

    public Lobby(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
        start();
    }

    public void start() {
        frame = new JFrame("Distributed Whiteboard");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        usernameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            public void changed() {
                if (usernameTextField.getText().trim().isEmpty()) {
                    usernameErrorLabel.setText("Please key in a username.");
                    correctUsername = false;
                }
                else {
                    usernameErrorLabel.setText("");
                    correctUsername = true;
                }
                checkCorrectInput();
            }
        });

        ipTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            public void changed() {
                String ipInput = ipTextField.getText().trim();
                if (!inputParser.isValidIP(ipInput)) {
                    ipErrorLabel.setText("IP should be in an IPv4 format or \"localhost\".");
                    correctIP = false;
                }
                else {
                    ipErrorLabel.setText("");
                    correctIP = true;
                }
                checkCorrectInput();
            }
        });

        portTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                changed(e);
            }
            public void removeUpdate(DocumentEvent e) {
                changed(e);
            }
            public void insertUpdate(DocumentEvent e) {
                changed(e);
            }

            public void changed(DocumentEvent e) {
                String portInput = portTextField.getText().trim();
                if (!inputParser.isValidPort(portInput)) {
                    portErrorLabel.setText("Port should be a number between 1025 and 65535");
                    correctPort = false;
                }
                else {
                    portErrorLabel.setText("");
                    correctPort = true;
                }
                checkCorrectInput();
            }
        });

        joinButton.setEnabled(false);
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTextField.getText().trim();
                String ip = ipTextField.getText().trim();
                String portInput = portTextField.getText().trim();
                int port = Integer.parseInt(portInput);
                clientFacade.joinServer(username, ip, port);
            }
        });
    }

    public void showEnded() {
        String message = "The session has ended. You may start a new one as a manager or wait to join another.";
        showJOption(message, "Session Ended");
    }

    public void showDenied() {
        String message = "You have been denied access to the whiteboard by the manager.";
        showJOption(message, "Access Denied");
    }

    public void showFailure(String message) {
        showJOption(message, "Join Failure");
    }

    public void showKicked() {
        String message = "You have been kicked from the whiteboard by the manager.";
        showJOption(message, "Access Revoked");
    }

    public void showError(String message) {
        showJOption(message, "Error");
    }

    public void setVisible(boolean visibility) {
        frame.setVisible(visibility);
    }

    private void checkCorrectInput() {
        joinButton.setEnabled(correctIP && correctPort && correctUsername);
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
