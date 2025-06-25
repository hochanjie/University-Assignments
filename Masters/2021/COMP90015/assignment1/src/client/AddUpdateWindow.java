/**
 * @Title: COMP90015 Distributed Systems 2021 Sem 1 Assignment 1
 * @Description: Multithreaded dictionary server
 * @author Chan Jie Ho
 * @studentID 961948
 * @email chanjieh@student.unimelb.edu.au
 * @date 19/4/2021
 */

package client;

import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;

/**
 * A class to handle creating the window to prompt the user to input the meanings.
 */

public class AddUpdateWindow {

    /* ============================================================================================================== */
    /* GUI COMPONENTS */
    /* -------------- */

    private JPanel mainPanel;
    private JPanel responsePanel;
    private JLabel wordLabel;
    private JTextField meaningField;
    private JLabel meaningLabel;
    private JLabel givenWordLabel;
    private JButton actionButton;
    private JLabel errorLabel;
    private JPanel headerPanel;
    private JLabel headerLabel;
    private JLabel instructionLabel;

    /* ============================================================================================================== */
    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * AddUpdateWindow constructor that creates the window.
     *
     * @param client Main client frame
     * @param word Word that the meaning is for
     * @param action add or update
     */
    public AddUpdateWindow(DictClient client, String word, String action) {
        String title = action.substring(0, 1).toUpperCase() + action.substring(1) + " Meaning";
        JFrame frame = new JFrame(title);
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Disable the buttons on the main client frame when this frame is open
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                client.setWaiting(true);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                client.setWaiting(false);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                client.setWaiting(false);
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        givenWordLabel.setText(word);
        actionButton.setText(action.substring(0, 1).toUpperCase() + action.substring(1));
        actionButton.setEnabled(false);
        errorLabel.setVisible(false);

        // Only enable the add/update action button if the text field has a non-whitespace character
        meaningField.getDocument().addDocumentListener(new DocumentListener() {
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
                if (meaningField.getText().trim().isEmpty()){
                    actionButton.setEnabled(false);
                }
                else {
                    actionButton.setEnabled(true);
                }

            }
        });

        actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String meaning = meaningField.getText();
                if (meaning.trim().isEmpty()) {
                    errorLabel.setText("Meaning must contain at least 1 character");
                    errorLabel.setVisible(true);
                }
                else {
                    // Parse the meanings and create the JSON object
                    String [] meanings = meaning.toLowerCase().split(", ");
                    JSONObject request = new JSONObject();
                    request.put("request", action);
                    request.put("word", word.toLowerCase());
                    request.put("meanings", Arrays.toString(meanings));

                    // Get the client to send the request
                    client.sendRequest(request);
                    frame.dispose();
                }
            }
        });
    }

    /* ============================================================================================================== */
}


