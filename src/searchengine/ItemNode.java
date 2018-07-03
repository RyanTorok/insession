package searchengine;

public class ItemNode {
    Identifier identifier;
    int relevance;

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public ItemNode(Identifier identifier, int relevance) {
        this.identifier = identifier;
        this.relevance = relevance;
    }

    public void merge(ItemNode other) {
        relevance += other.relevance;
    }
}
