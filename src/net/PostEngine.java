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
        getPosts().remove(post);
        Root.getPortal().getSearchBox().getEngine().getIndex().remove(post);
        //remove from server
        new ThreadedCall<Boolean>("post/deletePost.php", true,
                new PostRequest("classId", post.getClassId()),
                new PostRequest("postId", post.getIdentifier().getId()),
                new PostRequest("classItemId", post.getClassItemId())).procedureCall((list) ->
                list.contains("done"), (b) -> {}
        );

    }

    public void addPost(Post newPost) {
        posts.add(newPost);
    }
}
