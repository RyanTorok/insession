package searchengine;

import classes.ClassPd;
import classes.Post;
import main.Administrator;
import main.Student;
import main.Teacher;
import net.PostRequest;
import net.ThreadedCall;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

public class IndexableCache {

    public IndexableCache() {
        cache = new HashMap<>();
    }

    public void put(Identifier uniqueIdentifier, Indexable i) {

    }

    public void hardRemove(Identifier uniqueIdentifier) {

    }

    private enum Status {
        MODIFIED, OWNED, EXCLUSIVE, SHARED, INVALID
    }

    private HashMap<Identifier, Indexable> cache;

    public <T extends Indexable> T get(Identifier id) {
        Indexable indexable = cache.get(id);
        try {
            if (indexable == null)
                return (T) cacheMiss(id);
            return (T) indexable;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Wrong class type for requested id - found " + indexable.getClass().getName());
        }
    }

    private Indexable cacheMiss(Identifier id) {
        return null;
    }

    private void pageOut(Indexable i) {
        new ThreadedCall<>("objects/pageOut.php", true, new PostRequest("json", i.toJSONObject().toString())).procedureCall((outputList) -> null, (o) -> {});
    }

}
