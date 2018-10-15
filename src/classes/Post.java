package classes;

import gui.CompressedRichText;
import gui.HTMLText;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import main.User;
import main.UtilAndConstants;
import searchengine.Identifier;
import searchengine.Indexable;
import searchengine.RankedString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Post implements Indexable, Serializable, Comparable<Post> {

    static final long serialVersionUID = 99L;
    private long postId;
    private long classId;
    private long classItemId;
    private long posterId;
    private String posterFirst;
    private String posterLast;
    private String posterUsername;
    private Type type;
    private long likes;
    private long views;
    private String title;
    private HTMLText formattedText;
    private long lastIndexed;
    private long created;
    private long modified;
    private Identifier identifier;
    private boolean posterNameVisible;
    private long visibleTo = -1; //-1: private to instructor, 0: entire class, >= 1: group with this id only
    private long parentId;
    private boolean currentUserLikedThis;
    private boolean currentUserViewedThis;
    private List<PostStatus> statusLabels;
    private boolean pinned;
    private List<Post> studentAnswers;
    private List<Post> comments;
    private Post instructorAnswer;
    private HashMap<Long, Pair<String, CompressedRichText>> history;

    public Post(User postedBy, Type type, String title, String source, boolean posterNameVisible) {
        this.setType(type);
        this.setTitle(title);
        formattedText = new HTMLText(source);
        this.posterId = Long.parseLong(postedBy.getID());
        this.posterFirst = postedBy.getFirst();
        this.posterLast = postedBy.getLast();
        this.posterUsername = postedBy.getUsername();
        this.setPosterNameVisible(posterNameVisible);
        setLikes(0);
        setViews(0);
        setLastIndexed(1);
        setIdentifier(new Identifier(title, Identifier.Type.Post, -1));
        identifier.setAuthorName(posterNameVisible ? posterFirst + " " + posterLast : "Anonymous");
        statusLabels = new ArrayList<>();
        studentAnswers = new ArrayList<>();
        comments = new ArrayList<>();
        history = new HashMap<>();
        history.put(System.currentTimeMillis(), new Pair<>(title, new CompressedRichText(new TextFlow(new Text(source)))));
    }

    public Post(long postId, long classId, long classItemId, long posterId, String posterFirst, String posterLast, String posterUsername, Type type, long likes, boolean currentUserLikedThis, long views, boolean currentUserViewedThis, String title, String source, long lastIndexed, long created, long modified, boolean posterNameVisible, long visibleTo, long parentId) {
        this.postId = postId;
        this.classId = classId;
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
        this.formattedText = new HTMLText(source);
        this.lastIndexed = lastIndexed;
        this.created = created;
        this.modified = modified;
        this.posterNameVisible = posterNameVisible;
        this.visibleTo = visibleTo;
        this.parentId = parentId;
        statusLabels = new ArrayList<>();
        studentAnswers = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public static Post newPost() {
        return new Post(User.active(), Type.Question, "", "", true);
    }

    public static Post fromEncoding(String encoding) {
        String[] split = UtilAndConstants.parsePHPDataOutBase64(encoding, 19);
        Long postId = Long.parseLong(split[0]);
        Long classId = Long.parseLong(split[1]);
        Long classItemId = Long.parseLong(split[2]);
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
        return new Post(postId, classId, classItemId, userId, posterFirst, posterLast, posterUsername, type, likes,
                liked, views, viewed, title, text, 1, created, modified, nameVisibility, visibility, parent);
    }

    @Override
    public Timestamp lastIndexed() {
        return new Timestamp(getLastIndexed());
    }

    @Override
    public List<RankedString> getIndexTextSets() {
        ArrayList<RankedString> strings = new ArrayList<>();
        strings.add(new RankedString("#" + Long.toString(identifier.getId()), HEADER_RELEVANCE));
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
        //TODO launch post interface
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
        return formattedText.getUnformatted();
    }

    public void setText(String text) {
        formattedText.setUnformatted(text);
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

    public List<PostStatus> getStatusLabels() {
        return statusLabels;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public long getClassItemId() {
        return classItemId;
    }

    public void setClassItemId(long classItemId) {
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

    public void setStatusLabels(List<PostStatus> statusLabels) {
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
        return formattedText.getSource(); //TODO support encoding of symbols
    }

    public HTMLText getFormattedText() {
        return formattedText;
    }

    public void setFormattedText(HTMLText formattedText) {
        this.formattedText = formattedText;
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
        history.put(System.currentTimeMillis(), new Pair<>(newTitle, newCRT));
    }
}

