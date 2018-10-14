package searchengine;

import net.PostRequest;
import net.ThreadedCall;
import terminal.Address;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Index implements Serializable {


    static final long serialVersionUID = 1000L;
    private HashMap<Identifier, Indexable> objects;

    public Index() {
        map = new HashMap<>();
        objects = new HashMap<>();
    }

    private HashMap<String, HashMap<Identifier, Integer>> map;

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
        HashMap<Identifier, Integer> wordSet = map.computeIfAbsent(word, k -> new HashMap<>());
        Integer existing = wordSet.get(id);
        if (existing == null) existing = 0;
        wordSet.put(id, existing + relevance);
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
                for (String s1 :
                        splitOnSpace) {
                    associate(s1, i.getUniqueIdentifier(), s.getRelevance());

                }
            }
        }
    }

    public HashMap<Identifier, Indexable> getObjects() {
        return objects;
    }

    public Indexable getObject(Identifier identifier) {
        return objects.get(identifier);
    }

}