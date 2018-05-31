package terminal;

public class TerminalRet {
    private String text;
    private boolean hide;
    private boolean clear;

    public TerminalRet(String text, boolean hide, boolean clear) {
        this.text = text;
        this.hide = hide;
        this.clear = clear;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }
}
