package searchengine;

import java.sql.Timestamp;

public interface Indexable {
    Timestamp lastIndexed();
    String[] getIndexTextSets();

    String getUniqueIdentifier();
}
