package gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import main.User;

public class UserProfile extends TaskView {
    public UserProfile(User user) {
        super(user.getFirst() + " " + user.getLast() + "'s Profile");
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }

    @Override
    protected Pane initDisplay() {
        return null;
    }
}