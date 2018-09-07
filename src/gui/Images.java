package gui;

import javafx.scene.image.Image;
import terminal.Address;

import java.util.HashMap;

public class Images {


    private static Image defaultUserImage;
    private static HashMap<Long, Image> userImages = new HashMap<>();

    public static Image getUserPicture(long posterId) {
        Image image = userImages.get(posterId);
        if (image == null)
            image = lookupImage(posterId);
        return image == null ? defaultUserImage() : image;
    }

    private static Image lookupImage(long posterId) {
        return null; //TODO
    }

    public static Image defaultUserImage() {
        if (defaultUserImage != null)
            return defaultUserImage;
        try {
            Image img = new Image("file:" + Address.fromRootAddr("resources", "default_user.png"));
            defaultUserImage = img;
            return img;
        } catch (Exception e1) {
            return null;
        }
    }

    public static void addUserImage(Long id, Image image) {
        userImages.put(id, image);
    }
}
