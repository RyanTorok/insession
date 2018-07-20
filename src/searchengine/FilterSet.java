package searchengine;

import classes.ClassPd;
import gui.SearchFilterBox;

import java.util.ArrayList;
import java.util.Date;

public class FilterSet {
    //data in lists represents what should NOT be searched for, since an unbounded filter is most common, and should be the most optimized
    ArrayList<Identifier.Type> types;
    ArrayList<ClassPd> classPds;
    Date dateRestriction;
    DateConstraint dateConstraint;

    //order here matters due to the implementation in SearchFilterBox.java
    public enum  DateConstraint {
        TODAY, PAST_WEEK, ON, AFTER, BEFORE, NONE;
    }

    public FilterSet(SearchFilterBox box) {
        types = new ArrayList<>();
        classPds = new ArrayList<>();
        for (Identifier.Type val: Identifier.Type.values()) {
            if (!box.isSelected(val)) {
                types.add(val);
            }
        }
        for (ClassPd classPd: box.getIndexedClasses()) {
            if (!box.isSelected(classPd))
                classPds.add(classPd);
        }
        dateRestriction = box.getDateRestriction();
        dateConstraint = box.getDateConstraint();
    }
}
