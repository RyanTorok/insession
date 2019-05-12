package net;

import classes.ClassPd;
import classes.Post;
import main.Root;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostEngine implements Serializable {

    static final long serialVersionUID = 98L;

    private long lastUpdate;
    private ClassPd belongsTo;
    private List<Post> posts;
    private List<Post> displayedPosts;

    public PostEngine(ClassPd belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void update() {

    }

    public void index() {
    }

    public List<Post> getPosts() {
        if (posts == null) {
            posts = new ArrayList<>();
        }
        return posts;
    }

    public ClassPd getBelongsTo() {
        return belongsTo;
    }

    public List<Post> getDisplayedPosts() {
        return displayedPosts;
    }

    public void setDisplayedPosts(ArrayList<Post> displayedPosts) {
        this.displayedPosts = displayedPosts;
    }

    public void deletePost(Post post) {
        try (ServerSession serverSession = new ServerSession()) {
            serverSession.open();
            serverSession.sendOnly("deletepost", String.valueOf(post.getIdentifier().getId()));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO tell user the delete failed
            return;
        }
        getPosts().remove(post);
        Root.getPortal().getSearchBox().getEngine().getIndex().remove(post);
        Root.getPortal().getSearchIndex().remove(post);
        //remove from server

    }

    public void addPost(Post newPost) {
        posts.add(newPost);
    }
}
