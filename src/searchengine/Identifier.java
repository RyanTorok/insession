package searchengine;

public class Identifier implements Comparable {
    private String name;
    private Type type;
    private long id;

    public Identifier(String name, Type type, long id) {
        this.setName(name);
        this.type = type;
        this.setId(id);
    }

    public Indexable find() {
        return null;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    enum Type {
        Module, Class, Organization, Class_Item, Post, Utility, Setting
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Identifier))
            return 0;
        return getName().compareTo(((Identifier) o).getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Identifier))
            return false;
        return getId() == ((Identifier) obj).getId() && getType() == ((Identifier) obj).getType();
    }
}
