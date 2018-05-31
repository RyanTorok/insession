package filesystem;

public class Directory extends FileSystemElement {

    public String address;


    public Directory(String name) {
        super(name);
    }

    public FileSystemElement addSubElement(FileSystemElement element) {
        this.getChildren().add(element);
        element.getParents().add(this);
        return element;
    }
}
