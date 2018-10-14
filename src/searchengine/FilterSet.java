package searchengine;

import classes.ClassPd;
import gui.SearchFilterBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FilterSet {
    //data in lists represents what should NOT be searched for, since an unbounded filter is most common, and should be the most optimized
    ArrayList<Identifier.Type> types;
    List<ClassPd> classPds;
    Date dateRestriction;
    DateConstraint dateConstraint;

    //order here matters due to the implementation in SearchFilterBox.java
    public enum  DateConstraint {
        TODAY, PAST_WEEK, ON, AFTER, BEFORE, NONE
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

    public FilterSet(Integer typesParam, DateConstraint constraint, Date restriction, Long... classPds) {
        types = new ArrayList<>();
        if (isNthBitNotSet(typesParam, 1)) types.add(Identifier.Type.Class_Item);
        if (isNthBitNotSet(typesParam, 2)) types.add(Identifier.Type.Module);
        if (isNthBitNotSet(typesParam, 3)) types.add(Identifier.Type.Post);
        if (isNthBitNotSet(typesParam, 4)) types.add(Identifier.Type.Class);
        if (isNthBitNotSet(typesParam, 5)) types.add(Identifier.Type.Organization);
        if (isNthBitNotSet(typesParam, 6)) types.add(Identifier.Type.People);

        dateConstraint = constraint;
        dateRestriction = restriction;
        this.classPds = Arrays.stream(classPds).map(ClassPd::fromId).collect(Collectors.toList());
    }

    private boolean isNthBitNotSet(int src, int n) {
        return (src & (1 << (n - 1))) == 0;
    }
}
