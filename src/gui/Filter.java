package gui;

import classes.Post;
import classes.PostStatus;
import classes.TimeStatus;
import searchengine.Identifier;

import java.util.Date;

class Filter {

    private TimeStatus timeStatus;
    private Date comparisonTime;
    private PostStatus postStatus;
    String name;
    long userId;
    long classItemId;

    Filter(String name, long userId, long classItemId) {
        this.name = name;
        this.userId = userId;
        this.classItemId = classItemId;
    }

    Filter(String name, PostStatus status) {
        this.name = name;
        this.postStatus = status;
    }

    Filter(String name, TimeStatus status, long comparisonTime) {
        this.name = name;
        this.timeStatus = status;
        this.comparisonTime = new Date(comparisonTime);
    }

    Filter(String name, TimeStatus status) {
        this(name, status, -1);
    }

    boolean matches(Post post) {
        if (postStatus != null && !post.getStatusLabels().contains(postStatus)) return false;
        if (timeStatus != null && !timeMatches(post)) return false;
        return classItemId == -1 || post.getClassItemId() == classItemId;
    }

    private boolean timeMatches(Post post) {
        Identifier id = post.getIdentifier();
        long secs = 86400;
        switch (timeStatus) {
            case TODAY: {
                long now = System.currentTimeMillis();
                long today = now - now % secs;
                long diff1 = id.getTime1() - today;
                long diff2 = id.getTime2() - today;
                return (diff1 > 0 && diff1 < secs) || (diff2 > 0 && diff2 < secs);
            }
            case THIS_WEEK: {
                Date idDate = new Date(id.getTime1() - id.getTime1() % secs);
                long now = System.currentTimeMillis();
                Date lastWeek = new Date(now - now % secs - secs * 7 - 1);
                return idDate.after(lastWeek) || new Date(id.getTime2() - id.getTime2() % secs).after(lastWeek);
            }
            case ON:
                long diff1 = id.getTime1() - comparisonTime.getTime();
                long diff2 = id.getTime2() - comparisonTime.getTime();
                return (diff1 > 0 && diff1 < secs) || (diff2 > 0 && diff2 < secs);
            case AFTER:
                return new Date(id.getTime1()).after(comparisonTime) || new Date(id.getTime2()).after(comparisonTime);
            case BEFORE:
                return (new Date(id.getTime1()).before(comparisonTime) && (id.getTime1() != 0)) || (new Date(id.getTime2()).before(comparisonTime) && (id.getTime2() != 0));
            default: return false;
        }
    }

    public Date getComparisonTime() {
        return comparisonTime;
    }

    public void setComparisonTime(Date comparisonTime) {
        this.comparisonTime = comparisonTime;
    }
}
