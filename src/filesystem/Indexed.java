package filesystem;

import java.util.ArrayList;

public interface Indexed {

    ArrayList<Directory> getParents();
    ArrayList<FileSystemElement> getChildren();
    String ls();

}
