package gui;

import classes.ClassPd;

public class ClassView extends TaskView {

    public ClassView(ClassPd classPd) {
        super(classPd.getCastOf().getName() + " - P" + classPd.getPeriodNo() + " - " + classPd.getTeacherLast());
    }
}
