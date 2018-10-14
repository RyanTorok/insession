package searchengine;

import classes.ClassPd;

import java.util.ArrayList;

public class ItemNode {

    Identifier identifier;

    private int relevance;

    public static ItemNode fromRemoteRegex(String regex) {
        int start = 0, end = parseSingleton(regex, 0);
        ArrayList<String> fields = new ArrayList<>();
        while (true) {
            fields.add(makeString(regex, start, end));
            start = end + 1;
            if (start >= regex.length())
                break;
            end = parseSingleton(regex, start);
        }
        if (fields.size() != 11) {
            System.err.println("Warning: incorrect ItemNode regex size: " + fields.size());
        }
        Identifier id = new Identifier(fields.get(0), Identifier.Type.valueOf(fields.get(2)), Long.parseLong(fields.get(4)));
        id.setDescription(fields.get(1));
        id.setId(Long.parseLong(fields.get(3)));
        id.setBelongsTo(ClassPd.fromId(id.getId()));
        id.setAuthorName(fields.get(5));
        id.setTime1(Long.parseLong(fields.get(6)));
        id.setTime2(Long.parseLong(fields.get(7)));
        id.setViews(Long.parseLong(fields.get(8)));
        id.setLikes(Long.parseLong(fields.get(9)));
        return new ItemNode(id, Integer.parseInt(fields.get(10)));
    }

    private static String makeString(String regex, int start, int end) {
        return regex.substring(start, end).replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\;", ";");
    }

    private static int parseSingleton(String regex, int start) {
        boolean escape = false;
        int i = start;
        while(regex.charAt(i) != ';' || escape) {
            if (escape) {
                escape = false;
                i++;
                continue;
            }
            if (regex.charAt(i) == '\\')
                escape = true;
            i++;
        }
        return i;
    }

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
