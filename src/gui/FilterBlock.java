package gui;

import classes.Post;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class FilterBlock extends VBox {

    private AutoColoredLabel header;
    private List<Node> options;
    private final ToggleGroup toggleGroup;
    private final List<Filter> filters;
    private boolean multiple;

    FilterBlock(ClassView wrapper, String header, boolean multiple, List<Filter> filters) {
        this.header = new AutoColoredLabel(header, wrapper);
        options = new ArrayList<>();
        toggleGroup = new ToggleGroup();
        this.multiple = multiple;
        if (this.multiple)
            options = filters.stream().map(filter -> (new CheckBox(filter.name) {{setTextFill(wrapper.getLighterTextFill()); setSelected(true); selectedProperty().addListener((observable, oldValue, newValue) -> FilterBlock.this.changeEvent(wrapper));}})).collect(Collectors.toList());
        else options = filters.stream().map(filter -> (new RadioButton(filter.name) {{setTextFill(wrapper.getLighterTextFill()); setToggleGroup(toggleGroup); selectedProperty().addListener((observable -> FilterBlock.this.changeEvent(wrapper)));}})).collect(Collectors.toList());
        this.filters = filters;
        getChildren().add(this.header);
        getChildren().addAll(options);
        getChildren().forEach(node -> node.setFocusTraversable(false));
        setSpacing(5);
    }

    void changeEvent(ClassView wrapper) {
        wrapper.getPostFiltersAndList().getChildren().set(0, wrapper.makePostsList());
    }

    FilterBlock(ClassView wrapper, String header, boolean multiple, Filter... filters) {
        this(wrapper, header, multiple, Arrays.asList(filters));
    }

    boolean matches(Post post) {
        if (multiple) {
            if (filters.size() == 0)
                return true;
            for (int i = 0; i < filters.size(); i++) {
                Filter f = filters.get(i);
                if (f.matches(post) && options.get(i) instanceof CheckBox && ((CheckBox) options.get(i)).isSelected())
                    return true;
            }
            return false;
        }
        for (int i = 0; i < filters.size(); i++) {
            if (toggleGroup.getToggles().get(i).isSelected() && !filters.get(i).matches(post))
                return false;
        }
        return true;
    }

    public AutoColoredLabel getHeader() {
        return header;
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
