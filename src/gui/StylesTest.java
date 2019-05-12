package gui;

import javafx.scene.text.Text;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StylesTest {

    @Test
    public void basicTest() {
        Text dummy = new Text("Hello World!");
        Styles.setProperty(dummy, "-fx-font-size", "24");
        assertEquals(Styles.getProperty(dummy, "-fx-font-size"), "24");
    }

    @Test
    public void twoParams() {
        Text dummy = new Text("Hello World!");
        Styles.setProperty(dummy, "-fx-font-size", "24");
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        assertEquals(Styles.getProperty(dummy, "-fx-font-family"), "Arial");
        assertEquals(Styles.getProperty(dummy, "-fx-font-size"), "24");
    }

    @Test
    public void setAndRemove() {
        Text dummy = new Text("Hello World!");
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        Styles.removeProperty(dummy, "-fx-font-family");
        assertEquals("", Styles.getProperty(dummy, "-fx-font-family"));
    }

    @Test
    public void overwrite() {
        Text dummy = new Text("Hello World!");
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        assertEquals("Arial", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.setProperty(dummy, "-fx-font-family", "Comic Sans");
        assertEquals("Comic Sans", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        assertEquals("Arial", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.removeProperty(dummy, "-fx-font-family");
        assertEquals("", Styles.getProperty(dummy, "-fx-font-family"));
    }

    @Test
    public void integration() {
        Text dummy = new Text("Hello World!");
        Styles.setProperty(dummy, "-fx-font-size", "24");
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        Styles.setProperty(dummy, "-fx-text-fill", "#00ff00");
        assertEquals("Arial", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.setProperty(dummy, "-fx-font-family", "Comic Sans");
        assertEquals("Comic Sans", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.setProperty(dummy, "-fx-font-family", "Arial");
        assertEquals("Arial", Styles.getProperty(dummy, "-fx-font-family"));
        Styles.removeProperty(dummy, "-fx-font-family");
        assertEquals("", Styles.getProperty(dummy, "-fx-font-family"));
        assertEquals("#00ff00", Styles.getProperty(dummy, "-fx-text-fill"));
        assertEquals("", Styles.getProperty(dummy, "-fx-nonexistent-property"));
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest1() {
        Styles.setProperty(null, "",  "");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest2() {
        Styles.setProperty(new Text(), null, "");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest3() {
        Styles.setProperty(new Text(), "", null);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest4() {
        Styles.removeProperty(null, "");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest5() {
        Styles.removeProperty(new Text(), null);
    }


    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest6() {
        Styles.getProperty(null, "");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void nullTest7() {
        Styles.getProperty(null, "");
    }
}