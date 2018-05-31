package modulesearch;

public class WordVectorDimension {
    Long value;

    Long getValue(){
        return value;
    }

    void increment() {
        if (value == Long.MAX_VALUE)
            return;
        value++;
    }
}
