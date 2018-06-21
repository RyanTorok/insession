package searchengine;

import main.Root;

public class QueryEngine {


    public void query(String query) {

        Root.getActiveUser().search(query);
    }
}
