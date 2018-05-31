package cafeteria;

import java.util.ArrayList;
import java.util.List;

public class LineMenu {

    public List<FoodItem> items;
    public double linePrice;

    public LineMenu() {
        items = new ArrayList();
    }

    public LineMenu(double linePrice) {
        items = new ArrayList<>();
        this.linePrice = linePrice;
    }
}