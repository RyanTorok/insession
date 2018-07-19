package searchengine;

public class RankedString {
    private String string;
    private int relevance;

    public RankedString(String string, int relevance) {
        this.string = string;
        this.relevance = relevance;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }
}
