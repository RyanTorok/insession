package gui;

import classes.Post;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class FilterBlock extends VBox {

    private Text header;
    private List<Node> options;
    private final ToggleGroup toggleGroup;
    private final List<Filter> filters;

    FilterBlock(ClassView wrapper, String header, boolean multiple, List<Filter> filters) {
        this.header = new Text(header);
        options = new ArrayList<>();
        toggleGroup = new ToggleGroup();
        if (multiple)
            options = filters.stream().map(filter -> (new CheckBox(filter.name) {{setTextFill(wrapper.getTextFill());}})).collect(Collectors.toList());
        else options = filters.stream().map(filter -> (new RadioButton(filter.name) {{setTextFill(wrapper.getTextFill()); setToggleGroup(toggleGroup);}})).collect(Collectors.toList());
        this.filters = filters;
//        System.out.println(filters.get(0).name);
        getChildren().addAll(options);
    }

    FilterBlock(ClassView wrapper, String header, boolean multiple, Filter... filters) {
        this(wrapper, header, multiple, Arrays.asList(filters));
    }

    boolean matches(Post post) {
        for (int i = 0; i < filters.size(); i++)
            if (toggleGroup.getToggles().get(i).isSelected() && !filters.get(i).matches(post))
                return false;
        return true;
    }

    public Text getHeader() {
        return header;
    }

    public void setHeader(Text header) {
        this.header = header;
    }

    public List<Node> getOptions() {
        return options;
    }

    public void setOptions(List<Node> options) {
        this.options = options;
    }

    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }

    public List<Filter> getFilters() {
        return filters;
    }
}
