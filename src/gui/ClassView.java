package gui;

import classes.ClassPd;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class ClassView extends TaskView {

    public ClassView(ClassPd classPd) {
        super(classPd.getCastOf().getName() + " - P" + classPd.getPeriodNo() + " - " + classPd.getTeacherLast());
    }

    public Pane getFullDisplay() {
        return null;
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }
}
