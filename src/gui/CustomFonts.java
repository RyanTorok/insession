package gui;

import javafx.scene.text.Font;
import main.Size;
import terminal.Address;

import java.io.File;

public class CustomFonts {

    private static final Font COMFORTAA = Font.loadFont("file:" + Address.fromRootAddr("resources", "fonts", "Comfortaa-Regular.ttf") , 12);
    private static final Font COMFORTAA_BOLD = Font.loadFont("file:" + Address.fromRootAddr("resources", "fonts", "Comfortaa-Bold.ttf") , 12);
    private static final Font COMFORTAA_LIGHT = Font.loadFont("file:" + Address.fromRootAddr("resources", "fonts", "Comfortaa-Light.ttf") , 12);

    public static Font comfortaa(double size) {
        return Font.font(COMFORTAA.getFamily(), Size.fontSize(size));
    }

    public static Font comfortaa_bold(double size) {
        return Font.font(COMFORTAA_BOLD.getFamily(), Size.fontSize(size));
    }

    public static Font comfortaa_light(double size) {
        return Font.font(COMFORTAA_LIGHT.getFamily(), Size.fontSize(size));
    }
}
