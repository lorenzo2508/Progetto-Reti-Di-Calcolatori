
import java.io.Serializable;
import java.util.ArrayList;

/*Classe che definisce un utente*/
/*La classe implementa l'interfaccia Serializable perché altrimenti avevo problemi con RMI in quanto la lista dei follower è composta da
* istanze della classe User. Senza l'interfaccia implementata l'eccezione "java.rmi.MarshalException: error marshalling arguments; nested exception is:
    java.io.NotSerializableException" veniva sollevata ogni volta che provavo a seguire un utente */
public class User implements Serializable {
    private String userName;
    private String password;
    private ArrayList<String> tags;
    //private ArrayList<User>follower;
    //private ArrayList<User> following;
    private ArrayList<String>follower;
    private ArrayList<String>following;


    private ArrayList<Post> listPost;

    private int id;

    //COSTRUTTORE
    public User(String userName, String password, ArrayList<String>tags){
        this.userName = userName;
        this.password = password;
        this.following = new ArrayList<>();
        this.follower = new ArrayList<>();
        this.tags = tags;
        this.listPost = new ArrayList<>();

    }

    public User(String userName, String password, ArrayList<String> tags, ArrayList<String> follower, ArrayList<String> following){
        this.userName = userName;
        this.password = password;
        this.tags = tags;
        this.follower = follower;
        this.following = following;
        this.listPost = new ArrayList<>();

    }


    /*override del metodo .equals() perché dal momento che uso il metodo ".remove()" nella classe RMIfollowClient
    * riscontravo il problema che dentro il metodo remove viene usato il metodo equals per vedere se l'oggetto da rimovere è
    * uguale o meno all'oggetto che si chiede di rimuovere. Il problema stava nel fatto che siccome la struttura dati della classe
    * conteneva riferimenti ad oggetti della classe user, quando andava a fare il confronto tra l'oggetto che chiedevo di rimuovere
    * e il riferimento ad esso il metodo equals tornava false e dunque non rimuoveva l'oggetto.
    * Posto che, per tesi, i miei user name sono univoci allora ho riscritto il metodo equals che viene invocato dal metodo remove() su
    * gli oggetti della classe user, contenuti nella struttura dati, in modo tale che non faccia il confronto tra gli oggetti
    * ma tra gli user name (che sono un campo della classe User, quindi un attributo degli oggetti contenuti nella struttura dati). */
    @Override
    public boolean equals(Object o){
        if(!(o instanceof User))
            return false;
        User u = (User) o;
        return this.userName.equals(u.userName);
    }
    public String getPassword(){
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> getFollower() {
        return follower;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public ArrayList<Post> getListPost() {
        return listPost;
    }

    public void addPost (Post post){
        listPost.add(post);
    }




}
