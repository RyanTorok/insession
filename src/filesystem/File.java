package filesystem;

public class File extends FileSystemElement {

    private String extension;

    public File(String name) {
        super(name);
    }

    public String getExtension() {
        return extension;
    }
}
