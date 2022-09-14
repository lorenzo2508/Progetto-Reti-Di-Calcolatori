
//CLASSE CHE UTILIZZO PER COSTRUIRE UNA COPPIA CHE POI USERO' COME CHIAVE PER LA CONCURRENT HASH MAP
//CHE UTILIZZO PER IMPLEMENTARE LA HOME DEL SOCIALNETWORK


public class Pair<T, U> {
    //primo elemento della coppia
    private T first;
    //socondo elemento
    private U second;

    //costruttore che inizializza gli elementi della coppia.
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    //Metodo che ritorna il primo elemento della coppia
    public T getFirst() {
        return first;
    }

    //Metodo che ritorna il secondo elemento della coppia
    public U getSecond() {
        return second;
    }

    public void setSecond(U second) {
        this.second = second;
    }
}
