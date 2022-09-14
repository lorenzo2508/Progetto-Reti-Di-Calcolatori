

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ListenerForNotification implements Runnable {
    private String indirizzoMulticast = null;
    private MulticastSocket multicastSocket = null;
    private int porta;
    private boolean flagControlloLogin;
    public ListenerForNotification(String indirizzo, int porta, boolean flagControlloLogin){
        this.indirizzoMulticast = indirizzo;
        this.porta = porta;
        this.flagControlloLogin = flagControlloLogin;
    }
    @Override
    //attende per il messaggio mandato dal server e stampa a video il messaggio del server
    public void run() {
        try {
            multicastSocket = new MulticastSocket(porta);
            multicastSocket.joinGroup(InetAddress.getByName(indirizzoMulticast));
            System.out.println("listener multicast avviato");

            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                multicastSocket.receive(datagramPacket);
                String pacchettoRicevuto = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                if(flagControlloLogin==true){
                    System.out.println(pacchettoRicevuto);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
