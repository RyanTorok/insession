package searchengine;

import terminal.Address;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class WeightedPredictor {

    private Node root;
    private int weight_depth = 1;

    private WeightedPredictor() {
        root = new Node("", null);
    }

    public static WeightedPredictor read() {
        File f = new File(Address.fromRootAddr("resources", "stemIndex.ser"));
        if (!f.exists())
            try {
                return new WeightedPredictor() {{
                    initialize(Address.fromRootAddr("resources", "dictionary.txt"));
                }};
            } catch (Exception e) {
                return new WeightedPredictor();
            }
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
            return ((WeightedPredictor) stream.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return new WeightedPredictor();
        }
    }

    void initialize(String filename) {
        root.stemIndex.initialize(filename);
    }

    public void write() {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(new File(Address.fromRootAddr("resources", "stemIndex.ser"))));
            stream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void associate(String query) {
        root.put(new LinkedList<>(Arrays.asList(query.split("\\s+"))));
    }

    public List<String> predict(String query, int n) {
        return predict(new LinkedList<>(Arrays.asList(query.split("\\s+"))), n).stream().map(Trie::getStem).collect(Collectors.toList());
    }
    
    private TreeSet<Trie> predict(LinkedList<String> terms, int n) {
        TreeSet<Trie> returnVal = new TreeSet<>();
        String stem = terms.removeLast();
        Node node = root.findMostSpecific(terms);
        // begins with the highest seed value for random writing, which is the farthest down the tree possible.
        // We percolate up (i.e. look at fewer words as the basis until we reach the root or the specified number of returns)
        while (returnVal.size() < n && node != null) {
            List<Trie> tries = node.stemIndex.findStemDescending(stem, n - returnVal.size());
            returnVal.addAll(tries);
            node = node.parent;
        }
        return returnVal;
    }

    public Node getRoot() {
        return root;
    }

    public class Node {
        private String stem;
        private HashMap<String, Node> children;
        private StemIndex stemIndex;
        private Node parent;

        Node(String stem, Node parent) {
            this.stem = stem;
            stemIndex = new StemIndex();
            children = new HashMap<>();
            this.parent = parent;
        }

        void put(LinkedList<String> list) {
            String element = list.removeLast();
            stemIndex.add(element);
            if (list.size() > 0)
                children.computeIfAbsent(element, node -> new Node(element, this)).put(list);
        }

        public Node find(LinkedList<String> terms) {
            if (terms.size() == 0)
                return this;
            Node child = children.get(terms.removeLast());
            if (child != null) return child.find(terms);
            return null;
        }

        Node findMostSpecific(LinkedList<String> terms) {
            if (terms.size() == 0)
                return this;
            Node child = children.get(terms.removeLast());
            if (child != null) return child.find(terms);
            return this;
        }

        public StemIndex getStemIndex() {
            return stemIndex;
        }
    }
}
