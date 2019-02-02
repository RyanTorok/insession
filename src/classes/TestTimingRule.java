package classes;

public class TestTimingRule {

    private long miniumum_millis;
    private long limit_milis;
    private boolean pauseOnExit;
    private boolean exitable;

    public long getMiniumum_millis() {
        return miniumum_millis;
    }

    public void setMiniumum_millis(long miniumum_millis) {
        this.miniumum_millis = miniumum_millis;
    }

    public long getLimit_milis() {
        return limit_milis;
    }

    public void setLimit_milis(long limit_milis) {
        this.limit_milis = limit_milis;
    }

    public boolean isPauseOnExit() {
        return pauseOnExit;
    }

    public void setPauseOnExit(boolean pauseOnExit) {
        this.pauseOnExit = pauseOnExit;
    }

    public boolean isExitable() {
        return exitable;
    }

    public void setExitable(boolean exitable) {
        this.exitable = exitable;
    }
}
