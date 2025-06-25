package gui;

import client.ClientFacade;
import database.User;

import javax.swing.*;
import java.util.ArrayList;

public class UsersGUI {
    private JButton kickButton;
    private JScrollPane userScrollPane;
    private JPanel mainPanel;
    private JList userList;

    private ClientFacade clientFacade;

    public UsersGUI(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
        userList.setEnabled(clientFacade.isManager());
        kickButton.setVisible(clientFacade.isManager());


        kickButton.addActionListener(e -> {
            try {
                String selected = (String) userList.getSelectedValue();
                if (selected.equals(clientFacade.getUsername())) {
                    clientFacade.showNotification(User.Notification.ERROR, "You can't kick yourself!");
                }
                else {
                    try {
                        clientFacade.kickUser(selected);
                    }
                    catch (Exception er) {
                        clientFacade.showNotification(User.Notification.ERROR, "Error when kicking user. Please try again later.");
                    }
                }
            }
            catch (Exception er) {
                clientFacade.showNotification(User.Notification.ERROR, "Error when kicking user. Please try again later.");
            }
        });

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void updateUserList(ArrayList<User> users) {
        DefaultListModel listModel = new DefaultListModel();

        for (User currentUser: users) {
            listModel.addElement(currentUser.getUserName());
        }
        userList.setModel(listModel);
    }
}
