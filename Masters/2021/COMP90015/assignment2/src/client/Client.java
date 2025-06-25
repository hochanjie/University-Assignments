package client;

public class Client {

    public static void main(String[] args) {
        try {
            ClientFacade clientFacade = new ClientFacade();
            clientFacade.launchLobby();
        }
        catch(Exception e) {
            System.out.println("Failed to launch client.");
            System.exit(1);
        }
    }
}