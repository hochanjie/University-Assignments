package server;
import util.inputParser;

public class Server {

    public static void main(String[] args) {
        try {
            String ip = args[0];
            String port = args[1];

            if (inputParser.isValidIP(ip) && inputParser.isValidPort(port)) {

                System.setProperty("java.rmi.server.hostname", ip);
                ServerFacade serverFacade = ServerFacade.getInstance();

                serverFacade.connectDatabase(ip, Integer.parseInt(port));
                serverFacade.runServer(ip, Integer.parseInt(port));
            }
        }
        catch (Exception e) {
            System.out.println("Server launch failed");
            System.exit(1);
        }
    }
}
