package terminal;

import gui.SearchModule;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import main.Events;
import main.Root;

import java.util.ArrayList;

public class SEARCH extends Command {

    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        if (tokens.size() == 1) assertTokenCount(2, tokens, "search query");
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 1; i < tokens.size(); i++) {
            if (i > 1)
                queryBuilder.append(' ');
            queryBuilder.append(tokens.get(i).getTokenLabel());
        }
        String query = queryBuilder.toString();
        return new TerminalRet("", TerminalDrivenEvent.HIDE,
                new TerminalDrivenEvent(() -> {
                    SearchModule module = Root.getPortal().openSearchBar();
                    Timeline delay = new Timeline(new KeyFrame(Duration.millis(300)));
                    delay.setOnFinished(event -> {
                        TextField searchBox = module.getSearchBox();
                        searchBox.setText(query);
                        searchBox.requestFocus();
                        searchBox.positionCaret(searchBox.getText().length());
                        module.getSearchBox().getOnAction().handle(new ActionEvent());
                    });
                    Events.animation(delay);
                    return true;
                }));
    }
}
