


import java.sql.Timestamp;
import java.util.ArrayList;

public class Wallet {
    private ArrayList<Pair<Double, Timestamp>> transazioniAvvenute;

    public Wallet(){
        this.transazioniAvvenute = new ArrayList<Pair<Double, Timestamp>>();
    }

    public void addTransazione (Double ricompensa){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Pair<Double, Timestamp> coppiaRicompensaTimestamp = new Pair<>(ricompensa, timestamp);
        transazioniAvvenute.add(coppiaRicompensaTimestamp);
    }

    public ArrayList<Pair<Double, Timestamp>> getTransazioniAvvenute() {
        return transazioniAvvenute;
    }
}
