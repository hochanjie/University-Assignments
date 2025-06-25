/**
 * @Title: COMP90015 Distributed Systems 2021 Sem 1 Assignment 1
 * @Description: Multithreaded dictionary server
 * @author Chan Jie Ho
 * @studentID 961948
 * @email chanjieh@student.unimelb.edu.au
 * @date 19/4/2021
 */

package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.net.ServerSocketFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A class to handle all server-client communication and the processing of the request.
 */

public class DictServer {

    /* ============================================================================================================== */
    /* INSTANCE VARIABLES */
    /* ------------------ */

    private static int version = 0;
    private static int port = 1999;
    private static String DICT_FILE = "dict.txt";
    private static HashMap<String, String[]> dict =  new HashMap<String, String[]>();

    /* ============================================================================================================== */
    /* MAIN & FILE METHODS */
    /* ------------------- */

    /**
     * Main function that launches the server.
     *
     * @param args
     */
    public static void main(String[] args) {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        // Read in the initial dictionary file
        readDict();

        // Wait for requests
        try (ServerSocket server = factory.createServerSocket(port)) {
            System.out.println("Waiting for requests...");

            while (true) {
                Socket client = server.accept();
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
            return;

        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to read the initial dictionary file.
     */
    private static void readDict() {
        File f = new File(DICT_FILE);
        try{
            if(!f.exists()) {
                dict = new HashMap<String,String[]>();
                f.createNewFile();
            }
            dict = new HashMap<String,String[]>();
            BufferedReader br = null;

            try{
                //create file object
                br = new BufferedReader( new FileReader(f) );

                String line = null;

                //read file line by line
                while ( (line = br.readLine()) != null ){

                    String[] entry = line.split(": ");
                    String word = entry[0];
                    String meaningList = entry[1].trim();
                    String[] meanings = meaningList.substring(1,meaningList.length()-1).split(", ");
                    dict.put(word, meanings);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(br != null){
                    try {
                        br.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    };
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to update the dictionary file.
     */
    private static void updateDict(){
        try {
            File f = new File(DICT_FILE);
            if(!f.exists()) {
                f.createNewFile();
            }
            BufferedWriter bf = null;
            try {
                // create new BufferedWriter for the output file
                bf = new BufferedWriter(new FileWriter(f));
                // iterate map entries
                for (Map.Entry<String, String[]> entry : dict.entrySet()) {
                    // put key and value separated by a colon
                    bf.write(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
                    // new line
                    bf.newLine();
                }
                bf.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    // always close the writer
                    bf.close();
                }
                catch (Exception e) {
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ============================================================================================================== */
    /* METHODS FOR CLIENT COMMUNICATION */
    /* -------------------------------- */

    /**
     * Method to get the json request from the client input stream.
     *
     * @param client Client socket that we're getting the data from.
     */
    private static void serveClient(Socket client) {

        try (Socket clientSocket = client) {
            JSONParser parser = new JSONParser();
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("CLIENT: " + input.readUTF());

            // Receive more data..
            while(true){
                if(input.available() > 0){
                    // Attempt to convert read data to JSON
                    JSONObject request = (JSONObject) parser.parse(input.readUTF());
                    JSONObject response = parseRequest(request);
                    System.out.println("SERVER: " + response);
                    output.writeUTF(response.toJSONString());
                    System.out.println("Finished with response\n");
                }
            }
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to parse the JSON request received from the client.
     *
     * @param request Request received from the client.
     * @return Response to be sent back to the client.
     */
    private synchronized static JSONObject parseRequest(JSONObject request) {
        String word;
        String meaningString;
        String[] meanings;

        String comm = (String) request.get("request");
        System.out.println("REQUEST RECEIVED: " + comm);

        switch (comm) {
            case "start":
                return startClient();

            case "full":
                int v = Math.toIntExact((Long) request.get("version"));
                System.out.println("VERSION RECEIVED: " + v);

                return fullDictionary(v);

            case "add":
                word = (String) request.get("word");
                meaningString = (String) request.get("meanings");
                meanings = meaningString.substring(1, meaningString.length() - 1).split(", ");

                System.out.println("WORD RECEIVED: " + word);
                System.out.println("MEANINGS RECEIVED:" + meanings);

                return addWord(word, meanings);

            case "search":
                word = (String) request.get("word");
                System.out.println("WORD RECEIVED: " + word);

                return searchWord(word);

            case "remove":
                word = (String) request.get("word");
                System.out.println("WORD RECEIVED: " + word);

                return removeWord(word);

            case "update":
                word = (String) request.get("word");
                meaningString = (String) request.get("meanings");
                meanings = meaningString.substring(1, meaningString.length() - 1).split(", ");

                System.out.println("WORD RECEIVED: " + word);
                System.out.println("MEANINGS RECEIVED: " + meanings);

                return updateWord(word, meanings);

            default:
                try {
                    throw new Exception();
                }
                catch (Exception e) {
                    JSONObject response = new JSONObject();
                    response.put("status", "error");
                    response.put("message", "Unknown request. Please try again later.");
                    return response;
                }

        }
    }

    /* ============================================================================================================== */
    /* METHODS FOR REQUEST RESPONDING */
    /* ------------------------------ */

    /**
     * Method to respond to a "start" request.
     *
     * @return Response to be sent back to the client.
     */
    private static JSONObject startClient() {
        JSONObject response = new JSONObject();
        String[] words = dict.keySet().toArray(new String[0]);

        // Get first 10 words
        if (words.length > 10) {
            String[] lessWords = new String[10];
            for (int i = 0; i < 10; i++) {
                lessWords[i] = words[i];
            }
            response.put("words", Arrays.toString(lessWords));
        }
        else {
            response.put("words", Arrays.toString(words));
        }
        return response;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to respond to a "full" request.
     *
     * @param v Version number that the client has in cache.
     * @return Response to be sent back to the client.
     */
    private static JSONObject fullDictionary(int v) {
        JSONObject response = new JSONObject();
        if (v == version) {
            System.out.println("Client's version is up-to-date");
            response.put("status", "up-to-date");
        }
        else {
            System.out.println("Client's dictionary is outdated. Sending updated dictionary.");
            response.put("status", "updated");

            response.put("version", version);
            String[] keys = dict.keySet().toArray(new String[0]);
            response.put("keys", Arrays.toString(keys));

            String[] values = new String[keys.length];
            int i = 0;
            for (String[] val : dict.values()) {
                values[i] = Arrays.toString(val);
                i++;
            }
            response.put("values", Arrays.toString(values));
        }
        return response;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to respond to an "add" request.
     *
     * @param word Word to be added.
     * @param meanings Meaning(s) of the word.
     * @return Response to be sent back to the client.
     */
    private static JSONObject addWord(String word, String[] meanings) {
        JSONObject response = new JSONObject();
        JSONObject searchResponse = searchWord(word);
        String searchStatus = (String) searchResponse.get("status");

        if (searchStatus.equals("success")) {
            response.put("status", "failure");

            String message = word + " already exists with the following meanings:";
            response.put("message", message);

            response.put("meanings", searchResponse.get("meanings"));
        }
        else if (searchStatus.equals("failure")) {
            dict.put(word, meanings);
            response.put("status", "success");

            String message = word + " added successfully";
            response.put("message", message);

            System.out.println(word + " has been added to the dictionary with: " + meanings);
            version++;
            updateDict();
        }
        else {
            return searchResponse;
        }

        return response;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to respond to a "search" request.
     *
     * @param word Word to be searched for.
     * @return Response to be sent back to the client.
     */
    private static JSONObject searchWord(String word) {
        JSONObject response = new JSONObject();

        if (word != null) {
            System.out.println("Searching for: " + word);
            String[] meanings = dict.get(word);

            if (meanings != null) {
                System.out.println("Found! Meanings: " + Arrays.toString(meanings));
                response.put("status", "success");

                String message = word + " found with the following meanings:";
                response.put("message", message);
                response.put("meanings", Arrays.toString(meanings));
            }

            else {
                System.out.println("Not found!");
                response.put("status", "failure");

                String message = word + " does not exist";
                response.put("message", message);
            }
        }
        else {
            System.out.println("Word submitted was null");
            response.put("status", "error");

            String message = "Word submitted was null. Please try again.";
            response.put("message", message);
        }

        return response;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to respond to a "remove" request.
     *
     * @param word Word to be removed.
     * @return Response to be sent back to the client.
     */
    private static JSONObject removeWord(String word) {
        JSONObject response = new JSONObject();
        JSONObject searchResponse = searchWord(word);
        String searchStatus = (String) searchResponse.get("status");

        if (searchStatus.equals("success")) {
            dict.remove(word);
            response.put("status", "success");

            String message = word + " removed successfully";
            response.put("message", message);

            System.out.println(word + " has been removed from the dictionary");
            version++;
            updateDict();
        }

        else {
            return searchResponse;
        }

        return response;
    }

    /* -------------------------------------------------------------------------------------------------------------- */
    /**
     * Method to respond to an "update" request.
     * @param word Word to be updated.
     * @param meanings Meaning(s) of the word.
     * @return Response to be sent back to the client.
     */

    private static JSONObject updateWord(String word, String[] meanings) {
        JSONObject response = new JSONObject();
        JSONObject searchResponse = searchWord(word);
        String searchStatus = (String) searchResponse.get("status");

        if (searchStatus.equals("success")) {
            removeWord(word);
            addWord(word, meanings);
            response.put("status", "success");

            String message = word + " updated successfully";
            response.put("message", message);

            version++;
            updateDict();
        }
        else {
            return searchResponse;
        }

        return response;
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