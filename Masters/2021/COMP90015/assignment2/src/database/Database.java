package database;

import util.inputParser;

public class Database {

    public static void main(String[] args) {

        System.setProperty("java.security.policy", "file:./security.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String ip = args[0];
        String port = args[1];

        if (inputParser.isValidIP(ip) && inputParser.isValidPort(port)) {
            try {
                System.setProperty("java.rmi.server.hostname", ip);
                DatabaseServer databaseServer = DatabaseServer.getInstance();
                databaseServer.runDatabase(port);
            }
            catch (Exception e) {
                System.out.println("Failed to launch database.");
                System.exit(1);
            }
        }
        else {
            System.out.println("Invalid IP or port number. Please ensure inputs are in the right format.");
            System.exit(1);
        }
    }
}


