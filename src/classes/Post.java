package classes;

import gui.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import main.Root;
import main.User;
import org.json.JSONObject;
import searchengine.Identifier;
import searchengine.Index;
import searchengine.Indexable;
import searchengine.RankedString;
import server.IDAllocator;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

public class Post implements Indexable, Serializable, Comparable<Post> {

    static final long serialVersionUID = 99L;
    private final ClassPd classPd;
    private CompressedRichText formattedText;
    private UUID classId;
    private UUID classItemId;
    private long posterId;
    private String posterFirst;
    private String posterLast;
    private String posterUsername;
    private Type type;
    private long likes;
    private long views;
    private String title;
    private long lastIndexed;
    private long created;
    private long modified;
    private Identifier identifier;
    private boolean posterNameVisible;
    private long visibleTo = -1; //-1: private to instructor, 0: entire class, >= 1: group with this id only
    private long parentId;
    private boolean currentUserLikedThis;
    private boolean currentUserViewedThis;
    private HashSet<PostStatus> statusLabels;
    private boolean pinned;
    private List<Post> studentAnswers;
    private List<Post> comments;
    private Post instructorAnswer;
    private HashMap<Long, Pair<String, CompressedRichText>> history;

    public Post(User postedBy, Type type, String title, String source, boolean posterNameVisible, ClassPd classPd) {
        this.classPd = classPd;
        classId = classPd.getUniqueId();
        this.setType(type);
        this.setTitle(title);
        this.posterId = postedBy.getUniqueID();
        this.posterFirst = postedBy.getFirst();
        this.posterLast = postedBy.getLast();
        this.posterUsername = postedBy.getUsername();
        this.setPosterNameVisible(posterNameVisible);
        setLikes(0);
        setViews(0);
        setLastIndexed(1);
        setIdentifier(new Identifier(title, Identifier.Type.Post, IDAllocator.get()));
        identifier.setAuthorName(posterNameVisible ? posterFirst + " " + posterLast : "Anonymous");
        statusLabels = new HashSet<>();
        studentAnswers = new ArrayList<>();
        comments = new ArrayList<>();
        history = new HashMap<>();
        history.put(System.currentTimeMillis(), new Pair<>(title, new CompressedRichText(new TextFlow(new Text(source)))));
        formattedText = new CompressedRichText(new TextFlow(new Text(source)));
        classItemId = new UUID(0, 0);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.append("type", "post");
        object.append("id", identifier.getId());
        object.append("classId", classId);
        object.append("classItemId", classItemId);
        object.append("posterFirst", posterFirst);
        object.append("posterLast", posterLast);
        object.append("posterUsername", posterUsername);
        object.append("type", type);
        object.append("likes", likes);
        object.append("views", views);
        object.append("title", title);
        object.append("text", formattedText.toJSONObject());
        object.append("created", created);
        object.append("modified", modified);
        object.append("posterNameVisible", posterNameVisible);
        object.append("visibleTo", visibleTo);
        object.append("parentId", parentId);
        object.append("currentUserLikedThis", currentUserLikedThis);
        object.append("currentUserViewedThis", currentUserViewedThis);
        object.append("pinned", pinned);
        object.append("history", history.toString());
        return object;
    }

    public Post(UUID postId, ClassPd classPd, UUID classItemId, long posterId, String posterFirst, String posterLast, String posterUsername, Type type, long likes, boolean currentUserLikedThis, long views, boolean currentUserViewedThis, String title, String source, long lastIndexed, long created, long modified, boolean posterNameVisible, long visibleTo, long parentId) {
        this.classPd = classPd;
        this.classId = classPd.getUniqueId();
        this.classItemId = classItemId;
        this.posterId = posterId;
        this.posterFirst = posterFirst;
        this.posterLast = posterLast;
        this.posterUsername = posterUsername;
        this.type = type;
        this.likes = likes;
        this.currentUserLikedThis = currentUserLikedThis;
        this.views = views;
        this.currentUserViewedThis = currentUserViewedThis;
        this.title = title;
        this.formattedText = new CompressedRichText(new TextFlow(new Text(source)));
        this.lastIndexed = lastIndexed;
        this.created = created;
        this.modified = modified;
        this.posterNameVisible = posterNameVisible;
        this.visibleTo = visibleTo;
        this.parentId = parentId;
        statusLabels = new HashSet<>();
        studentAnswers = new ArrayList<>();
        comments = new ArrayList<>();
        this.identifier = new Identifier(title, Identifier.Type.Post, postId);
    }

    public static Post newPost(ClassPd classPd) {
        Post post = new Post(User.active(), Type.Question, "", "", true, classPd);
        return post;
    }

    /*public static Post fromEncoding(String encoding) {
        String[] split = UtilAndConstants.parsePHPDataOutBase64(encoding, 19);
        UUID postId = UUID.fromString(split[0]);
        UUID classId = UUID.fromString(split[1]);
        UUID classItemId = UUID.fromString(split[2]);
        Long userId = Long.parseLong(split[3]);
        String title = split[4];
        String text = split[5];
        Long visibility = Long.parseLong(split[6]);
        boolean nameVisibility = Boolean.getBoolean(split[7]);
        Type type = Type.valueOf(split[8]);
        Long parent = Long.parseLong(split[9]);
        Long created = Long.parseLong(split[10]);
        Long modified = Long.parseLong(split[11]);
        Long likes = Long.parseLong(split[12]);
        boolean liked = Boolean.getBoolean(split[13]);
        Long views = Long.parseLong(split[14]);
        boolean viewed = Boolean.getBoolean(split[15]);
        String posterFirst = split[16];
        String posterLast = split[17];
        String posterUsername = split[18];
        return new Post(postId, ClassPd.fromId(classId), classItemId, userId, posterFirst, posterLast, posterUsername, type, likes,
                liked, views, viewed, title, text, 1, created, modified, nameVisibility, visibility, parent);
    }*/

    @Override
    public Timestamp lastIndexed() {
        return new Timestamp(getLastIndexed());
    }

    @Override
    public List<RankedString> getIndexTextSets() {
        ArrayList<RankedString> strings = new ArrayList<>();
        strings.add(new RankedString("#" + identifier.getId().toString(), HEADER_RELEVANCE));
        strings.add(new RankedString(getTitle(), TITLE_RELEVANCE));
        strings.add(new RankedString(posterFirst + " " + posterLast, HEADER_RELEVANCE));
        strings.add(new RankedString(getText(), TEXT_RELEVANCE));
        return strings;
    }

    @Override
    public Identifier getUniqueIdentifier() {
        return getIdentifier();
    }

    @Override
    public void launch() {
        ClassView active = Root.getPortal().launchClass(classPd, (classView) -> {
            ((PostsBody) classView.getBodyPanes()[0]).fire(this);
        });
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return formattedText.getUnformattedText();
    }


    public long getLastIndexed() {
        return lastIndexed;
    }

    public void setLastIndexed(long lastIndexed) {
        this.lastIndexed = lastIndexed;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public boolean isPosterNameVisible() {
        return posterNameVisible;
    }

    public void setPosterNameVisible(boolean posterNameVisible) {
        this.posterNameVisible = posterNameVisible;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public HashSet<PostStatus> getStatusLabels() {
        return statusLabels;
    }

    public UUID getClassId() {
        return classId;
    }

    public void setClassId(UUID classId) {
        this.classId = classId;
    }

    public UUID getClassItemId() {
        return classItemId;
    }

    public void setClassItemId(UUID classItemId) {
        this.classItemId = classItemId;
    }

    public long getPosterId() {
        return posterId;
    }

    public void setPosterId(long posterId) {
        this.posterId = posterId;
    }

    public long getVisibleTo() {
        return visibleTo;
    }

    public void setVisibleTo(long visibleTo) {
        this.visibleTo = visibleTo;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public boolean isCurrentUserLikedThis() {
        return currentUserLikedThis;
    }

    public void setCurrentUserLikedThis(boolean currentUserLikedThis) {
        this.currentUserLikedThis = currentUserLikedThis;
    }

    public boolean isCurrentUserViewedThis() {
        return currentUserViewedThis;
    }

    public void setCurrentUserViewedThis(boolean currentUserViewedThis) {
        this.currentUserViewedThis = currentUserViewedThis;
    }

    public void setStatusLabels(HashSet<PostStatus> statusLabels) {
        this.statusLabels = statusLabels;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public int compareTo(Post o) {
        if (o.isPinned() == isPinned())
            return Long.compare(identifier.getTime1(), o.identifier.getTime1());
        return Boolean.compare(isPinned(), o.isPinned());
    }

    public List<Post> getStudentAnswers() {
        return studentAnswers;
    }

    public Post getInstructorAnswer() {
        return instructorAnswer;
    }

    public String getSource() {
        return formattedText.getUnformattedText();
    }

    public CompressedRichText getFormattedText() {
        return formattedText;
    }

    public String getPosterUsername() {
        return posterUsername;
    }

    public void setPosterUsername(String posterUsername) {
        this.posterUsername = posterUsername;
    }

    public List<Post> getComments() {
        return comments;
    }

    public void like() {
        if (isCurrentUserLikedThis())
            return;
        setCurrentUserLikedThis(true);
        setLikes(getLikes() + 1);
        if (ClassPd.fromId(classId) != null && (ClassPd.fromId(classId).getTeacher() != null && ClassPd.fromId(classId).getTeacher().equals(User.active())))
            getStatusLabels().add(PostStatus.ENDORSED);
    }

    public void unlike() {
        if (!isCurrentUserLikedThis())
            return;
        setCurrentUserLikedThis(false);
        setLikes(getLikes() - 1);
        if (ClassPd.fromId(classId) != null && (ClassPd.fromId(classId).getTeacher() != null && ClassPd.fromId(classId).getTeacher().equals(User.active())))
            getStatusLabels().remove(PostStatus.ENDORSED);
    }

    public enum Type {
        Question, Student_Answer, Instructor_Answer, Note, Follow_Up
    }

    public String collapseText(int maxLength) {
        String modified = getText().replaceAll("\n", "  ");
        if (modified.length() <= maxLength)
            return modified;
        String max = modified.substring(0, maxLength - 3);
        for (int i = maxLength - 4;  i > maxLength - 16;  i--) {
            if (Character.isSpaceChar(max.charAt(i))) {
                return max.substring(0, i) + "...";
            }
        }
        return max.substring(0, maxLength - 4) + "...";
    }

    public void answer(Post child, boolean instructor) {
        if (instructor) instructorAnswer = child;
        else studentAnswers.add(child);
    }

    public void update(String newTitle, CompressedRichText newCRT) {
        Index index = Root.getPortal().getSearchBox().getEngine().getIndex();
        //TODO remove this line if we want history text to be searchable
        index.remove(this);
        statusLabels.add(PostStatus.UPDATED);
        long timeMillis = System.currentTimeMillis();
        history.put(timeMillis, new Pair<>(newTitle, newCRT));
        setFormattedText(newCRT);
        identifier.setTime2(timeMillis);
        index.index(this);
    }

    public void setFormattedText(CompressedRichText formattedText) {
        this.formattedText = formattedText;
    }
}

