package searchengine;

import java.util.List;

public class StemIndex extends Trie {

    @Override
    public void add(String stem) {
        stem = stem.toLowerCase();
        super.add(stem);
    }

    @Override
    public List<Trie> findStemDescending(String stem) {
        return super.findStemDescending(stem.toLowerCase());
    }

    @Override
    public boolean contains(String stem) {
        return super.contains(stem.toLowerCase());
    }

    @Override
    public String getBestMatch(String stem) {
        return super.getBestMatch(stem.toLowerCase());
    }
}
