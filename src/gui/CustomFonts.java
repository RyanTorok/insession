package gui;

import javafx.scene.text.Font;
import terminal.Address;

import java.io.File;

public class CustomFonts {

    static final Font COMFORTAA = Font.loadFont("file:" + Address.root_addr + File.separator + "resources" + File.separator + "fonts" + File.separator + "Comfortaa-Regular.ttf" , 12);
    static final Font COMFORTAA_BOLD = Font.loadFont("file:" + Address.root_addr + File.separator + "resources" + File.separator + "fonts" + File.separator + "Comfortaa-Bold.ttf" , 12);
    static final Font COMFORTAA_LIGHT = Font.loadFont("file:" + Address.root_addr + File.separator + "resources" + File.separator + "fonts" + File.separator + "Comfortaa-Light.ttf" , 12);


    public static Font comfortaa(double size) {
        return Font.font(COMFORTAA.getFamily(), size);
    }

    public static Font comfortaa_bold(double size) {
        return Font.font(COMFORTAA_BOLD.getFamily(), size);
    }

    public static Font comfortaa_light(double size) {
        return Font.font(COMFORTAA_LIGHT.getFamily(), size);
    }

}
