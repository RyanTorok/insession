package searchengine;

public class Identifier {
    private String name;
    private Type type;
    private long id;

    enum Type {
        Module, Class, Organization, Class_Item, Post, Utility, Setting
    }
}
