
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;



public class Client {
    public static boolean flagControlloLogin = false;
    private static RMIfollowClient rmIfollowClient ;
    private static NotifyFollowServer notifyFollowServer;
    private static long randIdClient;
    private static String userNameClient;
    private static InetAddress indirizzo;
    private static Socket sockClient;
    private static OutputStream outputStream;
    private static DataOutputStream dataOutputStream;
    private static InputStream inputStream;
    private static DataInputStream dataInputStream;
    private static ArrayList<String> comandiAccettati;
    private static LinkedBlockingQueue<String> followers;
    private static int portaTCP;
    private static int portaRMI;
    private static int portaRMIForFollowingService;
    private static String indirizzoMulticast;
    private static int portaMulticast;
    private static String indirizzoInetAddress;
    private static String hostRMI;




    public static void main(String[] args) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        //==============================================================================================================
        //lettura file di configurazione
        ArrayList<String> configFile = new ArrayList<>();
        int indexOfDataForConfig = 0;
        try {
            File file = new File("ConfigFile" + File.separator + "ClientConfig.txt");
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
            portaTCP = Integer.parseInt(configFile.remove(0));//deve essere: 5454
            portaRMI = Integer.parseInt(configFile.remove(0)); //deve essere: 5458
            portaRMIForFollowingService = Integer.parseInt(configFile.remove(0)); //deve essere 5896
            indirizzoInetAddress = configFile.remove(0); //deve essere localhost
            hostRMI = configFile.remove(0); //deve essere localhost
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //==============================================================================================================
        comandiAccettati = new ArrayList<>();
        comandiAccettati.add("register"); //comando per registrare
        comandiAccettati.add("login"); //comando per login
        comandiAccettati.add("logout"); //comando per logout
        comandiAccettati.add("list users");//comando per la lista degli utenti con tag comune
        comandiAccettati.add("list followers"); //comando per mostrare la lista dei follower
        comandiAccettati.add("list following"); //comando per mostrare la lista dei following
        comandiAccettati.add("follow"); //comando per seguire utente
        comandiAccettati.add("unfollow");//comando per smettere di seguire utente
        comandiAccettati.add("blog");//comando per vedere il profilo di un utente
        comandiAccettati.add("post");//comando per postare
        comandiAccettati.add("show feed");//comando per mostrare la home (il feed)
        comandiAccettati.add("delete");//comando per eliminare un post
        comandiAccettati.add("rewin");//comando per condividere un post
        comandiAccettati.add("rate");//comando per votare un post
        comandiAccettati.add("comment");//comando per commentare un post
        comandiAccettati.add("wallet");//comando per vedere il wallet
        comandiAccettati.add("wallet btc");//comando per recuperare il valore del proprio wallet in bitcoin
        comandiAccettati.add("show post");//comando per mostrare un post dato il suo id

        Random rand = new Random();
        randIdClient = rand.nextLong();

        //istanzio un oggetto della classe hashingPassword che mi serve per "criptare la password"
        //che poi passerò al server.
        Scanner scannerTastiera = new Scanner(System.in);

        while (true) {
            //prendo l'input da tastiera
            String inputDaTastiera = null;
            //questo try catch è per gestrire la terminazione alla pressione del ctrl + c (altrimenti eccezione sollevata dalla classe scanner)
            try {
                 inputDaTastiera = scannerTastiera.nextLine();
            }
            catch (Exception e ){
                System.out.println("chiusura");
                break;
            }

            //creo un array che conterrà l'input immesso dall'utente per la registrazione, quindi i suoi dati di registrazione
            ArrayList<String> datiUtente = new ArrayList<>();
            ArrayList<String> datiUtenteForPost = new ArrayList<>(); //contiene il comando "createPost", titolo e contenuto del post
            ArrayList<String> datiUtenteForComment = new ArrayList<>();
            String comandoInserito; // prima parte del comando

            //questo if mi serve per differenizare le tokenizzazioni, perché nei casi in cui non devo creare un post o commentarlo
            //devo tockenizzare sugli spazi, mentre negli altri casi devo tockenizzare sulle virgolette
            if(inputDaTastiera.startsWith("post")){
                datiUtenteForPost = tockenizerForPost(inputDaTastiera);
                comandoInserito = datiUtenteForPost.get(0);
            }
            else if(inputDaTastiera.startsWith("comment")){
                datiUtenteForComment = tockenizerForComment(inputDaTastiera);
                comandoInserito = datiUtenteForComment.get(0);
            }
            else {
                //tockenizzo la stringa immassa dall'utente e la metto nell'array "datiUtente"
                datiUtente = tockenizzatore(inputDaTastiera);
                //prendo dall'array la prima parola che corrisponde al comando immesso, quindi al servizio che vuole avere
                comandoInserito = datiUtente.remove(0);
                if(!comandiAccettati.contains(comandoInserito)){
                    System.out.println("il comando inserito non e' accettato. I comandi accettati sono:  ");
                    System.out.println(comandiAccettati);
                    System.out.println("Riprova usando uno di questi comandi");
                    continue;
                }
            }

            /*==========================================================================================================
            *                                           REGISTRAZIONE                                                   */


            //se il comando è uguale a "register" o "Register" si avvia la procedura di registrazione del nuovo utente

            if (comandoInserito.equalsIgnoreCase("register")) {
                String risultatoRegistrazione = registrazione(datiUtente);
                if(risultatoRegistrazione.equals("Already registered user")){
                    System.out.println("Utente gia' registrato, fare il login");
                    continue;
                }
                if(risultatoRegistrazione.equals("username not available")){
                    System.out.println("il nome utente scelto e' gia' in uso, provare un altro nome:");
                    continue;
                }

                if(risultatoRegistrazione.equals("ok")){
                    Registry registry = LocateRegistry.getRegistry(hostRMI,portaRMIForFollowingService);
                    notifyFollowServer = (NotifyFollowServer) registry.lookup("follow");
                    rmIfollowClient = new RMIfollowClient(); //fai la getFollower, oggetto remoto questo (remoto per server)
                    notifyFollowServer.registerClientForCallBack(userNameClient, rmIfollowClient);
                    followers = rmIfollowClient.getFollower();
                    System.out.println("ok");
                    continue;
                }
                if(risultatoRegistrazione.equals("tooMuchTags")){
                    System.out.println("hai inserito troppi tag. Il limite e' di 5. Riprova la registrazione");
                    continue;
                }

            }

            /*==========================================================================================================
            *                                           LOGIN                                                           */


            //In caso di login;
            if(comandoInserito.equalsIgnoreCase("login") && !flagControlloLogin){
                try {
                    indirizzo = InetAddress.getByName(indirizzoInetAddress);
                    sockClient = new Socket(indirizzo, portaTCP);
                    outputStream = sockClient.getOutputStream();
                    // creo il dataOutputStream sull'outputStream per mandare le stringhe (i dati) attraverso esso
                    dataOutputStream = new DataOutputStream(outputStream);
                    inputStream = sockClient.getInputStream();
                    dataInputStream = new DataInputStream(inputStream);
                    //chiamo il metodo per fare il login
                    //e metto il suo risultato che sarà una stringa in "risultatoLogin"
                    String risultatoLogin = login(datiUtente, randIdClient, dataOutputStream, dataInputStream);

                    if(risultatoLogin.startsWith("ok")){
                        System.out.println(risultatoLogin + " " + "logged in");
                        Registry registry = LocateRegistry.getRegistry(hostRMI,portaRMIForFollowingService);
                        notifyFollowServer = (NotifyFollowServer) registry.lookup("follow");
                        rmIfollowClient = new RMIfollowClient(); //fai la getFollower, oggetto remoto questo (remoto per server)
                        notifyFollowServer.registerClientForCallBack(userNameClient, rmIfollowClient);
                        followers = rmIfollowClient.getFollower();
                        dataOutputStream.writeUTF("ok");
                        //avvio il thread che si registra sul multicast e ascolta per le notifiche inviate via server
                        ListenerForNotification listenerForNotification = new ListenerForNotification(indirizzoMulticast, portaMulticast, flagControlloLogin);
                        Thread listener = new Thread(listenerForNotification);
                        listener.start();
                        continue;

                    }
                    if(risultatoLogin.equals("ErrPass")){
                        System.out.println("password errata,  riprovare");
                        continue;
                    }
                    if(risultatoLogin.equals("ErrUsername")){
                        System.out.println("username errato, riprovare");
                        continue;
                    }
                    if(risultatoLogin.equals("UserNotReg")){
                        System.out.println("utente non registrato, fare la registrazione");
                        continue;
                    }
                    if(risultatoLogin.equals("loginErr")){
                        System.err.println("Errore nella procedura di login");
                        System.exit(1);

                    }
                    if(risultatoLogin.equals("passAbsent")){
                        System.err.println("manca la password");
                        continue;

                    }




                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if(comandoInserito.equalsIgnoreCase("login") && flagControlloLogin) {
                System.err.println("C'e' un utente gia' collegato, deve essere prima scollegato, effettuare 'logout' ");
                continue;
            }



            /*==========================================================================================================
            *                                           LOGOUT                                                          */
            if(comandoInserito.equalsIgnoreCase("logout") && flagControlloLogin){
                try {
                    String risultatoLogout = logout(datiUtente, randIdClient, dataOutputStream, dataInputStream);
                    if(risultatoLogout.startsWith("ok")){
                        System.out.println(risultatoLogout + " logout effettuato");
                        continue;
                    }
                    if(risultatoLogout.equals("UserNotFound")){
                        System.out.println("nome utente che is vuole disconnettere errato, riprovare");
                        continue;
                    }
                    if(risultatoLogout.equals("logoutErr")){
                        System.err.println("errore nella fase di logout");
                        System.exit(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            /*==========================================================================================================
             *                                           LIST USER                                                          */
            if(comandoInserito.equalsIgnoreCase("list users") && flagControlloLogin){
                try {
                    listUsers(dataOutputStream, dataInputStream);
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*==========================================================================================================
             *                                             CREA POST                                                          */
            if(comandoInserito.equalsIgnoreCase("post")){
                try {
                    createPost(dataOutputStream, dataInputStream, datiUtenteForPost);
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            /*==========================================================================================================
             *                                           FOLLOW USER                                                          */

            if(comandoInserito.equalsIgnoreCase("follow")){
                try {
                    followUser( dataOutputStream,  dataInputStream, comandoInserito, datiUtente);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;

            }
            /*==========================================================================================================
             *                                           FOLLOW USER                                                          */

            if(comandoInserito.equalsIgnoreCase("unfollow")){
                try {
                    unFollowUser( dataOutputStream,  dataInputStream, comandoInserito, datiUtente);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;

            }

            /*===========================================================================================================
                                                        LIST FOLLOWER */
            if(comandoInserito.equalsIgnoreCase("list followers")){
                listFollowers();
                continue;
            }

            /*===========================================================================================================
                                                        LIST FOLLOWING */
            if(comandoInserito.equalsIgnoreCase("list following")){
                try {
                    listFollowing(dataOutputStream, dataInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

             /*===========================================================================================================
                                                        VIEW BLOG */
            if(comandoInserito.equalsIgnoreCase("blog")){
                try {
                    viewBlog(dataOutputStream, dataInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

            /*===========================================================================================================
                                                        SHOW FEED */
            if(comandoInserito.equalsIgnoreCase("show feed")){
                try {
                    showFeed(dataOutputStream, dataInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

             /*===========================================================================================================
                                                        SHOW POST  */
            if(comandoInserito.equalsIgnoreCase("show post")){
                try {
                    showPost(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            /*===========================================================================================================
                                                        RATE POST */
            if(comandoInserito.equalsIgnoreCase("rate")){
                try {
                    ratePost(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

            /*===========================================================================================================
                                                        DELETE POST */
            if(comandoInserito.equalsIgnoreCase("delete")){
                try {
                    deletePost(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

            /*===========================================================================================================
                                                        ADD COMMENT */
            if(comandoInserito.equalsIgnoreCase("comment")){
                try {
                    addComment(dataOutputStream, dataInputStream, datiUtenteForComment);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

             /*===========================================================================================================
                                                        REWIN */
            if(comandoInserito.equalsIgnoreCase("rewin")){
                try {
                    rewinPost(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if(comandoInserito.equalsIgnoreCase("wallet")){
                try {
                    getWallet(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if(comandoInserito.equalsIgnoreCase("wallet btc")){
                try {
                    getWalletBitcoin(dataOutputStream, dataInputStream, datiUtente, comandoInserito);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }


        }

    }


    /*
    * metodo per fare la tockenizzazione delle stringe inserite dal client
    * ritorna un arrylist che contiene le stringhe tokenizzate sulla base degli spazi
    * in prima posizione dell'array si ha il comando
    * esempio: [register, <nome utente>, <password>, <interessi (tag)>]*/

    private static ArrayList<String> tockenizzatore (String tmp) {
        ArrayList<String> inputDatiUtente = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(tmp);

        ArrayList<String> comandiSingoli = new ArrayList<>();
        comandiSingoli.add("register");
        comandiSingoli.add("login");
        comandiSingoli.add("logout");
        comandiSingoli.add("follow");
        comandiSingoli.add("unfollow");
        comandiSingoli.add("delete");
        comandiSingoli.add("blog");
        comandiSingoli.add("rewin");
        comandiSingoli.add("rate");
        comandiSingoli.add("comment");
        // comandiSingoli.add("wallet");
        while (tokenizer.hasMoreTokens()) {
            inputDatiUtente.add(tokenizer.nextToken());
        }

        if (comandiSingoli.contains(inputDatiUtente.get(0))) {
            return inputDatiUtente;

        }
        if(inputDatiUtente.size() >= 2) {
            if (inputDatiUtente.get(1).equals("btc") && inputDatiUtente.get(0).equals("wallet")) {
                String primaParteComandoComposto = inputDatiUtente.remove(0);
                String secondaParteComandoComposto = inputDatiUtente.remove(0);
                String comandoCompleto = primaParteComandoComposto + " " + secondaParteComandoComposto;
                inputDatiUtente.add(0, comandoCompleto);

            } else if (inputDatiUtente.get(0).equals("wallet") && !inputDatiUtente.get(1).equals("btc")) {
                return inputDatiUtente;

            } else if (!comandiSingoli.contains(inputDatiUtente.get(0))) {

                String primaParteComandoComposto = inputDatiUtente.remove(0);
                String secondaParteComandoComposto = inputDatiUtente.remove(0);
                String comandoCompleto = primaParteComandoComposto + " " + secondaParteComandoComposto;
                inputDatiUtente.add(0, comandoCompleto);

            }
        }


        return inputDatiUtente;
    }



    /*Metodo che serve per estrarre dalla stringa inserita dall'utente le seguenti cose:
    * 1) comando "post" --> poi lo mando al server
    * 2) titolo del post --> poi lo mando al server
    * 3) corpo del post --> poi lo mando al server
    * ritorna un arrayList contenente 3 elementi che sono
    * le informazioni, di cui sopra. Le contiene nell'ordine in cui sono scritti in questo commento*/
    private static ArrayList<String> tockenizerForPost(String command){
        //estrapolo titolo e corpo del post dalla stringa inserita dall'utente. Separo sulle virgolette
        int first = command.indexOf("\"");
        int second = command.indexOf("\"", first+1);
        int third = command.indexOf("\"", second+1);
        int fourth = command.indexOf("\"", third+1);
        String title = command.substring(first+1, second);
        String post = command.substring(third+1, fourth);
        StringTokenizer tokenizer = new StringTokenizer(command);
        ArrayList<String> datiPost = new ArrayList<>();
        //prendo il comando "Post" e lo metto nell'array. Mi serve perché poi lo manderò al server
        String comando = tokenizer.nextToken() ;
        datiPost.add(comando); //aggiungo il comando all'array
        datiPost.add(title); // aggiungo il titolo
        datiPost.add(post); // aggiungo il corpo del post
        return datiPost; //ritorno l'array.
    }


    /*Metodo che serve per estrarre dalla stringa inserita dall'utente le seguenti cose:
     * 1) comando "comment" --> poi lo mando al server
     * 2) corpo del commento --> poi lo mando al server
     * ritorna un arrayList contenente 2 elementi che sono
     * le informazioni, di cui sopra. Le contiene nell'ordine in cui sono scritti in questo commento*/
    private static ArrayList<String> tockenizerForComment(String command){
        //estrapolo titolo e corpo del post dalla stringa inserita dall'utente. Separo sulle virgolette
        int first = command.indexOf("\"");
        int second = command.indexOf("\"", first+1);

        String commento = command.substring(first+1, second);
        StringTokenizer tokenizer = new StringTokenizer(command);
        ArrayList<String> datiCommento = new ArrayList<>();
        //prendo il comando "comment" e lo metto nell'array. Mi serve perché poi lo manderò al server
        String comando = tokenizer.nextToken() ;
        String idPost = tokenizer.nextToken();
        datiCommento.add(comando); //aggiungo il comando all'array
        datiCommento.add(idPost); // aggiungo idPost
        datiCommento.add(commento);//aggiungo il commento

        return datiCommento; //ritorno l'array.
    }


    /*METODO PER LA REGISTRAZIONE DI UN UTENTE SUL SOCIAL NETWORK
    * RITORNA:
    * OK -> SE LA REGISTRAZIONE VA A BUON FINE
    * username not available -> se il nome utete non è disponibile perché già usato da qualcuno
    * Already registered user -> se l'utente è già registrato
    * passNotUnique-> se la password non è univoca, quindi è gia usata da un altro user*/
    private static String registrazione(ArrayList<String> datiUtente) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        //prendo il nome utente dall'array
        String userName = datiUtente.remove(0);
        userNameClient = userName;
        //prendo la password dall'array, usando il metodo remove che proprio toglie l'elemento all'indice specificato
        //dall'array
        String userPassword = datiUtente.remove(0);
        if(datiUtente.size()>5){
            return "tooMuchTags";
        }


        //a questo punto l'arrayList datiUtente contiene i tag inseriti dall'user al momento della registrazione
        HashingPassword hashingPassword = new HashingPassword(); //creo un oggetto per fare l'hashing delle password
        //con RMI faccio la registrazione del nuovo utente invocando il metodo ".registrationMethod"
        Registration serverObj;
        Remote remoteObj;
        //prendo lo stub al registy dell'oggetto remoto
        Registry registry = LocateRegistry.getRegistry(portaRMI);
        //cerco la corrispondenza nello stab, per prendere il riferimento all'oggetto remoto
        remoteObj = registry.lookup("REGISTRATION_SERVICE");
        serverObj = (Registration) remoteObj;
        //nella stringa sottostante inserisco il risultato dell'invocazione del metodo remoto
        /*il metodo remoto ritorna -"re-try" se il nome utente scelto è già in uso su un altro profilo
         * ritorna -"do-login" se la coppia (username, password) è già presente all'interno del social
         * ritorna -"ok" se la registrazione è andata a buon fine e l'utente è stato registrato con successo
         * ritorna -"passNotUnique" se la password non è univoca*/
        String risultatoRegistrazione;
        risultatoRegistrazione = serverObj.registrationMethod(userName, hashingPassword.sha256(userPassword), datiUtente);
        if (risultatoRegistrazione.equals("re-try")) {
            return "username not available"; //username esiste già
        }
        else if(risultatoRegistrazione.equals("do-login")){
            return "Already registered user"; //utente già registrato

        }
        else if(risultatoRegistrazione.equals("re-try-pass")){
            return "passNotUnique"; //password già in uso su altro profilo
        }
        else {
            return "ok"; //registrazione andata a buon fine

        }

    }




    /*METODO PER IL LOG IN DI UN UTENTE NEL SOCIAL
    * RITORNA:
    * ok-> se il login va a buon fine
    * ErrPass-> se la password inserita è sbagliata
    * ErrUsername-> se lo user name inserito è sbagliato
    * UserNotReg-> se l'utente non è registrato sul social
    * loginErr-> se c'è stato un qualche errore durante il login
    * */
    private static String login (ArrayList<String> datiUtente, Long randIdClient, DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws NoSuchAlgorithmException, IOException {
        //prendo nome utente
        String userName = datiUtente.remove(0);
        userNameClient = userName;
        if(datiUtente.isEmpty()){
            return "passAbsent";
        }
        //prendo la password
        String password = datiUtente.remove(0);
        HashingPassword hashingPassword = new HashingPassword();
        String richiesta = "login " + userName + " "+ hashingPassword.sha256(password) + " " + randIdClient;



        // scrivo il messaggio che voglio mandare
        /*INVIO LA STRINGA AL SERVER MEDIANTE CONNESSIONE TCP*/
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        //leggo e controllo il messaggio di ritorno dal server
        String messageFromServer = dataInputStream.readUTF();
        if(messageFromServer.equals("ok")){
            flagControlloLogin = true;
            indirizzoMulticast = dataInputStream.readUTF();
            String port = dataInputStream.readUTF();
            portaMulticast = Integer.parseInt(port);
            return "ok" + ", " + userName;
        }
        if(messageFromServer.equals("ErrPass")){
            return "ErrPass";

        }
        if(messageFromServer.equals("ErrUsername")){
            return "ErrUsername";

        }
        if(messageFromServer.equals("UserNotReg")){
            return "UserNotReg";
        }
        return "loginErr";

    }



    /*METODO PER IL LOGOUT DI UN UTENTE DAL SOCIAL
     *chiude stream, socket e disesporta l'oggetto remoto di RMI
     * RITORNA:
     * ok-> se il logout va a buon fine
     * UserNotFound -> se l'utente non è loggato
     * logoutErr -> se c'è stato un quache errore durante il logout
     * */
    private static String logout(ArrayList<String> datiUtente, Long randIdClient, DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String richiesta = "logout " + userNameClient + " "+  " " + randIdClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        //leggo e controllo il messaggio di ritorno dal server
        String messageFromServer = dataInputStream.readUTF();
        if(messageFromServer.equals("ok")){
            flagControlloLogin = false;
            dataInputStream.close();
            dataOutputStream.close(); // chiudo gli stream e il socket
            sockClient.close();
            return "ok" + ", " + userNameClient;
        }

        if(messageFromServer.equals("UserNotFound")){
            return "UserNotFound";

        }


        return "logoutErr" ;
    }

    /*METODO PER MOSTRARE LA LISTA DI UTENTI, CON TAG UGUALI A QUELLI DELL'UTENTE RICHIEDENTE IL SERVIZIO,
     PRESENTI SUL SOCIAL
     * RITORNA:
     * stampa la lista degli utenti su standard output.
     * Esempio di output:
        * giovanni |-> tags..
        * francesco |-> tags..
        * ecc...
     * */
    private static void listUsers(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String richiesta = "list users " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        if (messaggioFromServer.equals("notUserLikeYou")){
            System.out.println("not user like you");
            return ;
        }
        if(messaggioFromServer.equals("ok")) {
            ArrayList<String> listaUtentiComuni = new ArrayList<>();
            while (!messaggioFromServer.equals("endOfList")) {
                messaggioFromServer = dataInputStream.readUTF();

                if (messaggioFromServer.equals("endOfList")) {
                    break;
                }
                listaUtentiComuni.add(messaggioFromServer);
            }

            System.out.println("|---Utenti--- |-> ---Tag---|");
            System.out.println("----------------------------------------------------");
            for (String entry : listaUtentiComuni) {
                System.out.println(entry);

            }
        }
        else
            System.err.println("Errore in fase di visualizzazione lista utenti");


    }


    /*METODO CHE INVIA AL SERVER I DATI DEL POST CHE VENGONO INSERITI DALL'UTENTE DA TASTIERA
    * SE TUTTO VA A BUON FINE STAMPA "Nuovo post creato (id= )" E A SEGUITO L'ID DEL POST MANDATO DAL SERVER*/
    private static void createPost(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtenteForPost) throws IOException {
       String comando = datiUtenteForPost.remove(0);
       dataOutputStream.writeUTF(comando);
       dataOutputStream.writeUTF(userNameClient);//invio il nome del creatore del post
       String titolo = datiUtenteForPost.remove(0);
       dataOutputStream.writeUTF(titolo); //invio titolo del post
       String corpo = datiUtenteForPost.remove(0);
       dataOutputStream.writeUTF(corpo); // invio il corpo del post


       //controllo che l'azione sia andata a buon fine e lo faccio sapere al client.
       String risultatoAzionePost = dataInputStream.readUTF();  //leggo il risultato dell'azione mandato dal server
       if(!risultatoAzionePost.equals("errPost")){
           System.out.println("Nuovo post creato (id= " + risultatoAzionePost + ")");
       }
       else if(risultatoAzionePost.equals("errPost")){
           System.err.println("errore durante posting. Riprovare");
       }

    }

    /*METODO CHE IMPLEMENTA IL FOLLOWING TRAMITE RMI*/
    private static void followUser(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String comandoInserito, ArrayList<String> datiUtente) throws IOException {
        //costruzione della richiesta da mandare al server

        String comando = comandoInserito;
        String userToFollow = datiUtente.remove(0);
        String richiesta = comando + " " + userToFollow;
        //inoltro della richiesta
        dataOutputStream.writeUTF(richiesta);
        dataOutputStream.writeUTF(userNameClient);
        //analisi della risposta del server
        String risposta = dataInputStream.readUTF();

        if(risposta.startsWith("ok")){
            System.out.println(risposta);
        }
        else if(risposta.startsWith("non")){
            System.out.println(risposta);
        }
        else if(risposta.equals("clientNull")){
            return;
        }
        else
            System.out.println("errore nel following, riprovare operazione");
    }

    /*METODO PER IMPLEMENTARE L'UNFOLLOWING TRAMITE RMI*/
    private static void unFollowUser(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String comandoInserito, ArrayList<String> datiUtente) throws IOException {
        //costruzione della richiesta da mandare al server
        System.out.println(datiUtente);
        String comando = comandoInserito;
        String userToUnFollow = datiUtente.remove(0);
        String richiesta = comando + " " + userToUnFollow;
        //inoltro della richiesta
        dataOutputStream.writeUTF(richiesta);
        dataOutputStream.writeUTF(userNameClient);
        //analisi della risposta del server
        String risposta = dataInputStream.readUTF();

        if(risposta.startsWith("ok")){
            System.out.println(risposta);
        }
        else if(risposta.equals("clientNull")){
            return;
        }
        else
            System.out.println("errore nell' unfollowing, riprovare operazione");
    }

    /*METODO CHE STAMPA LA LISTA DEI FOLLOWER */
    private static void listFollowers() {
        //prendo la lista invocando il metodo remoto e la salvo in una lista locale del client
        followers = rmIfollowClient.getFollower();
        if(followers.isEmpty()) {
            System.out.println("non ci sono persone che ti seguono");
            return;
        }
        for (String entry :
                followers) {
                //creo la stringa che il server manda al client. Unisco lo user name e la lista dei tag
                System.out.println(entry);

        }
    }
    /*METODO CHE STAMPA LA LISTA DEI FOLLOWING */
    private static void listFollowing (DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String richiesta = "list following " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        while (!messaggioFromServer.equals("endList")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endList"))
                System.out.println(messaggioFromServer);
        }


    }
    /*METODO CHE STAMPA IL CONTENUTO DEL BLOG (CHE SI TROVA SUL SERVER) DI UN UTENTE */
    private static void viewBlog(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String richiesta = "blog " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        while (!messaggioFromServer.equals("endList")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endList"))
                System.out.println(messaggioFromServer);
        }
    }
    /*MEDODO CHE STAMPA IL CONTEUTO DEL FEED (CHE SI TROVA SUL SERVER) DI UN UTENTE*/
    private static void showFeed(DataOutputStream dataOutputStream, DataInputStream dataInputStream ) throws IOException {
        String richiesta = "show feed " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        while (!messaggioFromServer.equals("endFeed")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endFeed"))
                System.out.println(messaggioFromServer);
        }
    }
    /*METODO CHE MOSTRA UN POST DATO UN ID IN INPUT*/
    private static void showPost(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito ) throws IOException {
        String idPost = datiUtente.remove(0);
        String richiesta = "show post" + " " + idPost + " " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        System.out.println("Commenti:");
        while (!messaggioFromServer.equals("endComment")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endComment"))
                System.out.println(messaggioFromServer);
        }
    }
    /*METODO CHE VALUTA UN POST DATO IN INPUT UN ID E UN VOTO
    * IL VOTO PUO' ASSUMERE VALORI +1 O -1*/
    private static void ratePost (DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito) throws IOException {
        String idPost = datiUtente.remove(0);
        String voto = datiUtente.remove(0);

        Integer vote = Integer.parseInt(voto); //faccio questa conversione per fare il controllo sottostante

        if(vote != 1 && vote != -1 ){
            System.out.println("Voto non valido, puoi votare positivamente inserendo: 1");
            System.out.println("Oppure puoi votare negativamente inserendo: -1");
            System.out.println("Gli altri valori non sono accettati");
            return;
        }
        String richiesta = "rate" + " " + idPost + " " + voto + " " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        if(messaggioFromServer.equals("tooMuchLikes")){
            System.out.println("non puoi mettere like piu' di una volta ad un post");
            return;
        }
        if(messaggioFromServer.equals("tooMuchDislike")){
            System.out.println("non puoi mettere dislike piu' di una volta ad un post");
            return;
        }
        System.out.println(messaggioFromServer);
    }


    /*METODO PER ELIMINARE UN POST DA FEED E BLOG DEL CREATORE DEL POST E DI TUTTE LE PERSONE CHE LO HANNO CONDIVISO*/
    private static void deletePost(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito) throws IOException {
        String idPost = datiUtente.remove(0);
        String richiesta = "delete" + " " + idPost + " " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        if(messaggioFromServer.equals("notYourPost")){
            System.out.println("non puoi cancellare un post che non e' tuo");
            return;
        }
        System.out.println(messaggioFromServer);
    }
    /*METODO PER AGGIUNGERE UN COMMENTO AL POST*/
    private static void addComment(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtenteForComment) throws IOException {
        String comando = datiUtenteForComment.remove(0);
        dataOutputStream.writeUTF(comando);
        String idPost = datiUtenteForComment.remove(0);
        dataOutputStream.writeUTF(idPost);
        String commento = datiUtenteForComment.remove(0);
        dataOutputStream.writeUTF(commento);
        dataOutputStream.writeUTF(userNameClient);
        String messaggioFromServer = dataInputStream.readUTF();
        if(messaggioFromServer.equals("feedEmpty")){
            System.out.println("IL TUO FEED E' VUOTO: non puoi commentare post che non si trovano sul tuo feed segui qualcuno :)");
            return;
        }
        if(messaggioFromServer.equals("notCommentYourPost")){
            System.out.println("non puoi commentare un tuo post");
            return;
        }
        if(messaggioFromServer.equals("postNotFoundOnFeed")){
            System.out.println("non puoi commentare post che non si trovano sul tuo feed");
            return;
        }
        System.out.println(messaggioFromServer);


    }
    /*METODO PER CONDIVIDERE UN POST*/
    private static void rewinPost(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito) throws IOException {
        String idPost = datiUtente.remove(0);
        String richiesta = "rewin" + " " + idPost + " " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        if(messaggioFromServer.equals("postNotOnYourFeed")){
            System.out.println("non puoi condividere un post di una persona che non segui");
            return;
        }
        System.out.println(messaggioFromServer);

    }

    private static void getWallet (DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito) throws IOException {
        String richiesta = "wallet " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        if(messaggioFromServer.equals("endWallet")){
            System.out.println("Il tuo portafogli e' vuoto");
            return;
        }
        while (!messaggioFromServer.equals("endWallet")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endWallet"))
                System.out.println(messaggioFromServer);
        }

    }

    private static void getWalletBitcoin(DataOutputStream dataOutputStream, DataInputStream dataInputStream, ArrayList<String> datiUtente, String comandoInserito) throws IOException {
        String richiesta = "wallet btc " + userNameClient;
        // scrivo il messaggio che voglio mandare
        dataOutputStream.writeUTF(richiesta); //mando il messaggio
        dataOutputStream.flush();
        String messaggioFromServer = dataInputStream.readUTF();
        System.out.println(messaggioFromServer);
        if(messaggioFromServer.equals("endWalletBTC")){
            System.out.println("Il tuo portafogli e' vuoto");
            return;
        }
        while (!messaggioFromServer.equals("endWalletBTC")){
            messaggioFromServer = dataInputStream.readUTF();
            if(!messaggioFromServer.equals("endWalletBTC"))
                System.out.println(messaggioFromServer);
        }

    }
}