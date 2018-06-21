package searchengine;

import module.Module;
import java.io.Serializable;
import java.sql.Timestamp;

public class WatchRecord implements Serializable {
    static final long serialVersionUID = 201L;
    private Module module;
    private Timestamp timeWatched;

    public WatchRecord(Module m) {
        this.setModule(m);
        this.setTimeWatched(new Timestamp(System.currentTimeMillis()));
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Timestamp getTimeWatched() {
        return timeWatched;
    }

    public void setTimeWatched(Timestamp timeWatched) {
        this.timeWatched = timeWatched;
    }
}
