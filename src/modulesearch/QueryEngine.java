package modulesearch;

import main.Root;

public class QueryEngine {


    public void query(String query) {
        //query logic goes here
        Root.getActiveUser().search(query);
    }
}
