package searchengine;

import java.util.*;
import java.util.stream.Collectors;

public class Trie implements Comparable<Trie> {
    private String stem;
    private HashMap<Character, Trie> children;
    private boolean isValid;
    private int maxPriority;
    private int priority;

    private Trie(String stem, boolean isValid) {
        setChildren(new HashMap<>());
        this.setStem(stem);
        this.setValid(isValid);
    }

    public Trie() {
        this("", false);
    }

    public void add(String stem) {
        if (stem == null)
            return;
        if (stem.length() == 0) {
            isValid = true;
            maxPriority++;
            return;
        }
        maxPriority++;
        Character first = stem.charAt(0);
        Trie child = getChildren().get(first);
        if (child == null) {
            child = new Trie(this.getStem() + stem.charAt(0), stem.length() == 1);
            getChildren().put(first, child);
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

    //returns the n highest priority nodes beginning with the provided stem
    List<Trie> findStemDescending(String stem, int n) {
        if (stem == null)
            return new ArrayList<>();
        Trie start = find(stem);
        if (start == null) {
            return new ArrayList<>();
        }
        return start.findBestNMatches(n);
    }

    private List<Trie> findBestNMatches(int n) {
        if (children == null || children.entrySet().size() == 0) {
            assert isValid;
            return new ArrayList<>() {{add(Trie.this);}};
        }
        List<Trie> childrenSorted = children.entrySet().stream().map(Map.Entry::getValue).sorted(Comparator.comparing(trie -> getMaxPriority()).reversed()).collect(Collectors.toList());
        List<Trie> found = new ArrayList<>();
        for (int i = 0; i < childrenSorted.size() && found.size() < n; i++) {
            found.addAll(childrenSorted.get(i).findBestNMatches(n - found.size()));
        }
        if (isValid)
            found.add(0, this);
        List<Trie> foundSorted = found.stream().sorted(Comparator.comparing(trie -> getMaxPriority()).reversed()).collect(Collectors.toList());
        assert foundSorted.size() < n + 2;
        if (foundSorted.size() > n)
            foundSorted.remove(n);
        return foundSorted;
    }

    //hyper-optimized version of findStemDescending which only gets the most popular match
    public String getBestMatch(String stem) {
        if (stem == null || stem.length() == 0)
            return null;
        Trie root = find(stem);
        if (root == null)
            return null;
        return root.findBest().item;
    }

    private Node findBest() {
        String best = "";
        int bestValue = -1;
        for (Map.Entry<Character, Trie> t : getChildren().entrySet()) {
            Node n = t.getValue().findBest();
            if (n.value > bestValue) {
                bestValue = n.value;
                best = n.item;
            }
        }
        if (isValid()) {
            if (maxPriority > bestValue) {
                best = getStem();
                bestValue = maxPriority;
            }
        }
        return new Node(best, bestValue);
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public HashMap<Character, Trie> getChildren() {
        return children;
    }

    public void setChildren(HashMap<Character, Trie> children) {
        this.children = children;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public int getMaxPriority() {
        return maxPriority;
    }

    public void setMaxPriority(int maxPriority) {
        this.maxPriority = maxPriority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(Trie o) {
        int diff = priority - o.priority;
        if (diff == 0) //prevent reduction in TreeSet
            return stem.compareTo(o.stem);
        return diff;
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
        return t.isValid();
    }

    private Trie find(String stem) {
        if (stem.equals(this.getStem()))
            return this;
        else {
            if (stem.startsWith(this.getStem())) {
                Trie child = getChildren().get(stem.charAt(this.getStem().length()));
                if (child == null)
                    return null;
                else return child.quickFind(stem);
            }
            else return null;
        }
    }

    //bypasses the stem start check to make recursion in find() faster
    private Trie quickFind(String stem) {
        if (stem.equals(this.getStem()))
            return this;
        else {
                Trie child = getChildren().get(stem.charAt(this.getStem().length()));
                if (child == null)
                    return null;
                else return child.quickFind(stem);
        }
    }

    private List<Trie> getAll() {
        ArrayList<Trie> results = new ArrayList<>();
        for (Map.Entry<Character, Trie> t: getChildren().entrySet()) {
            results.addAll(t.getValue().getAll());
        }
        if (isValid())
            results.add(this);
        return results.stream().sorted(Comparator.comparing(Trie::getMaxPriority)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Trie))
            return false;
        return getStem().equals(((Trie) obj).getStem());
    }
}
