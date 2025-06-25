/**
 * @Title: COMP90015 Distributed Systems 2021 Sem 1 Assignment 1
 * @Description: Multithreaded dictionary server
 * @author Chan Jie Ho
 * @studentID 961948
 * @email chanjieh@student.unimelb.edu.au
 * @date 19/4/2021
 */

package client;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A class to handle all server-client communication and most of the request creation. This class also handles the
 * creation of the main GUI.
 */

public class DictClient extends PlainDocument {

    /* ============================================================================================================== */
    /* GUI COMPONENTS */
    /* -------------- */

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel interactivePanel;
    private JLabel headerLabel;
    private JComboBox searchBox;
    private JButton searchButton;
    private JButton addButton;
    private JButton removeButton;
    private JButton updateButton;
    private JButton fullDictButton;
    private JLabel enterLabel;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox doNotSuggestWordCheckBox;

    /* ============================================================================================================== */
    /* INSTANCE VARIABLES */
    /* ------------------ */

    private boolean suggest = true;
    private boolean selecting = false;

    private int version = -1;
    private ArrayList<String> dictModel;
    private static HashMap<String, String[]> dict = new HashMap<String, String[]>();

    private static int port = 1999;
    private static String ip = "localhost";
    private static DataInputStream input;
    private static DataOutputStream output;

    /* ============================================================================================================== */
    /* MAIN, CONSTRUCTOR, SETTER */
    /* ------------------------- */

    /**
     * Main function that launches the client.
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Distributed Dictionary");
        frame.setContentPane(new DictClient().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * DictClient constructor that creates the client GUI.
     */
    public DictClient() {

        // Start the client and get the initial model for the search box
        startClient();
        JTextComponent editor = (JTextComponent) searchBox.getEditor().getEditorComponent();
        editor.setDocument(this);

        // Add listeners for components
        doNotSuggestWordCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1)
                    suggest = false;
                else
                    suggest = true;
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = editor.getText();

                if (word.trim().isEmpty()) {
                    new ResponseWindow("Add at least one character!", null);
                }
                else {
                    JSONObject request = new JSONObject();
                    request.put("request", "search");
                    request.put("word", word.toLowerCase());
                    sendRequest(request);
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = editor.getText();
                if (word.trim().isEmpty()) {
                    new ResponseWindow("Add at least one character!", null);
                }
                else {
                    new AddUpdateWindow(DictClient.this, word, "add");
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = editor.getText();
                if (word.trim().isEmpty()) {
                    new ResponseWindow("Add at least one character!", null);
                }
                else {
                    JSONObject request = new JSONObject();
                    request.put("request", "remove");
                    request.put("word", word.toLowerCase());
                    sendRequest(request);
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = editor.getText();
                if (word.trim().isEmpty()) {
                    new ResponseWindow("Add at least one character!", null);
                }
                else {
                    new AddUpdateWindow(DictClient.this, word, "update");
                }
            }
        });

        fullDictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject request = new JSONObject();
                request.put("request", "full");
                request.put("version", version);
                sendRequest(request);
            }
        });
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Disables the buttons in the main client GUI while waiting for user to finish using the add/update window.
     *
     * @param wait Determines if the buttons should be disabled.
     */
    public void setWaiting(boolean wait) {
        addButton.setEnabled(!wait);
        searchButton.setEnabled(!wait);
        updateButton.setEnabled(!wait);
        removeButton.setEnabled(!wait);
        fullDictButton.setEnabled(!wait);
    }

    /* ============================================================================================================== */

    /* METHODS FOR SERVER COMMUNICATION */
    /* -------------------------------- */

    /**
     * Method called when wanting to send a request to the server and receive the response.
     *
     * @param request Request to be sent to the server.
     */
    public void sendRequest(JSONObject request) {

        try (Socket socket = new Socket(ip, port);) {
            JSONParser parser = new JSONParser();
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // Send hello to Server
            output.writeUTF("I have a request!");
            output.flush();

            // Send message to Server
            System.out.println("Sending to server: " + request.toJSONString());
            output.writeUTF(request.toJSONString());
            output.flush();

            // Process the response received from server
            JSONObject response = (JSONObject) parser.parse(input.readUTF());
            System.out.println("Received from server: "+response.toJSONString());

            parseResponse((String) request.get("request"), response);

            // release resources
            output.close();
            input.close();
            System.out.println("Finished with request\n");
        }
        
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to parse the JSON response received from the server.
     * 
     * @param request Request type that was sent to the server.
     * @param response Response received from the server.
     */
    private void parseResponse(String request, JSONObject response) {
        String[] meanings = null;
        String[] words;
        String[] keys;
        String[] valuesList;

        // Get the status from the response
        String status = (String) response.get("status");

        switch (request) {

            case "start":
                words = parseList("words", response);
                dictModel = new ArrayList<String>(Arrays.asList(words));
                searchBox.setModel(new DefaultComboBoxModel(dictModel.toArray()));
                break;

            case "full":
                if (status.equals("updated")) {
                    version = Math.toIntExact((Long) response.get("version"));

                    String keyString = (String) response.get("keys");
                    if (!keyString.equals("[]")) {
                        keys = keyString.substring(1, keyString.length() - 1).split(", ");
                    }
                    else {
                        keys = new String[0];
                    }

                    String valueString = (String) response.get("values");

                    if (!keyString.equals("[]")) {
                        valuesList = valueString.substring(1, valueString.length() - 2).split("], ");
                    }
                    else {
                        valuesList = new String[0];
                    }
                    int i = 0;
                    dict = new HashMap<String, String[]>();
                    for (String s: valuesList) {
                        String[] meaningList = s.substring(1).split(", ");
                        dict.put(keys[i], meaningList);
                        i++;
                    }
                }
                showDict();
                break;

            case "add":
                if (status.equals("failure")) {
                    meanings = parseList("meanings", response);
                }
                break;

            case "search":
                if (status.equals("success")) {
                    meanings = parseList("meanings", response);
                }
                break;

            case "update":
            case "remove":
                break;

            default:
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        // Show the response unless we're just starting the client or if the full dict was already shown
        if (!request.equals("full") && !request.equals("start")) {
            new ResponseWindow((String) response.get("message"), meanings);
        }

    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Helper function to parse a JSON string to get a list.
     * 
     * @param key Key of the JSON string to get the list from.
     * @param response JSON object to be parsed.
     * @return List of words extract from the key in the response.
     */
    private String[] parseList(String key, JSONObject response) {
        String s = (String) response.get(key);
        String[] sList = s.substring(1, s.length() - 1).split(", ");
        return sList;
    }

    /* ============================================================================================================== */
    /* METHODS FOR EXTRA FEATURES */
    /* -------------------------- */
    // Auto-completion code inspired from http://www.orbital-computer.de/JComboBox/

    /**
     * Method called when showing the full dictionary.
     */
    private void showDict() {
        // Empty dictionary
        if (dict.size() == 0) {
            new ResponseWindow("Dictionary is empty", null);
        }
        else{
            // Turn hashmap of dictionary into array words and meanings
            String[] entries = new String[dict.size()];
            int i = 0;
            for (Map.Entry<String, String[]> entry : dict.entrySet()) {
                String s = entry.getKey() + ": " + Arrays.toString(entry.getValue());

                entries[i] = s;
                i++;
            }
            new ResponseWindow("Version " + version, entries);
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method called when first starting the client to populate the dictionary model in the search box.
     */
    private void startClient() {
        JSONObject request = new JSONObject();
        request.put("request", "start");
        sendRequest(request);
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Helper function to find all the words in the current search box model that match a pattern.
     *
     * @param pattern Pattern to match the words against.
     * @return First item that matches or null if none.
     */
    private Object lookupItem(String pattern) {
        Object selectedItem = searchBox.getSelectedItem();

        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        }
        // iterate over all items
        else {
            int n = searchBox.getItemCount();
            for (int i = 0; i < n; i++) {
                Object currentItem = searchBox.getItemAt(i);
                // current item starts with the pattern?
                if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Helper function to check if a word starts with a certain string while allowing for case insensitivity.
     *
     * @param word Word to check.
     * @param pattern Pattern to check.
     * @return true if the word starts with the pattern.
     */
    private boolean startsWithIgnoreCase(String word, String pattern) {
        return word.toUpperCase().startsWith(pattern.toUpperCase());
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Autocompletion helper function to complete the rest of the word.
     *
     * @param offs Number of characters to skip before inserting the string
     * @param str String to be inserted
     * @param a Attribute set
     * @throws BadLocationException
     */
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        JTextComponent editor = (JTextComponent) searchBox.getEditor().getEditorComponent();
        // return immediately when selecting an item
        if (selecting) {
            return;
        }
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));
        String word = str;
        if (item != null && suggest && !selecting) {

            // setText
            selecting = true;
            searchBox.setSelectedItem(item);
            selecting = false;

            word = editor.getText(0, getLength());
            word += item.toString().substring(word.length());

            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, word, a);
        }

        // highlight the completed part
        editor.setSelectionStart(offs+str.length());
        editor.setSelectionEnd(getLength());
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Autocompletion helper function to clear the search box text field after a certain offset.
     *
     * @param offs Number of characters that we don't want to remove.
     * @param len Length of the entire word.
     * @throws BadLocationException
     */
    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        super.remove(offs, len);
    }

    /* ============================================================================================================== */
}

/**
 * JSON responses will be in the following form:
 *
 * Client: {request: "start"}
 * Server: {words: [...]}
 *
 * Client: {request: "full", version: xx}
 * Server: {status: "up-to-date"}
 *         {status: "updated", version: xx, keys: [...], values: [[...], ...]}
 *
 * Client: {request: "add", word: "WORD", meaning: [...]}
 * Server: {status: "success", message: "WORD added successfully"}
 *         {status: "failure", message, "WORD found with the following meanings: ", meaning: [...]}
 *         {status: "error", message: "ERROR MESSAGE"}
 *
 * Client: {request: "search", word: "WORD"}
 * Server: {status: "failure", message: "WORD does not exist"}
 *         {status: "success", message, "WORD found with the following meanings: ", meaning: [...]}
 *         {status: "error", message: "ERROR MESSAGE"}
 *
 * Client: {request: "remove", word: "WORD"}
 * Server: {status: "failure", message: "WORD does not exist"}
 *         {status: "success", message, "WORD removed successfully"}
 *         {status: "error", message: "ERROR MESSAGE"}
 *
 * Client: {request: "update", word: "WORD", meaning: [...]}
 * Server: {status: "success", message: "WORD updated successfully"}
 *         {status: "failure", message, "WORD does not exist"}
 *         {status: "error", message: "ERROR MESSAGE"}
 *
 */
