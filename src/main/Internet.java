package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.*;

public class Internet {
    public static final String URL_Root = "http://www.paintbrusheducation.com/";

    public final void connectToPage(String filePath) throws IOException {
        URL url = null;
        try {
            url = new URL(URL_Root + filePath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        url.openConnection();
    }
    public Internet(String path) throws IOException {
        connectToPage(path);
    }
}
