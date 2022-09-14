



import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerServerRMI implements Runnable, Registration{
    //VARIABILI D'ISTANZA
    private ConcurrentHashMap<User, Blog> usersProfile;
    private ConcurrentHashMap <Pair<User, Blog>, Home> socialNetwork;

    private ConcurrentHashMap<String, Wallet>wallet;
    private int portaRMI;

    //COSTRUTTORE PER OGGETTO RMI
    public WorkerServerRMI(ConcurrentHashMap<User, Blog> usersProfile, ConcurrentHashMap<Pair<User, Blog>, Home> socialNetwork, ConcurrentHashMap<String, Wallet>wallet, int portaRMI) {
        this.usersProfile = usersProfile;
        this.socialNetwork = socialNetwork;
        this.wallet = wallet;
        this.portaRMI = portaRMI;


    }

    @Override
    public void run() {
        try {
            Registration stub = (Registration) UnicastRemoteObject.exportObject(this, 0 );
            LocateRegistry.createRegistry(portaRMI);
            Registry registry = LocateRegistry.getRegistry(portaRMI);
            registry.bind("REGISTRATION_SERVICE", stub);

        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }

    }


    @Override
    /*metodo remoto per la registrazione del nuovo utente
    * ritorna -"re-try" se il nome utente scelto è già in uso su un altro profilo
    * ritorna -"do-login" se la coppia (username, password) è già presente all'interno del social
    * ritorna -"ok" se la registrazione è andata a buon fine e l'utente è stato registrato con successo*/

    public String registrationMethod(String userName, String password, ArrayList<String> listOfTags) throws RemoteException {
        //se l'utente inserisce i tag in maiuscolo li converto in minuscolo in modo da rispettare la specifica richiesta
        ArrayList<String> listOfTagsLowerCase = new ArrayList<>();
        for (String s :
                listOfTags) {
            listOfTagsLowerCase.add(s.toLowerCase());
        }
        //istanzio nuovo utente con blog, home (feed), e la coppia (user, blog) che compone il social network
        User user = new User(userName, password,listOfTagsLowerCase);
        Blog blog = new Blog();
        Home home = new Home();
        Wallet portafogli = new Wallet() ;
        Pair<User, Blog> userBlogPair = new Pair<>(user, blog);



        if(usersProfile.isEmpty()){
            usersProfile.put(user, blog);

        }
        else {
            //controllo se nella hashmap è già in uso l'username con cui ci si vuole registrare.
            //questo perché voglio che l'username sia univoco
            for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
                if(entry.getKey().getUserName().equals(userName) ){
                    System.err.println("Username just in use");
                    return "re-try";
                }

            }

//            for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
//                if (!entry.getKey().getUserName().equals(userName) && entry.getKey().getPassword().equals(password)) {
//                    System.err.println("Password già in uso su altro profilo");
//                    return "re-try-pass";
//                }
//            }
            //controllo se l'utente esite già. Per identificare un utente utilizzo la coppia username e password
            for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
                if(entry.getKey().getUserName().equals(userName)&& entry.getKey().getPassword().equals(password)){
                    System.err.println("Utente già registrato, fare il login");
                    return "do-login";
                }

            }

        }

        //registro l'utente.
        usersProfile.put(user, blog );
        socialNetwork.put(userBlogPair, home);
        wallet.put(user.getUserName(), portafogli);

        return "ok";
    }
}
