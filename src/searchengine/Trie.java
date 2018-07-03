package searchengine;

import java.util.*;
import java.util.stream.Collectors;

public class Trie {
    String stem;
    HashMap<Character, Trie> children;
    boolean isValid;
    int accessCount;

    private Trie(String stem, boolean isValid) {
        children = new HashMap<>();
        this.stem = stem;
        this.isValid = isValid;
        accessCount = 0;
    }

    public Trie() {
        this("", false);
    }

    public void add(String stem) {
        if (stem == null || stem.length() == 0)
            return;
        accessCount++;
        Character first = stem.charAt(0);
        Trie child = children.get(first);
        if (child == null) {
            child = new Trie(this.stem + stem.charAt(0), stem.length() == 1);
            children.put(first, child);
        }
        if (stem.length() > 1)
            child.add(stem.substring(1));
    }

    //returns the list of valid words/expressions which begin with the argument String, in decreasing order of access count
    public List<Trie> findStemDescending(String stem) {
        if (stem == null)
            return new ArrayList<>();
        Trie start = find(stem);
        if (start == null)
            return new ArrayList<>();
        return start.getAll();
    }

    //hyper-optimized version of findStemDescending which only gets the most popular match
    public String getBestMatch(String stem) {
        if (stem == null || stem.length() == 0)
            return null;
        Trie root = find(stem);
        if (root == null)
            return null;
        return findBest().item;
    }

    private Node findBest() {
        String best = "";
        int bestValue = -1;
        for (Map.Entry<Character, Trie> t : children.entrySet()) {
            Node n = t.getValue().findBest();
            if (n.value > bestValue) {
                bestValue = n.value;
                best = n.item;
            }
        }
        if (isValid) {
            if (accessCount > bestValue) {
                best = stem;
                bestValue = accessCount;
            }
        }
        return new Node(best, bestValue);
    }

    private static class Node {
        String item;
        int value;

        public Node(String item, int value) {
            this.item = item;
            this.value = value;
        }
    }

    public boolean contains(String stem) {
        if (stem == null)
            return false;
        Trie t = find(stem);
        if (t == null)
            return false;
        return t.isValid;
    }

    private Trie find(String stem) {
        if (stem.equals(this.stem))
            return this;
        else {
            if (stem.startsWith(this.stem)) {
                Trie child = children.get(stem.charAt(this.stem.length()));
                if (child == null)
                    return null;
                else return child.quickFind(stem);
            }
            else return null;
        }
    }

    //bypasses the stem start check to make recursion in find() faster
    private Trie quickFind(String stem) {
        if (stem.equals(this.stem))
            return this;
        else {
                Trie child = children.get(stem.charAt(this.stem.length()));
                if (child == null)
                    return null;
                else return child.quickFind(stem);
        }
    }

    private List<Trie> getAll() {
        ArrayList<Trie> results = new ArrayList<>();
        for (Map.Entry<Character, Trie> t: children.entrySet()) {
            results.addAll(t.getValue().getAll());
        }
        if (isValid)
            results.add(this);
        return results.stream().sorted(Comparator.comparing(trie -> trie.accessCount)).collect(Collectors.toList());
    }
}
