package modulesearch;

import java.io.Serializable;
import java.sql.Timestamp;

public class SearchRecord implements Serializable {
    static final long serialVersionUID = 200L;

    private String query;
    private Timestamp searchTime;

    public SearchRecord(String query) {
        this.setQuery(query);
        setSearchTime(new Timestamp(System.currentTimeMillis()));
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Timestamp getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(Timestamp searchTime) {
        this.searchTime = searchTime;
    }
}
