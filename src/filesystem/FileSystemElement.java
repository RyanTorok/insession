package filesystem;

import classes.ClassItem;
import org.json.JSONObject;
import searchengine.Identifier;
import searchengine.Indexable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class FileSystemElement extends ClassItem implements Indexed, Indexable, Comparable<FileSystemElement> {

    private ArrayList<Directory> parents;
    private ArrayList<FileSystemElement> children;
    private String name;
    private Date dateModified;
    private Timestamp lastIndexed;

    public FileSystemElement(String name) {
        this.setName(name);
        lastIndexed = new Timestamp(1);
    }

    public void setParents(ArrayList<Directory> parents) {
        this.parents = parents;
    }

    @Override
    public ArrayList<FileSystemElement> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<FileSystemElement> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    @Override
    public ArrayList<Directory> getParents() {
        return parents;
    }

    @Override
    public String ls() {
        StringBuilder ls = new StringBuilder();
        for (FileSystemElement element : children) {
            ls.append(element.getNameWithExtension()).append(" ");
        }
        return ls.toString().trim();
    }

    private String getNameWithExtension() {
        if (this instanceof Directory)
            return name;
        return getName() + "." + ((File) this).getExtension();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void open() {
        //TODO
    }

    public FileSystemElement clone() {
        return null;
        //TODO
    }

    @Override
    public int compareTo(FileSystemElement o) { //used for resolving ambiguous file addresses during move/copy actions due to elements with multiple parents
        return dateModified.compareTo(o.getDateModified());
    }

    public Date getDateModified() {
        return dateModified;
    }

    public abstract List<Identifier> grep();

    @Override
    public Timestamp lastIndexed() {
        return lastIndexed;
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }

    public void setLastIndexed(Timestamp lastIndexed) {
        this.lastIndexed = lastIndexed;
    }

    public void setLastIndexedAsNow() {
        setLastIndexed(new Timestamp(System.currentTimeMillis()));
    }
}
