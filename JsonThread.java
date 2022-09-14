

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonThread implements Runnable{
    public ConcurrentHashMap<User, Blog> usersProfile;
    private ConcurrentHashMap<String, Wallet> wallet;

    public JsonThread(ConcurrentHashMap<User, Blog> usersProfile, ConcurrentHashMap<String, Wallet> wallet){
        this.usersProfile = usersProfile;
        this.wallet = wallet;
    }
    public void run(){
        while (true) {
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try{
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writerUserProfile = new FileWriter("jsonFile"+ File.separator +"userProfile.json" );
                FileWriter writerPost = new FileWriter("jsonFile"+ File.separator +"post.json");
                FileWriter writerWallet = new FileWriter("jsonFile" + File.separator +"wallet.json") ;
                StringBuilder JsonProfile = new StringBuilder("[");
                int cont = 0;
                //scrivo i profili sul file json per i profili
                for (Map.Entry<User, Blog> entry : usersProfile.entrySet()){
                    String jsonString = gson.toJson(entry.getKey(), User.class);
                    if(cont < usersProfile.size() - 1) {
                        JsonProfile.append(jsonString).append(",");
                        cont ++;
                    }
                    else
                        JsonProfile.append(jsonString);

                }
                JsonProfile.append("]");
                String writeOnfileProfile = JsonProfile.toString();
                writerUserProfile.write(writeOnfileProfile);
                writerUserProfile.flush();
                writerUserProfile.close();
                //scrivo i post sul file json per i post
                StringBuilder JsonPost = new StringBuilder();
                JsonPost.append("[");
                cont = 0;
                for (Map.Entry<User, Blog> entry : usersProfile.entrySet()){
                    if(entry.getKey().getListPost().isEmpty()){
                        continue;
                    }
                    String jsonString = gson.toJson(entry.getKey().getListPost());
                    jsonString = jsonString.substring(1, jsonString.length() - 1);
                    if(cont < usersProfile.size() - 1) {
                        JsonPost.append(jsonString).append(",");
                        cont ++;
                    }

                    else
                        JsonPost.append(jsonString);

                }
                JsonPost.append("]");
                String writeOnfilePost = JsonPost.toString();

                //controllo che la stringa sia formattata bene prima di scriverla su file

                if(!writeOnfilePost.equals("[,]")) {
                    if(writeOnfilePost.length() > 2){
                        writeOnfilePost = "[" + writeOnfilePost.substring(2, writeOnfilePost.length()-2) + "]";
                    }
                    if(writeOnfilePost.equals("[,]")){
                        writeOnfilePost = "[]";
                    }
                    writerPost.write(writeOnfilePost);
                    writerPost.flush();
                    writerPost.close();
                }


                //scrivo il wallet sul file json per il wallet
                gson.toJson(wallet, writerWallet);
                writerWallet.flush();
                writerWallet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
