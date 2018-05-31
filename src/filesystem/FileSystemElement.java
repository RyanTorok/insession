package filesystem;

import java.util.ArrayList;
import java.util.Date;

public abstract class FileSystemElement implements Indexed, Comparable<FileSystemElement> {

    private ArrayList<Directory> parents;
    private ArrayList<FileSystemElement> children;
    private String name;
    private Date dateModified;

    public FileSystemElement(String name) {
        this.setName(name);
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
        String ls = "";
        for (FileSystemElement element : children) {
            ls += element.getNameWithExtension() + " ";
        }
        return ls.trim();
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
}
