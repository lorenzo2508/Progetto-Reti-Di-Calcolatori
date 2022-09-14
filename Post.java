


import com.google.gson.annotations.Expose;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/*classe che definisce il post*/
public class Post {
    private String creator;

    @Expose(serialize = false, deserialize = false)
    private static AtomicInteger count = new AtomicInteger(0); //contatore che incremento ogni volta che creo un post cosi ho id progressivi

    private LinkedBlockingQueue<Pair<String, String>> comment; //mappa per i commenti
    private LinkedBlockingQueue<String>positiveEvaluation; //lista delle valutazioni positive
    private LinkedBlockingQueue<String> negativeEvaluation; //lista delle valutazioni negative
    //lista che contiene gli utenti che hanno ricondiviso il post
    private LinkedBlockingQueue<String>rewinnedUsers;
    private LinkedBlockingQueue<String> likeRecenti = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<String> dislikeRecenti = new LinkedBlockingQueue<>();
    //uso una coppia in modo tale da avere, nella entry della lista, anche l'informazione su quante volte
    //un utente ha commentato il post
    private LinkedBlockingQueue<Pair<String, Integer>> commentatoriRecenti = new LinkedBlockingQueue<>();
    private String titolo;
    private String contenuto;
    //variabile final in modo tale che non possa essere modificata una volta assegnato l'id
    private final Integer idPost;
    private Integer contatoreValutazioni = 0;



    public Post() {

        this.comment = new LinkedBlockingQueue<>();
        this.positiveEvaluation = new LinkedBlockingQueue<>();
        this.negativeEvaluation = new LinkedBlockingQueue<>();
        this.rewinnedUsers = new LinkedBlockingQueue<>();
        this.idPost = count.getAndIncrement();


    }

    /*override del metodo .equals() perché dal momento che uso il metodo ".remove()" nella classe Home per rimuovere il post dalla home
     * riscontravo il problema che dentro il metodo remove viene usato il metodo equals per vedere se l'oggetto da rimovere è
     * uguale o meno all'oggetto che si chiede di rimuovere. Il problema stava nel fatto che siccome la struttura dati della classe
     * conteneva riferimenti ad oggetti della classe post, quando andava a fare il confronto tra l'oggetto che chiedevo di rimuovere
     * e il riferimento ad esso il metodo equals tornava false e dunque non rimuoveva l'oggetto.
     * Posto che, per tesi, i miei idPost sono univoci allora ho riscritto il metodo equals che viene invocato dal metodo remove() su
     * gli oggetti della classe Post, contenuti nella struttura dati, in modo tale che non faccia il confronto tra gli oggetti
     * ma tra gli idPost (che sono un campo della classe Post, quindi un attributo degli oggetti contenuti nella struttura dati). */
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Post))
            return false;
        Post p = (Post) o;
        return this.idPost.equals(p.idPost);
    }

    public Integer getIdPost() {
        return idPost;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void addComment(String comment, String commentatore) {
        Pair<String, String> coppiaCommentatoreCommento = new Pair<>(commentatore, comment);
        this.comment.add(coppiaCommentatoreCommento );
    }


    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public LinkedBlockingQueue<Pair<String, String>> getComment() {
        return comment;
    }

    public LinkedBlockingQueue<String> getPositiveEvaluation() {
        return positiveEvaluation;
    }

    public LinkedBlockingQueue<String> getNegativeEvaluation() {
        return negativeEvaluation;
    }

    public void addPositiveEvaluetion (String evaluation){
        positiveEvaluation.add(evaluation);
    }

    public void addNegativeEvaluetion (String evaluation){
        negativeEvaluation.add(evaluation);
    }

    public Integer getContatoreValutazioni() {
        return contatoreValutazioni;
    }

    public LinkedBlockingQueue<String> getRewinnedUsers() {
        return rewinnedUsers;
    }

    public LinkedBlockingQueue<String> getLikeRecenti() {
        return likeRecenti;
    }

    public LinkedBlockingQueue<String> getDislikeRecenti() {
        return dislikeRecenti;
    }

    public LinkedBlockingQueue<Pair<String, Integer>> getCommentatoriRecenti() {
        return commentatoriRecenti;
    }

    //questi metodi clear li uso per "pulire" le liste, che uso per il calcolo delle ricompense" ad ogni fine iterazione
    //del calcolo delle ricompense
    
    public void  clearListLikeRecenti(){
        likeRecenti.clear();
    }
    public void  clearListdisLikeRecenti(){
        dislikeRecenti.clear();
    }
    public void  clearListCommentatoriRecenti(){
       commentatoriRecenti.clear();
    }

    //metodo usato per incrementare il contatore delle valutazioni, è synchronized in modo tale che l'accesso al contatore sia
    //thread safe
    public synchronized void incrementaContatoreValutazioni(){
        contatoreValutazioni ++;
    }


    public void  addListLikeRecenti(String username){
        likeRecenti.add(username);
    }
    public void  addListdisLikeRecenti(String username){
        dislikeRecenti.add(username);
    }
    public void  addListCommentatoriRecenti(String username){
        Integer contatore = 1; //lo faccio partire da 1 perché al momento che una persona commenta il conteggio deve già partire da 1 in quanto ne ha pubblicato uno
        boolean trovato = false;
        Pair<String, Integer> coppiaCommentoNumeroVolte = new Pair<>(username, contatore);
        //cerco nella lista dei commenti recenti
        for (Pair<String, Integer> entry : commentatoriRecenti){
            //se identifico il commentatore sulla base dello user name vuol dire che aveva già fatto un commento allora:
            if(entry.getFirst().equals(username)){
                Integer cont = 0;
                cont = entry.getSecond();
                cont ++; //incremento il numero dei commenti che ha fatto
                entry.setSecond(cont); //aumento di 1 il numero dei commenti fatti dall'utente
                trovato = true;
            }
        }
        //se non l'ho trovato nella lista allora è un nuovo commentatore
        if(trovato == false){
            commentatoriRecenti.add(coppiaCommentoNumeroVolte); //aggiungo la coppia alla lista dei commentatori recenti
        }
    }
  
}
