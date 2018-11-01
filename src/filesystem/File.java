package filesystem;

import org.json.JSONObject;
import searchengine.Identifier;

import java.util.Collections;
import java.util.List;

public abstract class File extends FileSystemElement {

    private String extension;

    public File(String name) {
        super(name);
    }

    @Override
    public List<Identifier> grep() {
        return Collections.singletonList(getUniqueIdentifier());
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }
}
