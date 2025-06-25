package remotes;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChatListener extends Remote, Serializable {

    void broadcastMessage(String message) throws RemoteException;

}
