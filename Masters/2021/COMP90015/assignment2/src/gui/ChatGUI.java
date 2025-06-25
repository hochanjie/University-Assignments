package gui;

import client.ClientFacade;
import database.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ChatGUI {
    private JPanel mainPanel;
    private JButton sendButton;
    private JTextField messageTextField;
    private JTextArea chatTextArea;
    private JScrollPane chatScrollPane;

    private ClientFacade clientFacade;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public ChatGUI(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;

        messageTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            public void insertUpdate(DocumentEvent e) { changed(); }

            public void changed() {
                String message = messageTextField.getText().trim();
                sendButton.setEnabled(!message.isEmpty());
            }
        });

        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> {
            String message = messageTextField.getText().trim();
            messageTextField.setText("");
            messageTextField.requestFocus();

            try {
                clientFacade.broadcastMessage(message);
            }
            catch (Exception er) {
                clientFacade.showNotification(User.Notification.ERROR, "Error sending chat. Please try again later.");
            }
        });
    }

    public void showChat(String message) {
        if (!chatTextArea.getText().trim().isEmpty()) {
            chatTextArea.append("\n");
        }
        chatTextArea.append(message);
        chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
    }
}
