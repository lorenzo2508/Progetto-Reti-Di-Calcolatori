



import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;



public class WorkerServerTCP implements Runnable {
    //VARIABILI D'ISTANZA
    private ConcurrentHashMap<User, Blog> usersProfile;
    private ConcurrentHashMap <Pair<User, Blog>, Home> socialNetwork;
    private ConcurrentHashMap<User, Post> posts;
    private final Socket sockServer;
    private LinkedBlockingQueue<Pair<User, String>> utentiLoggati;
    private RMIFollowServer rmiFollowing;
    private ConcurrentHashMap<String, Wallet> wallet;
    private String indirizzoMulticast;
    private Integer portaMulticast = 0;
    //COSTRUTTORE
    public WorkerServerTCP(ConcurrentHashMap<User, Blog> usersProfile, ConcurrentHashMap<User, Post> posts, Socket sockServer, LinkedBlockingQueue<Pair<User, String>> utentiLoggati, RMIFollowServer rmiFollowing, ConcurrentHashMap <Pair<User, Blog>, Home> socialNetwork, ConcurrentHashMap<String, Wallet>wallet, int portaMulticast, String indirizzoMulticast) {
        this.usersProfile = usersProfile;
        this.posts = posts;
        this.sockServer = sockServer;
        this.utentiLoggati = utentiLoggati;
        this.rmiFollowing = rmiFollowing;
        this.socialNetwork = socialNetwork;
        this.wallet = wallet;
        this.portaMulticast = portaMulticast;
        this.indirizzoMulticast = indirizzoMulticast;

    }

    @Override
    public void run() {
        while (true) {
            try {
                InputStream inputStream = sockServer.getInputStream();
                // create a DataInputStream so we can read data from it.
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                OutputStream outputStream = sockServer.getOutputStream();
                // create a data output stream from the output stream so we can send data through it
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                // read the message from the socket
                String message = dataInputStream.readUTF();
                System.out.println(message);

                //CREATEPOST
                if (message.equals("post")) {
                    createPost(dataInputStream, dataOutputStream);
                    continue;
                }
                if(message.equals("comment")){
                    addComment(dataInputStream, dataOutputStream);
                    continue;
                }
                ArrayList<String> datiUtenteOperation = new ArrayList<>();
                datiUtenteOperation = tockenizzatore(message);

                //LOGIN
                if (datiUtenteOperation.get(0).equalsIgnoreCase("login")) {
                    //rimuovo la parola "login" dal primo indice dell'array
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    String password = datiUtenteOperation.remove(0);
                    String randIdClient = datiUtenteOperation.remove(0);
                    System.out.println("idClient= " + randIdClient);
                    logIn(userName, password, dataOutputStream, randIdClient, dataInputStream);
                    continue;

                }

                //LOGOUT
                if (!datiUtenteOperation.isEmpty() && datiUtenteOperation.get(0).equalsIgnoreCase("logout")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    String randIdClient = datiUtenteOperation.remove(0);
                    logOut(userName, randIdClient);
                    return;
                }

                //LISTUSERS
                if (!datiUtenteOperation.isEmpty() && datiUtenteOperation.get(0).equalsIgnoreCase("list users")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    listUsers(dataOutputStream, userName);
                    continue;
                }
                //FOLLOW 
                if(!datiUtenteOperation.isEmpty() && datiUtenteOperation.get(0).equalsIgnoreCase("follow")){
                    String command = datiUtenteOperation.remove(0);
                    String userToFollow = datiUtenteOperation.remove(0);
                    follow(userToFollow, dataInputStream, dataOutputStream);
                    continue;
                }
                //UNFOLLOW
                if(!datiUtenteOperation.isEmpty() && datiUtenteOperation.get(0).equalsIgnoreCase("unfollow")){
                    String command = datiUtenteOperation.remove(0);
                    String userToUnFollow = datiUtenteOperation.remove(0);
                    unFollow(userToUnFollow, dataInputStream, dataOutputStream);
                    continue;
                }

                //LIST FOLLOWING
                if (!datiUtenteOperation.isEmpty() && datiUtenteOperation.get(0).equalsIgnoreCase("list following")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    listFollowing(userName, dataOutputStream);
                    continue;
                }
                //VIEW BLOG
                if (datiUtenteOperation.get(0).equalsIgnoreCase("blog")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    viewBlog(userName, dataOutputStream);
                    continue;
                }

                //SHOWFEED
                if (datiUtenteOperation.get(0).equalsIgnoreCase("show feed")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    showFeed(userName, dataOutputStream);
                    continue;
                }
                //SHOWPOST
                if (datiUtenteOperation.get(0).equalsIgnoreCase("show post")) {
                    String command = datiUtenteOperation.remove(0);
                    String idPost = datiUtenteOperation.remove(0);
                    showPost(idPost, dataOutputStream);
                    continue;
                }

                //RATEPOST
                if (datiUtenteOperation.get(0).equalsIgnoreCase("rate")) {
                    String command = datiUtenteOperation.remove(0);
                    String idPost = datiUtenteOperation.remove(0);
                    String voto = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    ratePost(idPost, voto, userName, dataOutputStream);
                    continue;
                }
                //DELETEPOST
                if (datiUtenteOperation.get(0).equalsIgnoreCase("delete")) {
                    String command = datiUtenteOperation.remove(0);
                    String idPost = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    deletePost(idPost, userName, dataOutputStream);
                    continue;
                }
                //REWIN
                if (datiUtenteOperation.get(0).equalsIgnoreCase("rewin")) {
                    String command = datiUtenteOperation.remove(0);
                    String idPost = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    rewinPost(idPost, userName, dataOutputStream);
                    continue;
                }
                //GETWALLETT
                if (datiUtenteOperation.get(0).equalsIgnoreCase("wallet")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    getWallet( dataOutputStream, userName);
                    continue;
                }
                //GETWALLETBTC
                if (datiUtenteOperation.get(0).equalsIgnoreCase("wallet btc")) {
                    String command = datiUtenteOperation.remove(0);
                    String userName = datiUtenteOperation.remove(0);
                    getWalletBitcoin(dataOutputStream, userName);
                    continue;
                }







            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /*METODO PER IL LOGOUT DI UN UTENTE DAL SOCIAL
     * RITORNA AL CLIENT:
     * ok-> se il logout va a buon fine
     * UserNotFound -> se l'utente non è loggato
     * */
    public void logOut(String username, String randIdClient) throws IOException {
        OutputStream outputStream = sockServer.getOutputStream();

        // create a data output stream from the output stream so we can send data through it
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        for (Pair<User, String> entry : utentiLoggati ) {
            if(entry.getFirst().getUserName().equals(username) && entry.getSecond().equals(randIdClient)){
                System.out.println("Utente logout " + entry.getFirst().getUserName() +  " " +  entry.getSecond());
                utentiLoggati.remove(entry);
                dataOutputStream.writeUTF("ok");
                return;
            }

        }
        dataOutputStream.writeUTF("UserNotFound");




    }

    /*METODO PER FARE IL LOGIN
    * SCRIVE SUL SOCKET I SEGUENTI MESSAGGI IL CLIENT LEGGE I MESSAGGI E SI COMPORTA DI CONSEGUENZA:
    * ErrPass -> se la password è sbagliata al momento del login
    * ErrUsername -> se l'username è sbagliato (cioè viene trovata la password ma l'username associato è sbagliato)
    * UserNotReg-> se l'utente non è presente nel social, perché non ha prima fatto la registrazione
    * ok-> se tutto va a buon fine e l'utente è loggato
    * al momento del login viene inviato anche l'indirizzo di multicast e la porta che il client dovrà usare */
    public void logIn(String userName, String password, DataOutputStream dataOutputStream, String randIdClient, DataInputStream dataInputStream) throws IOException {
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            //va tutto bene faccio il login e aggiungo alla lista degli utenti loggati l'utente e l'identificativo del client
            //loggato
            if (entry.getKey().getUserName().equals(userName) && entry.getKey().getPassword().equals(password)) {
                //creo la coppia (utente, idClient)
                Pair<User, String> identificativoUserClientConnesso = new Pair<>(entry.getKey(), randIdClient);
                //aggiungo la coppia alla lista che contiene i login
                utentiLoggati.add(identificativoUserClientConnesso);
                //invio il messaggio al client che va tutto bene
                dataOutputStream.writeUTF("ok");

                dataOutputStream.writeUTF(indirizzoMulticast); //indirizzo di multicast

                String multicastPort = Integer.toString(portaMulticast);

                dataOutputStream.writeUTF(multicastPort); //porta di multicast
                //Parte di RMI per il login 
               //aggiorno la lista dei follower tramite RMI, perché al momento del login di un nuovo utente
                // io devo aggiornare in massa la lista che tengo lato client dei follower
               //perché lui potrebbe richiederla subito in quanto, potrebbe avere vecchi follower
                String clientNotification = dataInputStream.readUTF();
                if(clientNotification.equals("ok")) {
                    ArrayList<String> listaFollower = entry.getKey().getFollower();
                    NotifyFollowClient client = rmiFollowing.getMappa().get(userName);//user name del client che si è connesso
                    if(client != null) {
                        client.sendList(listaFollower); //così chiamo il metodo RMI che mi riempie la lista.
                    }
                }

                return;
            }
        }
        //password errata
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userName) && !entry.getKey().getPassword().equals(password)) {
                System.err.println("password errata");
                //QUI DEVO MANDARE UN MESSAGGIO AL CLIENT CON UN CODICE DI ERRORE PER DIRE CHE LA PASS è SBAGLIATA
                dataOutputStream.writeUTF("ErrPass");
                return;

            }
        }
        //username non trovato
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (!entry.getKey().getUserName().equals(userName) && entry.getKey().getPassword().equals(password)) {
                System.err.println("Username non trovato");
                //QUI MANDARE UN CODICE DIVERSO AL CLIENT IN MODO TALE CHE SAPPIA CHE L'UTENTE HA INSERITO UNO USER NAME SBAGLIATO
                dataOutputStream.writeUTF("ErrUsername");
                return;
            }
        }

        //utente non registrato
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
             if (!entry.getKey().getUserName().equals(userName) && !entry.getKey().getPassword().equals(password)) {
                System.err.println("utente non registrato");
                //QUI MANDARE UN CODICE DIVERSO AL CLIENT IN MODO TALE CHE SAPPIA CHE L'UTENTE NON è REGISTRATO
                dataOutputStream.writeUTF("UserNotReg");
                return;
            }
        }






    }

    /*METODO PER CREARE IL POST E AGGIUNGERLO AL BLOG DI UN UTENTE E AL SOCIAL.
     * INVIA AL CLIENT  L'ID DEL POST SE L'AZIONE DI POSTING AVVIENE CORRETTAMENTE*/
    public void createPost(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String username = dataInputStream.readUTF(); //leggo il nome di chi ha fatto il post
        System.out.println(username);
        String titolo = dataInputStream.readUTF(); //leggo il titolo
        String contenuto = dataInputStream.readUTF(); //leggo il contenuto
        Post post = new Post(); //creo un nuovo post
        //nella mappa userProfile cerco l'utente con tale username, per individuarne il blog
        //faccio una ricerca e non una semplice get perché voglio assicurarmi che lo user name di chi posta sia effettivamente
        //associato ad un utente sul social
        User user = null;
        Blog blog = null;
        boolean trovato = false;
        String idPost = "";
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(username)) {
                user = entry.getKey(); //prendo lo user name
                blog = entry.getValue(); //prendo il blog
                trovato = true;

            }

        }
        if(trovato == false){
            System.err.println("utente non trovato" );
            dataOutputStream.writeUTF("errPost");
            return;
        }
        post.setCreator(user.getUserName()); //setto l'autore del post
        post.setTitolo(titolo); //setto il titolo del post
        post.setContenuto(contenuto); //setto il contenuto
        blog.getListaDeiPostSulBlog().add(post); //aggiungo il post al blog
        user.addPost(post);
        posts.put(user, post); //metto il post nella mappa che contiene come chiave gli utenti e come valore i post
        idPost = post.getIdPost().toString();

        //metto il post che io ho creato nel mio feed
        for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet()) {
            if (entry.getKey().getFirst().getUserName().equals(username)) {
                entry.getValue().addPost(post);
            }
        }
        //riempio la mappa social network mettendo il post nel feed dei follower del creatore del post
        //per ogni utente nella lista dei follower di colui che ha postato
        for (String name : user.getFollower()){
            //cerco il feed dell'utente presente nella lista dei follower e ci aggiungo il post creato dal client che ha richiesto il servizio
            for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet())
                if(entry.getKey().getFirst().getUserName().equals(name)){
                    entry.getValue().addPost(post);
                }

        }
        dataOutputStream.writeUTF(idPost); //invio al client il messaggio che l'azione di posting è andata a buon fine
        return;


    }


    /*METODO PER MANDARE AL CLIENT LA LISTA DELLE PERSONE CHE HANNO ALMENO UN TAG IN COMUNE CON IL CLIENT
    * IL METODO NON FA ALTRO CHE INVIARE UNA STRINGA AL SERVER CON LO USER NAME E I TAG DELL'UTETNTE INDIVIDUATO
    */
    public void listUsers(DataOutputStream dataOutputStream, String userName) throws IOException {
        User utenteRichiedenteListaUtenti = null;
        //cerco l'utente che ha richiesto la lista degli utenti, in modo da prendere la sua lista dei tags
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet())  {
            if(entry.getKey().getUserName().equals(userName)){
                utenteRichiedenteListaUtenti = entry.getKey();
            }

        }

        ArrayList<String> listTags ; //variabile ausiliaria a cui assegno la lista di tag di un utente del social
        ArrayList<User> listaUtentiTagComuni = new ArrayList<>(); //contiene gli utenti con stessa lista di tag del richiedente del serivzio
        //prendo la lista dei tag per ogni utente e la confronto con quella dell'utente che ha richiesto il comando
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            listTags = new ArrayList<String>(entry.getKey().getTags()) ;
            for (String tag : listTags) {
                assert utenteRichiedenteListaUtenti != null;
                //se l'utente ha almeno un tag in comune con il richiedente e
                if(utenteRichiedenteListaUtenti.getTags().contains(tag) && !entry.getKey().getUserName().equals(utenteRichiedenteListaUtenti.getUserName())){
                    //se l'utente non è già stato aggiunto alla lista
                    /*Questo if mi serve per togliere la ridondanza perché altrimenti se due utenti hanno in comune
                    * più di un tag, verrebbero aggiunti tante volte alla lista "listaUtentiTagComuni", quante sono i tag uguali
                    *   */
                    if(!listaUtentiTagComuni.contains(entry.getKey())) {
                        listaUtentiTagComuni.add(entry.getKey()); //allora aggiungo l'utente alla lista degli user con tag in comune
                    }
                }

            }

        }
        //se esiste almeno un utente allora ritorno "ok" al client per fargli sapere che tutto è andato bene
        if(!listaUtentiTagComuni.isEmpty()){
            dataOutputStream.writeUTF("ok");
        }

        //se non ci sono utenti mando il messaggio del fatto che "nessuno è come te" per adesso sul social
        else
            dataOutputStream.writeUTF("notUserLikeYou");

        for (User entry :
                listaUtentiTagComuni) {
            //se c'è almeno un utente allora invio lo user name e ci concateno i tag
            if(!listaUtentiTagComuni.isEmpty()){
                //creo la stringa che il server manda al client. Unisco lo user name e la lista dei tag
                String rispostaFromServer = entry.getUserName() + " |-> ";
                //unisco username e lista dei tag con il metodo .join()
                String joinedRispFromServer = rispostaFromServer + String.join(" ", entry.getTags());
                //invio lo user name con i tag concatenati
                dataOutputStream.writeUTF(joinedRispFromServer);
            }


        }
        dataOutputStream.writeUTF("endOfList");


    }


    /*METODO PER IMPLEMENTARE IL FOLLOWING TRAMITE RMI*/
    public void follow (String userNamePersonToFollow, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String userNameClient = dataInputStream.readUTF(); //user name di colui che segue un utente

        User userFollowed = null; //utente che viene seguito
        User utenteCheSegue = null; //utente che compie l'azione di seguirne un altro

        //cerco l'utente che viene seguito nella mappa dei profili (cerco proprio l'oggetto User che poi a cui aggiornerò la lista dei follower)
        Blog blogUtenteSeguito = null;
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNamePersonToFollow)) {
                userFollowed = entry.getKey();
                blogUtenteSeguito = entry.getValue();
            }
        }
        //cerco l'utente che segue nella mappa dei profili (cerco proprio l'oggetto User che poi a cui aggiornerò la lista dei following)
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                utenteCheSegue = entry.getKey();
            }
        }
        assert userFollowed != null;
        //controllo che non si provi a seguire più di una volta un utente
        if(userFollowed.getFollower().contains(utenteCheSegue.getUserName())){
            System.err.println("non puoi seguire più di una volta");
            dataOutputStream.writeUTF("non puoi seguire due volte lo stesso utente");
            return;
        }
        //alla lista dei follower dell'utente che viene seguito aggiungo l'utente che segue
        userFollowed.getFollower().add(utenteCheSegue.getUserName());
        //aggiungo alla lista dei following, dell'utente che compie l'azione di seguire, l'utente che viene seguito.
        utenteCheSegue.getFollowing().add(userFollowed.getUserName());

        //RMI
        NotifyFollowClient client = rmiFollowing.getMappa().get(userFollowed.getUserName());

        try {
            if(client != null) {
                client.setFollower(utenteCheSegue.getUserName()); //aggiorno la lista dei follower dell'utente che viene seguito
                dataOutputStream.writeUTF("ok, ora segui " + userNamePersonToFollow);
            }
            else
                dataOutputStream.writeUTF("clientNull");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //riempio la mappa social network mettendo il post nel feed dei follower del creatore del post
        //per ogni utente nella lista dei follower di colui che ha postato
        for (Post post : blogUtenteSeguito.getListaDeiPostSulBlog()){
            //cerco il feed dell'utente presente nella lista dei follower e ci aggiungo il post creato dal client che ha richiesto il servizio
            for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet())
                if(entry.getKey().getFirst().getUserName().equals(utenteCheSegue.getUserName())){
                    entry.getValue().addPost(post);
                }

        }


    }

    /*METODO PER IMPLEMENTARE L'UNFOLLOWING TRAMITE RMI*/
    public void unFollow (String userNamePersonToUnFollow,DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String userNameClient = dataInputStream.readUTF(); //user name di colui che vuole smettere di seguire un utente
        System.out.println(userNameClient);
        User userUnFollowed = null; //utente che viene smesso di essere seguito
        User utente = null; //utente che compie l'azione di smettere di seguire un altro
        //cerco l'utente che viene smesso di seguire nella mappa dei profili
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNamePersonToUnFollow)) {
                userUnFollowed = entry.getKey();
            }
        }
        //cerco l'utente che smette di seguire
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                utente = entry.getKey();
            }
        }
        //provocano la terminazione del programma qualora uno dei due oggetti fosse null
        assert userUnFollowed != null;
        assert utente != null;
        userUnFollowed.getFollower().remove(utente.getUserName()); //alla lista dei follower dell'utente che viene seguito aggiungo l'utente che segue

        utente.getFollowing().remove(userUnFollowed.getUserName());
        System.out.println(userUnFollowed.getUserName() + " " + userUnFollowed.getFollower());

        NotifyFollowClient client = rmiFollowing.getMappa().get(userUnFollowed.getUserName());
        try {
            System.out.println(userNamePersonToUnFollow);
            if(client != null) {
                client.removeFollower(utente.getUserName()); //aggiorno la lista dei follower dell'utente che viene smesso di seguire
                dataOutputStream.writeUTF("ok, ora hai smesso di seguire " + userNamePersonToUnFollow);
            }
            else
                dataOutputStream.writeUTF("clientNull");
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    /*METODO PER MANDARE AL CLIENT LA LISTA DELLE PERSONE CHE SEGUE (APPUNTO I SUOI FOLLOWING)
     */
    public void listFollowing(String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        User utenteRichiedente = null ;
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                utenteRichiedente = entry.getKey();
            }
        }
        for (String entry :
                utenteRichiedente.getFollowing()) {
            dataOutputStream.writeUTF(entry);
        }
        dataOutputStream.writeUTF("endList");

    }

    /*METODO PER MANDARE AL CLIENT I POST CHE LUI HA CREATO E CHE STANNO SUL SUO BLOG
     */
    public void viewBlog (String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        Blog userBlog = null;
        String postCreator = null;
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                userBlog = entry.getValue();
            }
        }
        String infoPost;
        assert userBlog != null;
        for (Post post :
                userBlog.getListaDeiPostSulBlog()) {
            postCreator = post.getCreator();
            infoPost = "id: " + post.getIdPost() + " Autore: " + postCreator
                    + " Titolo: " + post.getTitolo();
            dataOutputStream.writeUTF(infoPost);

        }
        dataOutputStream.writeUTF("endList");
    }

    /*METODO PER MANDARE AL CLIENT I POST DELLE PERSONE CHE SEGUE*/
    public void showFeed (String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        Blog userBlog = null;
        User user = null;
        String infoPost; //stringa che invio sullo stream
        String postCreator; //mi serve per prendere lo user name del creatore del post. perché l'oggetto post nel campo creatore ha un oggetto di tipo User
        Pair<User, Blog> userBlogPair = null;
        //cerco nella mappa social network l'utente che vuole vedere il proprio feed
        for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet()) {
            if(entry.getKey().getFirst().getUserName().equals(userNameClient)){
                userBlogPair = entry.getKey();
                user = entry.getKey().getFirst();
                userBlog = entry.getKey().getSecond();
            }
        }
        //per ogni post nella home di un utente identificato con la chiave "userBlogPair" nella mappa
        // social network (home è il valore della mappa) prendo la lista dei post
        //con la entry "post" prendo il singolo post della lista, costruisco la stringa da mandare al client e la invio
        for (Post post : socialNetwork.get(userBlogPair).getListOfPost()) {
            postCreator = post.getCreator();
            infoPost = "id: " + post.getIdPost() + " autore: " + postCreator + " titolo: " + post.getTitolo(); //costruisco stringa
            dataOutputStream.writeUTF(infoPost);
        }
        dataOutputStream.writeUTF("endFeed");


    }


    /*METODO PER MOSTRARE AL CLIENT IN COMPLETEZZA UN POST DATO L'ID QUINDI CON:
    * NUMERO DI LIKE
    * NUMERO DI DISLIKE
    * COMMENTI
    * TITOLO
    * E CORPO DEL POST*/
    public void showPost(String idPost, DataOutputStream dataOutputStream) throws IOException{
        Post post = null;
        Integer id = Integer.parseInt(idPost);
        for (Map.Entry<User, Post> entry : posts.entrySet()) {
            for(Post p : entry.getKey().getListPost()){
                if (p.getIdPost().equals(id)) {
                    post = p;
                }
            }

        }
        String infoPost; //stringa che invio sullo stream
        String postCreator;
        assert post != null;
        postCreator = post.getCreator();
        int numeroVotiPositivi = post.getPositiveEvaluation().size();
        int numeroVotiNegativi = post.getNegativeEvaluation().size();
        infoPost =  "titolo: " + post.getTitolo() + " contenuto: " + post.getContenuto() + " voti negativi: " + numeroVotiNegativi + " voti positivi: " + numeroVotiPositivi ;
        dataOutputStream.writeUTF(infoPost);
        //cerco nella lista dei commenti, che è un attributo dell'oggetto post tutti i commenti e li invio al client
        for (Pair<String, String> entry : post.getComment()){
            dataOutputStream.writeUTF(entry.getFirst() + " : " + entry.getSecond());
        }
        dataOutputStream.writeUTF("endComment");


    }

    /*METODO PER DARE UNA VALUTAZIONE POSITIVA O NEGATIVA AL POST DI UN UTENTE SE E' PRESENTE NEL FEED DEL CLIENT*/
    public void ratePost(String idPost, String vote, String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        User user = null;
        Blog userBlog = null;
        Integer id = Integer.parseInt(idPost);
        Integer voto = Integer.parseInt(vote);
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                user = entry.getKey(); //utente che richiede servizio
                userBlog = entry.getValue(); //blog dell'utente
            }
        }

        LinkedBlockingQueue<Post> listPostFeed = new LinkedBlockingQueue<>();


        //creco se l'utente ha nel suo feed il post, in tal caso può dargli una valutazione altrimenti no


        for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet()) {
            if(entry.getKey().getFirst().getUserName().equals(userNameClient)){
                listPostFeed = entry.getValue().getListOfPost(); //prendo la lista dei post nel feed del client richiedente il servizio
            }

        }
        //se la lista dei post sul feed è vuota significa che il client non ha post sul suo feed quindi non può votare
        if(listPostFeed.isEmpty()){
            System.err.println("feed vuoto per user: " + userNameClient);
            dataOutputStream.writeUTF("feedEmpty");
            return;
        }

        //controllo se il post si trova nella lista dei post del client
        for (Post post : listPostFeed) {
            //identifico il post
            if (Objects.equals(post.getIdPost(), id)){
                //controllo che l'utente non abbia già votato positivamente
                if(post.getPositiveEvaluation().contains(user.getUserName())){
                    dataOutputStream.writeUTF("tooMuchLikes");
                    return;

                }
                //se ho già messo dislike
                if(post.getNegativeEvaluation().contains(user.getUserName())){
                    dataOutputStream.writeUTF("tooMuchDislike");
                    return;
                }
                if(voto == 1 && !post.getNegativeEvaluation().contains(user.getUserName())){
                    post.addPositiveEvaluetion(user.getUserName());
                    post.addListLikeRecenti(userNameClient);
                    dataOutputStream.writeUTF("hai messo like al post. idPost: " + id);
                    return;
                }
                if(voto == -1 && !post.getPositiveEvaluation().contains(user.getUserName())){
                    post.addNegativeEvaluetion(user.getUserName());
                    post.addListdisLikeRecenti(userNameClient);
                    dataOutputStream.writeUTF("hai messo dislike al post. idPost: " +id);
                    return;
                }


            }
        }
        dataOutputStream.writeUTF("idNotFoundInYourFeed");
    }


    /*METODO PER COMMENTARE IL POST DI UN UTENTE SE E' PRESENTE NEL FEED DEL CLIENT*/
    public void addComment (DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        User user = null;
        boolean trovato = false;
        String id = dataInputStream.readUTF();
        Integer idPost = Integer.parseInt(id);
        String commento = dataInputStream.readUTF();
        String userNameClient = dataInputStream.readUTF();
        //cerco l'utente che richiede il servizio (il client)
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                user = entry.getKey(); //utente che richiede servizio
            }
        }

        LinkedBlockingQueue<Post> listPostFeed = new LinkedBlockingQueue<>();

        //creco se l'utente ha nel suo feed il post, in tal caso può dargli una valutazione altrimenti no
        for (Map.Entry<Pair<User, Blog>, Home> entry : socialNetwork.entrySet()) {
            if(entry.getKey().getFirst().getUserName().equals(userNameClient)){
                listPostFeed = entry.getValue().getListOfPost(); //prendo la lista dei post nel feed del client richiedente il servizio
            }

        }

        //se la lista dei post sul feed è vuota significa che il client non ha post sul suo feed quindi non può commentare
        if(listPostFeed.isEmpty()){
            System.err.println("feed vuoto per user: " + userNameClient);
            dataOutputStream.writeUTF("feedEmpty");
            return;
        }

        //nella lista dei post presenti sul feed cerco il post d'interesse e aggiungo il commento
        for (Post post : listPostFeed) {
            //identifico il post
            if (Objects.equals(post.getIdPost(), idPost)) {
                if (post.getCreator().equals(userNameClient)){
                    dataOutputStream.writeUTF("notCommentYourPost");
                    return;
                }
                post.addComment(commento, user.getUserName()); //aggiungo il commento alla mappa MAPPA <COMMENTO, USER>
                post.addListCommentatoriRecenti(userNameClient);
                trovato = true;
            }

        }
        if(trovato == false){
            dataOutputStream.writeUTF("postNotFoundOnFeed");
            return;
        }

        dataOutputStream.writeUTF("commentato correttamente");

    }

    /*METODO PER CANCELLARE UN POST */
    public void deletePost (String id, String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        User user = null;
        Blog userBlog = null;
        Post postToDelete = null;
        Integer idPost = Integer.parseInt(id);
        //cerco l'utente che richiede il serivzio
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                user = entry.getKey(); //utente che richiede servizio
                userBlog = entry.getValue(); //blog dell'utente
            }
        }
        assert userBlog != null;
        boolean trovato = false;
        //controllo se il post è stato creato dall'utente richiedente il servizio o no.
        for (Map.Entry<User, Post> entry1 : posts.entrySet()) {
            //se l'idPost inserito dal client è uguale all'id di un post e l'user che ha creato il post è uguale all'user che ha richiesto il
            //servizio allora so che il post è stato creato dall'utente che ha richiesto il servizio
            if (Objects.equals(entry1.getValue().getIdPost(), idPost) && entry1.getKey().equals(user)){
                trovato = true;
                postToDelete = entry1.getValue();
                posts.remove(entry1.getKey(), entry1.getValue());
            }
        }

        //se non ho trovato il post associato all'utente che richiede il servizio allora mando il messaggio al client e termino
        if(trovato == false){
            dataOutputStream.writeUTF("notYourPost");
            return;
        }
        //elimino il post dal mio blog, e dalla lista dei post dell'utente
        userBlog.getListaDeiPostSulBlog().remove(postToDelete);
        user.getListPost().remove(postToDelete);
        //per tutti gli utenti che mi seguono rimuovo il post dal loro blog (se hanno fatto rewin)


        for (String entry : user.getFollower()) {
            for(Map.Entry<User, Blog> entry1 : usersProfile.entrySet()){
                if(entry1.getKey().equals(entry)){
                    Blog b = entry1.getValue();
                    //rimuovo il post dal blog degli utenti che mi seguono
                    if(b.getListaDeiPostSulBlog().contains(idPost)){
                        b.removePostFromBlog(idPost);
                        usersProfile.put(entry1.getKey(), b);
                    }
                }
            }


        }

        //creo un arrayList di feed, dopo di che scorrerò tutto l'arrayList per cercare il post da eliminare nei vari feed

        ArrayList<Home> listaFeed = new ArrayList<>();
        for (String entry : user.getFollower()){
            for(Map.Entry<Pair<User, Blog>, Home> entry1 : socialNetwork.entrySet()){
                if(entry1.getKey().getFirst().getUserName().equals(entry) || entry1.getKey().getFirst().getUserName().equals(userNameClient)){
                    Home h = entry1.getValue();
                    if(h.getListOfPost().contains(postToDelete)){
                        h.removePost(postToDelete);
                        socialNetwork.put(entry1.getKey(), h);
                    }

                }
            }

        }
//        for(Home entry : listaFeed){
//            if(entry.getListOfPost().contains(postToDelete)){
//                entry.removePost(postToDelete);
//            }
//        }

        dataOutputStream.writeUTF("cancellato correttamente");
    }

    public void rewinPost (String id, String userNameClient, DataOutputStream dataOutputStream) throws IOException {
        User user = null;
        Blog userBlog = null;
        Integer idPost = Integer.parseInt(id);
        Post postToRewin = null;
        //cerco l'utente che richiede il serivzio
        for (Map.Entry<User, Blog> entry : usersProfile.entrySet()) {
            if (entry.getKey().getUserName().equals(userNameClient)) {
                user = entry.getKey(); //utente che richiede servizio
                userBlog = entry.getValue(); //blog dell'utente
            }
        }
        //lista dei post che uso per vedere se il post è sul feed del client che richiede il servizio
        LinkedBlockingQueue<Post> listOfPostOnFeed = null;

        for(Map.Entry<Pair<User,Blog>, Home> entry : socialNetwork.entrySet()){
            if(entry.getKey().getFirst().getUserName().equals(userNameClient)){
                listOfPostOnFeed = entry.getValue().getListOfPost();
            }
        }

        //prendo la lista dei post che si trovano sul feed del client richiedente il servizio
        boolean trovato = false;
        //cerco il post se è presente salvo il suo riferimento in postToRewin
        assert listOfPostOnFeed != null;
        for (Post post :
                listOfPostOnFeed) {
            if (Objects.equals(post.getIdPost(), idPost)){
                postToRewin=post;
                postToRewin.getRewinnedUsers().add(userNameClient);
                trovato=true;

            }
        }
        //se non ho trovato il post invio al client il messaggio che il post non è sul suo feed
        if (trovato==false){
            dataOutputStream.writeUTF("postNotOnYourFeed");
            return;
        }

        //aggiungo il post al blog dell'utente richiedente il servizio di rewin
        usersProfile.get(user).getListaDeiPostSulBlog().add(postToRewin);

        assert user != null;
        //prendo la lista dei follower dell'utente che ha richiesto il servizio
        ArrayList<String>listFollower = user.getFollower();

        //per ogni follower aggiungo il post condiviso al loro feed
        for (String name : listFollower) {
            for (Map.Entry<Pair<User, Blog>, Home> entry1 : socialNetwork.entrySet()){
                if(entry1.getKey().getFirst().getUserName().equals(name)){
                    entry1.getValue().addPost(postToRewin);

                }
            }

        }
        dataOutputStream.writeUTF("Post condiviso");
    }

    /*METODO PER RESTITUIRE LO STORICO DELLE TRANSAZIONI DI UN UTENTE SUL SOCIAL NETWORK
    * RITORNA:
    * SCRIVE SUL DATAOUTPUTSTREAM IL VALORE DELLA TRANSAZIONE E IL TIMESTAMP*/
    public void getWallet(DataOutputStream dataOutputStream, String userNameClient) throws IOException {
        ArrayList<Pair<Double, Timestamp>> transazioni = new ArrayList<>();
        if(wallet.isEmpty()){
            dataOutputStream.writeUTF("portafogli vuoto");
            dataOutputStream.writeUTF("endWallet");
            return;
        }
        for(Map.Entry<String, Wallet> entry : wallet.entrySet()){
            if(entry.getKey().equals(userNameClient)){
               transazioni = entry.getValue().getTransazioniAvvenute();
            }
        }
        double totaleWallet = 0;
        for(Pair<Double, Timestamp> entry : transazioni){
            totaleWallet = totaleWallet + entry.getFirst();
            dataOutputStream.writeUTF("Transazione -> " + entry.getFirst() + " TimeStamp-> " + entry.getSecond());
        }
        String totale = Double.toString(totaleWallet);
        dataOutputStream.writeUTF("Totale Wallet -> " + totale);
        dataOutputStream.writeUTF("endWallet");

    }

    /*METODO PER RESTITUIRE LO STORICO DELLE TRANSAZIONI DI UN UTENTE SUL SOCIAL NETWORK IN BITCOIN
     * RITORNA:
     * SCRIVE SUL DATAOUTPUTSTREAM IL VALORE DELLA TRANSAZIONE IN BITCOIN E IL TIMESTAMP*/
    public void getWalletBitcoin(DataOutputStream dataOutputStream, String userNameClient) throws IOException {

        //creo la url
        URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=4&col=1&format=plain&rnd=new");

        // prendo l'inputStream attraverso la connessione con la url
        URLConnection connessione = url.openConnection();
        InputStream inputStream = connessione.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String valoreDalSito = null;
        double moltiplicatoreBTC = 0;
        // leggo il valore e lo salvo dentro la variabile moltiplicatoreBTC
        while ((valoreDalSito = bufferedReader.readLine()) != null) {
            System.out.println("valore BTC " + valoreDalSito);
            moltiplicatoreBTC = Double.parseDouble(valoreDalSito);
        }

        ArrayList<Pair<Double, Timestamp>> transazioni = new ArrayList<>();
        for(Map.Entry<String, Wallet> entry : wallet.entrySet()){
            if(entry.getKey().equals(userNameClient)){
                transazioni = entry.getValue().getTransazioniAvvenute();
            }
        }
        double ammountBitcoin = 0;
        double totaleWallet = 0;
        for(Pair<Double, Timestamp> entry : transazioni){
            ammountBitcoin = entry.getFirst() * moltiplicatoreBTC;
            totaleWallet = totaleWallet + ammountBitcoin;
            dataOutputStream.writeUTF("Transazione -> " + ammountBitcoin + " TimeStamp-> " + entry.getSecond());
        }
        String totale = Double.toString(totaleWallet);
        dataOutputStream.writeUTF("Totale Wallet BTC -> " + totale);
        dataOutputStream.writeUTF("endWalletBTC");


    }





    //METODO PER FARE LA TOCKENIZZAZIONE DI STRINGHE
    //RITORNA: ARRAYLIST CONETENTE LE STRINGHE TOCKENIZZATE SULLO SPAZIO
    private static ArrayList<String> tockenizzatore (String tmp){
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


        return inputDatiUtente;

    }

}