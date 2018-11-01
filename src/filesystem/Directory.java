package filesystem;

import org.json.JSONObject;
import searchengine.Identifier;
import searchengine.Indexable;
import searchengine.RankedString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Directory extends FileSystemElement {

    public String address;
    public List<FileSystemElement> contents;

    public Directory(String name) {
        super(name);
    }

    @Override
    public List<Identifier> grep() {
        ArrayList<Identifier> combined = new ArrayList<>();
        contents.forEach(child -> combined.addAll(child.grep()));
        return combined;
    }

    public FileSystemElement addSubElement(FileSystemElement element) {
        this.getChildren().add(element);
        element.getParents().add(this);
        return element;
    }

    @Override
    public List<RankedString> getIndexTextSets() {
        return Collections.singletonList(new RankedString(getName(), Indexable.TITLE_RELEVANCE));
    }

    @Override
    public Identifier getUniqueIdentifier() {
        return null;
    }

    @Override
    public void launch() {

    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }
}
