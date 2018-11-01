package searchengine;

import classes.ClassPd;

import java.util.Objects;
import java.util.UUID;

public class Identifier implements Comparable {
    private String name;
    private String description;
    private Type type;
    private ClassPd belongsTo;
    private UUID id;
    private String authorName; //first and last
    private long time1; //date of posting
    private long time2; //due date for class assignments
    private long views; //used only for modules and posts
    private long likes; //used only for modules and posts

    public Identifier(String name, Type type, UUID id) {
        this.setName(name);
        this.setType(type);
        this.setId(id);
    }

    public Identifier(String name, Type type, long id) {
        this(name, type, new UUID(0, id));
    }

    public Indexable find(Index index) {
        return index.getObject(this);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public long getIdLSB() {
        return id.getLeastSignificantBits();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = new UUID(0, id);
    }

    public ClassPd getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(ClassPd belongsTo) {
        this.belongsTo = belongsTo;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getTime1() {
        return time1;
    }

    public void setTime1(long time1) {
        this.time1 = time1;
    }

    public long getTime2() {
        return time2;
    }

    public void setTime2(long time2) {
        this.time2 = time2;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum Type {
        Module, Class, Organization, People, Class_Item, Post, Utility, Setting
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Identifier))
            return 0;
        return getName().compareTo(((Identifier) o).getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Identifier))
            return false;
        return getId() == ((Identifier) obj).getId() && getType() == ((Identifier) obj).getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
