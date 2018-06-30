package searchengine;

import java.sql.Timestamp;

public interface Indexable {
    Timestamp lastIndexed();
    String[] getIndexTextSets();

    String getUniqueIdentifier();

    default boolean containsString(String word) {
        for (String s: getIndexTextSets()) {
            if (s.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
