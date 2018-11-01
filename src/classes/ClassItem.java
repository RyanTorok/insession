package classes;

import main.User;

import java.util.UUID;

public class ClassItem {
    private String name;
    private UUID id;

    public static ClassItem fromId(UUID classItemId) {
        return null; //TODO
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
