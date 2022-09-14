
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server {
    public static boolean stop = false;
    public ConcurrentHashMap<User, Blog>usersProfile;
    private ConcurrentHashMap<User, Post>posts;
    private ConcurrentHashMap <Pair<User, Blog>, Home> socialNetwork;
    private LinkedBlockingQueue <Pair<User, String>> utentiLoggati;
    private ConcurrentHashMap<String, Wallet> wallet;
    public static RMIFollowServer rmiFollowing = null;
    public  static Registry registry = null;
    private String indirizzoMulticast;
    private int portaMulticast  ;
    private int portaRMIForFollowingService;
    private int portaRMI;
    private int portaTCP;
    private static int timeToWait = 0;


    public Server (){
        this.usersProfile = new ConcurrentHashMap<>();
        this.socialNetwork = new ConcurrentHashMap<>();
        this.utentiLoggati = new LinkedBlockingQueue<>();
        this.posts = new ConcurrentHashMap<>();
        this.wallet = new ConcurrentHashMap<>();
    }


    public static void main(String[] args)  {
        Server server = new Server();

    //==================================================================================================================
    //lettura file di configurazione
        ArrayList<String> configFile = new ArrayList<>();
        int indexOfDataForConfig = 0;
        try {
            File file = new File("ConfigFile" + File.separator + "ServerConfig.txt");
            Scanner reader = new Scanner(file);
            while (reader.hasNext()) {
                if(reader.nextLine().startsWith("#")){
                    continue;
                }
                String data = reader.next();
                if(data.equals("#")){
                    continue;
                }
                indexOfDataForConfig = data.indexOf("=");
                data = data.substring(indexOfDataForConfig+1);
                configFile.add(data);
            }
            reader.close();
            server.portaTCP = Integer.parseInt(configFile.remove(0));
            server.indirizzoMulticast = configFile.remove(0);
            server.portaMulticast = Integer.parseInt(configFile.remove(0));
            server.portaRMI = Integer.parseInt(configFile.remove(0));
            server.portaRMIForFollowingService = Integer.parseInt(configFile.remove(0));
            timeToWait = Integer.parseInt(configFile.remove(0));
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        //  Servizio di RMI per following

        try {
            rmiFollowing = new RMIFollowServer();
            registry = LocateRegistry.createRegistry(server.portaRMIForFollowingService);
            registry.bind("follow", rmiFollowing);
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }




    //==================================================================================================================




        //==============================================================================================================
        //per ripristinare con Gson lo stato del server
        //leggo eventualmente i file su disco per riattivare lo stato del server
        File userProfileFile = new File("jsonFile" + File.separator +"userProfile.json");
        File postsFile = new File("jsonFile" + File.separator +"post.json");
        File walletFile = new File("jsonFile" + File.separator +"wallet.json");
        Type type = null;
        BufferedReader bufferedReader = null;
        ArrayList<User> listaUtentiDaRipristinare = new ArrayList<>();
        ArrayList<Post> listaPostDaRipristinare = new ArrayList<>();
        if(userProfileFile.exists() || walletFile.exists() || postsFile.exists()) {
            //USERPROFILE
            User[] users = new User[0];
            try {
                Gson gson = new Gson();
                users = gson.fromJson(new FileReader("jsonFile"+ File.separator + "userProfile.json"), User[].class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < users.length; i++) {
                listaUtentiDaRipristinare.add(users[i]);

            }

            //POSTS
            if (postsFile.exists() && !(postsFile.length() == 0)) {
                try {
                    JsonReader fileReader = new JsonReader(new FileReader("jsonFile" + File.separator +"post.json"));
                    fileReader.beginArray();

                    while (fileReader.hasNext()) {
                        Gson gson = new Gson();
                        Post post = gson.fromJson(fileReader, Post.class);
                        listaPostDaRipristinare.add(post);

                    }
                    fileReader.endArray();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            //RIPRISTINO LE MAPPE
            for (User entry : listaUtentiDaRipristinare) {
                User user = entry;
                Blog blog = new Blog();
                for (Post post : listaPostDaRipristinare) {
                    if (post.getCreator().equals(entry.getUserName())) {
                        if(!blog.getListaDeiPostSulBlog().contains(post)) {
                            blog.getListaDeiPostSulBlog().add(post);
                            user.getListPost().add(post);
                            server.posts.put(user, post);
                        }
                    }
                }
                server.usersProfile.put(user, blog);
            }


            for (Map.Entry<User, Blog> entry: server.usersProfile.entrySet()){
                Pair<User, Blog> coppiaUtenteBlog = new Pair<>(entry.getKey(), entry.getValue());
                Home home = new Home();
                server.socialNetwork.put(coppiaUtenteBlog, home);
            }

            for (Map.Entry<User, Blog> entry : server.usersProfile.entrySet()){
                for (String name : entry.getKey().getFollower()){
                    for (Map.Entry<Pair<User, Blog>, Home> entry1 : server.socialNetwork.entrySet())
                        if(entry1.getKey().getFirst().getUserName().equals(name)){
                            //per ogni utente follower di "entry" prendo la lista dei post di "entry" e la metto nella home dei follower
                            entry1.getValue().addListOfPost(entry.getKey().getListPost());
                        }
                }
            }

            //WALLET
            if (walletFile.exists()) {

                try {
                    bufferedReader = new BufferedReader(new FileReader("jsonFile" + File.separator +"wallet.json"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Gson gson = new Gson();
                type = new TypeToken<ConcurrentHashMap<String, Wallet>>() {
                }.getType();
                assert bufferedReader != null;
                server.wallet = gson.fromJson(bufferedReader, type);
            }
        }

        //==============================================================================================================

        //thread per la registrazione
        Thread thread = new Thread(new WorkerServerRMI(server.usersProfile, server.socialNetwork, server.wallet, server.portaRMI));
        //threadPool per la registrazione
        ExecutorService threadpool = Executors.newCachedThreadPool();
        threadpool.execute(thread);
        //thread per il calcolo delle ricompense
        RevenueWorker revenueWorker = new RevenueWorker(timeToWait, server.posts, server.wallet, server.indirizzoMulticast, server.portaMulticast);
        Thread threadrevenueWorker = new Thread(revenueWorker);
        threadrevenueWorker.start();

        try {


            ServerSocket serverSocket = new ServerSocket(server.portaTCP);

            JsonThread json = new JsonThread(server.usersProfile, server.wallet);
            Thread jsonthread = new Thread(json);
            jsonthread.start();
            while (stop == false){
                //accetto connessioni TCP e avvio un thread per connessione
                Socket sockClient = serverSocket.accept();
                threadpool.execute(new WorkerServerTCP(server.usersProfile, server.posts,sockClient, server.utentiLoggati, rmiFollowing,server.socialNetwork, server.wallet, server.portaMulticast, server.indirizzoMulticast));

            }



            //termino il thread pool alla chiusura
            threadpool.shutdown();
            if(!threadpool.isTerminated()){
                threadpool.shutdownNow();
            }


        } catch (IOException e ) {
            e.printStackTrace();
        }



    }

}
