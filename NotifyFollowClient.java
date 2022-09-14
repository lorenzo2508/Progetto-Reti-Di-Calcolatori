

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public interface NotifyFollowClient extends Remote {
    //Notifica una sottoscrizione al servizio di notifica riguardo i follower e mi aggiunge in locale al client
    //nella sua struttura dati, l'utente che si è aggiunto alla lista dei follower;
    void setFollower (String username) throws RemoteException;
    void removeFollower (String username) throws RemoteException;
    public void sendList(ArrayList<String> listOfFollower) throws RemoteException;
    public LinkedBlockingQueue<String> getFollower() throws RemoteException;



}
