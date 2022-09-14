

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/*Da intendersi come la home di facebook*/
public class Home {
    private LinkedBlockingQueue<Post> listOfPost;
    public Home (){
        this.listOfPost = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<Post> getListOfPost() {
        return listOfPost;
    }
    //aggiunge un post al feed di un utente
    public void addPost(Post post) {
        this.listOfPost.add(post);
    }
    public void addListOfPost (ArrayList<Post> list){
        for(Post p : list){
            if(!listOfPost.contains(p)) {
                listOfPost.add(p);
            }
        }
    }
    //rimuove il post dal feed di un utente
    public void removePost(Post post) {
        this.listOfPost.remove(post);
    }
}
