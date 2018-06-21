package searchengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Cache {

    HashMap<String, ArrayList<Identifier>> cache;
    private int entrySize;

    public void save(String query, ArrayList<Identifier> results) {
        cache.put(query, results);
    }

    public List<Identifier> get(String query, int count) {
        ArrayList<Identifier> data = cache.get(query);
        if (data == null) return new ArrayList<Identifier>();
        return data.subList(0, Math.min(count, entrySize));
    }

    public Cache(int entrySize) {
        cache = new HashMap<>();
        this.entrySize = entrySize;
    }

    public Cache() {
        this(100);
    }
}
