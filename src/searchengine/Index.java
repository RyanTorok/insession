package searchengine;

import terminal.Address;

import java.io.*;
import java.util.*;

public class Index implements Serializable {


    static final long serialVersionUID = 1000L;
    private IndexableCache objects;
    private HashMap<String, HashMap<Identifier, Integer>> map;

    public Index() {
        map = new HashMap<>();
        objects = new IndexableCache();
    }


    public static Index loadLocal() {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(new File(Address.fromRootAddr("resources", "index.ser"))));
            return (Index) stream.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return new Index();
        }
    }

    public void write() {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(new File(Address.fromRootAddr("resources", "index.ser"))));
            stream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void associate(String word, Identifier id, int relevance) {
        HashMap<Identifier, Integer> wordSet = map.computeIfAbsent(word.toLowerCase(), k -> new HashMap<>());
        Integer existing = wordSet.get(id);
        if (existing == null) existing = 0;
        wordSet.put(id, existing + relevance);
    }

    // relevance == 0 means complete dissociation, > 0 means subtract that value from existing relevance
    public void dissociate(String word, Identifier id, int relevance) {
        HashMap<Identifier, Integer> wordSet = map.get(word.toLowerCase());
        if (wordSet == null)
            return;
        Integer existing = wordSet.get(id);
        if (existing == null)
            return;
        if (relevance == 0 || existing - relevance <= 0)
            wordSet.remove(id);
        else
            wordSet.put(id, existing - relevance) ;
    }

    public void remove(Indexable item) {
        item.getIndexTextSets().forEach(rankedString -> dissociate(rankedString.getString(), item.getUniqueIdentifier(), rankedString.getRelevance()));
        objects.hardRemove(item.getUniqueIdentifier());
    }

    HashSet<ItemNode> getItems(String key) {
        HashMap<Identifier, Integer> wordSet = map.get(key);
        if (wordSet == null)
            return new HashSet<>();
        HashSet<ItemNode> nodes = new HashSet<>();
        wordSet.forEach((key1, value) -> nodes.add(new ItemNode(key1, value)));
        return nodes;
    }

    public int find(String key, Identifier identifier) {
        HashMap<Identifier, Integer> wordSet = map.get(key);
        if (wordSet == null)
            return 0;
        Integer existing = wordSet.get(identifier);
        if (existing == null)
            return 0;
        return existing;
    }

    public void index(Collection<? extends Indexable> list) {
        for (Indexable i : list) {
            for (RankedString s : i.getIndexTextSets()) {
                String[] splitOnSpace = s.getString().split("\\s+");
                for (String s1 : splitOnSpace) {
                    associate(s1, i.getUniqueIdentifier(), s.getRelevance());
                }
            }
            objects.put(i.getUniqueIdentifier(), i);
        }
    }

    public Indexable getObject(Identifier identifier) {
        return objects.get(identifier);
    }

    public void index(Indexable i) {
        index(Collections.singletonList(i));
    }
}