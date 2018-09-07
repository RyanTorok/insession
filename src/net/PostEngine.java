package net;

import classes.ClassPd;
import classes.Post;
import main.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static net.Net.root;
import static net.Net.urlEncode;

public class PostEngine implements Serializable {

    static final long serialVersionUID = 98L;

    private long lastUpdate;
    private ClassPd belongsTo;
    private ArrayList<Post> posts;
    private ArrayList<Post> displayedPosts;

    public PostEngine(ClassPd belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void update() {
        try {
            //post data
            String data = "id=" + User.active().getID() + "&username="+ User.active().getUsername() + "&password=" + urlEncode(User.active().getPassword()) + "&classId=" + belongsTo.getUniqueId();
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            //start connection
            URL url = new URL(root(), "post/getPosts.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }
            BufferedReader readDone = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String encoding = readDone.readLine();
            ArrayList<Post> newList = new ArrayList<>();
            while (encoding != null) {
                newList.add(Post.fromEncoding(encoding));
                encoding = readDone.readLine();
            }
            posts = newList;
        } catch (IOException e) {
        }
    }

    public void index() {
    }

    public ArrayList<Post> getPosts() {
        if (posts == null) {
            posts = new ArrayList<>();
        }
        return posts;
    }

    public ClassPd getBelongsTo() {
        return belongsTo;
    }

    public ArrayList<Post> getDisplayedPosts() {
        return displayedPosts;
    }

    public void setDisplayedPosts(ArrayList<Post> displayedPosts) {
        this.displayedPosts = displayedPosts;
    }

}
