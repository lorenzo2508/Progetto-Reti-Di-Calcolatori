

/*Da intendersi come un profilo di facebook*/

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;


public class Blog {
    private LinkedBlockingQueue<Post> listaDeiPostSulBlog;

    public Blog() {
        this.listaDeiPostSulBlog = new LinkedBlockingQueue<>();
    }
    //ritorna la lista dei post sul blog
    public LinkedBlockingQueue<Post> getListaDeiPostSulBlog() {
        return listaDeiPostSulBlog;
    }
    //elimina un post sul blog dato l'id
    public void removePostFromBlog (Integer idPost){
        listaDeiPostSulBlog.removeIf(post -> Objects.equals(post.getIdPost(), idPost));
    }
}
