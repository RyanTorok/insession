package searchengine;

import java.sql.Timestamp;
import java.util.List;

public interface Indexable {

    int TITLE_RELEVANCE = 5;
    int HEADER_RELEVANCE = 3;
    int TEXT_RELEVANCE = 1;

    Timestamp lastIndexed();
    List<RankedString> getIndexTextSets();

    Identifier getUniqueIdentifier();

    default boolean containsString(String word) {
        for (RankedString s: getIndexTextSets()) {
            if (s.getString().toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    void launch();
}
