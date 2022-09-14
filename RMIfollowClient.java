

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class RMIfollowClient extends UnicastRemoteObject implements NotifyFollowClient {
    private LinkedBlockingQueue<String> linkedBlockingQueue;

    public RMIfollowClient() throws RemoteException {
        this.linkedBlockingQueue = new LinkedBlockingQueue<>();
    }
    @Override
    public void setFollower(String username) throws RemoteException {
        linkedBlockingQueue.add(username);
    }

    @Override
    public void removeFollower(String username) throws RemoteException {
        linkedBlockingQueue.remove(username);

    }

    public void sendList(ArrayList<String> listOfFollower){
        for (String entry :
                listOfFollower) {
            if(!linkedBlockingQueue.contains(entry)) {
                linkedBlockingQueue.add(entry);
            }
        }

    }

    public LinkedBlockingQueue<String> getFollower() {
        return linkedBlockingQueue;
    }
}
