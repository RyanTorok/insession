package net;

public class PostRequest {


    private final String identifier;
    private final Object value;

    public PostRequest(String identifier, Object value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public String toString() {
        return identifier + "=" + value;
    }
}
