
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class RMIFollowServer extends UnicastRemoteObject implements NotifyFollowServer {
    private ConcurrentHashMap<String, NotifyFollowClient> mappa;

    public RMIFollowServer() throws RemoteException {
        mappa = new ConcurrentHashMap<>();
    }

    @Override
    public void registerClientForCallBack(String username, NotifyFollowClient client) throws RemoteException {
        mappa.put(username, client);

    }

    public  void removeClientFromCallBack(String username, NotifyFollowClient client) throws  RemoteException{
        mappa.remove(username, client);
    }

    public ConcurrentHashMap<String, NotifyFollowClient> getMappa() {
        return mappa;
    }
}
