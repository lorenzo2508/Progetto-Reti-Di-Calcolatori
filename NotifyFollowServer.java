
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public interface NotifyFollowServer extends Remote {
    //metodo che mi sottoscrive alle notifiche riguardo i follower
    public void registerClientForCallBack (String username, NotifyFollowClient client)throws RemoteException;
    public  void removeClientFromCallBack(String username, NotifyFollowClient client) throws  RemoteException;
    public ConcurrentHashMap<String, NotifyFollowClient> getMappa() throws RemoteException;
}
