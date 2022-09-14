

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public interface Registration extends Remote {
    String registrationMethod(String userName, String Password, ArrayList<String> listOfTags) throws RemoteException;
}
