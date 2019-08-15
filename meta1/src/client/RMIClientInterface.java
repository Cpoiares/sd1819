package client;

import java.rmi.*;

public interface RMIClientInterface extends Remote {

    void givePermission() throws RemoteException;

    void print(String message) throws RemoteException;
}
