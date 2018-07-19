package searchengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StemIndex extends Trie implements Serializable {

    static final long serialVersionUID = 80L;

    @Override
    public void add(String stem) {
        stem = stem.toLowerCase();
        super.add(stem);
    }

    @Override
    public List<Trie> findStemDescending(String stem) {
        if (stem.length() == 0)
            return new ArrayList<>();
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

    void initialize(String filename) {
        File f = new File(filename);
        try {
            Scanner s = new Scanner(f);
            while (s.hasNextLine())
                add(s.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
