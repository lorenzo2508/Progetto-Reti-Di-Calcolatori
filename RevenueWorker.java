




import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RevenueWorker implements Runnable {
    private int tempoAttesa;
    private DatagramSocket datagramSocket = null;
    private ConcurrentHashMap<String, Wallet> wallet;
    private ConcurrentHashMap<User, Post> posts;
    private String indirizzoMulticast = null;
    private int porta;

    public RevenueWorker(int tempoAttesa, ConcurrentHashMap<User, Post> posts, ConcurrentHashMap<String, Wallet> wallet, String IndirizzoMulticast, int porta){
        this.tempoAttesa = tempoAttesa;
        this.posts = posts;
        this.wallet = wallet;
        this.indirizzoMulticast = indirizzoMulticast;
        this.porta = porta;
    }
    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(tempoAttesa);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            calcoloRicompense();

            //logica per comunicare sul multicast il fatto che il calcolo è stato fatto e che
            //potresti avre il wallet aggiornato
            try {
                datagramSocket = new DatagramSocket();
                InetAddress inetAddress = InetAddress.getByName(indirizzoMulticast);
                String messaggioDiMulticast = "Sono state calcolate le ricompense";
                DatagramPacket datagramPacket = new DatagramPacket(messaggioDiMulticast.getBytes(), messaggioDiMulticast.length(), inetAddress, porta);
                datagramSocket.send(datagramPacket);


            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }



    private void calcoloRicompense() {
        //logica per il calcolo delle ricompense
        Integer numeroDiLike;
        Integer numeroDiDislike;
        Integer numeroDiCommentatori;
        double guadagnoParzialeDaCommenti = 0;
        double guadagnoParzialeDaLike = 0;
        int differenza;
        double guadagnoTotale = 0;
        double guadagnoCuratori = 0;
        double guadagnoAutore = 0;
        //per ogni post nella mappa dei post, prendo le liste dei like/dislike recenti, dei commenti recenti e calcolo le ricompense
        ArrayList<Post> tuttiIpost = new ArrayList<>();
        for (Map.Entry<User, Post> entry : posts.entrySet()) {
            for (Post p : entry.getKey().getListPost()){
                tuttiIpost.add(p);
            }
        }
        for (Post entry : tuttiIpost) {
            //se le liste sono vuote passo al post dopo
            if (entry.getCommentatoriRecenti().isEmpty() && entry.getLikeRecenti().isEmpty() && entry.getDislikeRecenti().isEmpty()) {
                entry.incrementaContatoreValutazioni();
                continue;
            }

            entry.incrementaContatoreValutazioni();
            numeroDiLike = entry.getLikeRecenti().size();
            numeroDiCommentatori = entry.getCommentatoriRecenti().size();
            numeroDiDislike = entry.getDislikeRecenti().size();
            differenza = numeroDiLike - numeroDiDislike;
            //scorro la lista dei commentatori recenti al post.
            for (Pair<String, Integer> entry1 : entry.getCommentatoriRecenti()) {
                //guadagno dai commenti
                if(!entry.getCommentatoriRecenti().isEmpty()){
                    //con entry1.getsecond() prendo il numero di volte che un utente ha commentato il post
                    guadagnoParzialeDaCommenti += Math.log((2 / (1 + (Math.exp(-(entry1.getSecond() - 1))))) + 1);
                }

            }
            //guadagno dai like
            guadagnoParzialeDaLike = Math.log(Math.max(differenza, 0) + 1);


            //guadagno totale
            guadagnoTotale = (guadagnoParzialeDaCommenti + guadagnoParzialeDaLike) / entry.getContatoreValutazioni();

            //all'autore del post va il 70% del guadagno totale
            guadagnoAutore = guadagnoTotale * 0.7;

            //ai curatori va il restante 30%. esso viene diviso per il numero dei curatori per spartire il totale
            guadagnoCuratori = (guadagnoTotale - guadagnoAutore) / (numeroDiLike + numeroDiCommentatori);

            //pago gli autori aggiungendo il loro guadagno al loro wallet
            for (Map.Entry<String, Wallet> entry2 : wallet.entrySet()) {
                if (entry2.getKey().equals(entry.getCreator())) {
                    entry2.getValue().addTransazione(guadagnoAutore);
                }

            }

            //pago i curatori che hanno messo like
            for (String userName : entry.getLikeRecenti()){
                for (Map.Entry<String, Wallet> entry1 : wallet.entrySet()) {
                    if (entry1.getKey().equals(userName)) {
                        entry1.getValue().addTransazione(guadagnoCuratori);
                    }

                }
            }

            //pago i curatori che hanno commentato
            for (Pair<String, Integer> userName : entry.getCommentatoriRecenti()){
                for (Map.Entry<String, Wallet> entry1 : wallet.entrySet()) {
                    if (entry1.getKey().equals(userName.getFirst())) {
                        entry1.getValue().addTransazione(guadagnoCuratori);
                    }

                }
            }
            //svuoto le liste per il ciclo di valutazione successiva
            entry.clearListLikeRecenti();
            entry.clearListdisLikeRecenti();
            entry.clearListCommentatoriRecenti();

        }




    }

}

