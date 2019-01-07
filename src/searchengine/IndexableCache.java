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
        return new ThreadedCall<Indexable>("objects/getObject.php", true, new PostRequest("id=", id.getId().toString())).returnValueCall((list) -> {
            JSONObject result = new JSONObject(list.get(0));
            String type = result.getString("type");
            try {
                switch (type) {
                    case "module":
                        break;
                    case "class":
                        break;
                    case "organization":
                        break;
                    case "people":
                        switch (result.getString("userType")) {
                            case "student":
                                return new Student(null, result.getString("username"), null, result.getString("first"), result.getString("middle"), result.getString("last"), result.getString("email"), new Timestamp(result.getLong("timestamp")), result.getString("studentId"), result.getInt("grade"));
                            case "teacher":
                                return new Teacher(null, result.getString("username"), null, result.getString("first"), result.getString("middle"), result.getString("last"), result.getString("email"), result.getString("eid"), new Timestamp(result.getLong("timestamp")));
                            case "administrator":
                                return new Administrator(null, result.getString("username"), null, result.getString("first"), result.getString("middle"), result.getString("last"), result.getString("email"), result.getString("eid"), new Timestamp(result.getLong("timestamp")));
                        }
                        break;
                    case "class_item":
                        break;
                    case "post":
                        return new Post(UUID.fromString(result.getString("id")),
                                ClassPd.fromId(UUID.fromString(result.getString("classId"))),
                                UUID.fromString(result.getString("classItemId")),
                                result.getLong("userId"), result.getString("posterFirst"),
                                result.getString("posterLast"),
                                result.getString("posterUsername"),
                                Post.Type.valueOf(result.getString("type")),
                                result.getLong("likes"),
                                result.getBoolean("currentUserLikedThis"),
                                result.getLong("views"),
                                result.getBoolean("currentUserViewedThis"),
                                result.getString("title"),
                                result.getString("source"),
                                result.getLong("timestamp"),
                                result.getLong("created"),
                                result.getLong("modified"),
                                result.getBoolean("posterNameVisible"),
                                result.getLong("visibleTo"),
                                result.getLong("parentId"));
                    case "utility":
                        break;
                    case "setting":
                        break;
                    default: throw new IllegalArgumentException("Missing or invalid type field in JSON object : " + type);
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        });

    }

    private void pageOut(Indexable i) {
        new ThreadedCall<>("objects/pageOut.php", true, new PostRequest("json", i.toJSONObject().toString())).procedureCall((outputList) -> null, (o) -> {});
    }

}
