package classes;

import java.util.ArrayList;
import java.util.List;

public class ScaleMap implements NumberListOperation {

    private double multiply = 1;
    private double add = 0;

    @Override
    public List<Double> evaluate(List<Double> input) {
        ArrayList<Double> result = new ArrayList<>(input);
        result.replaceAll(item -> getMultiply() * item + getAdd());
        return result;
    }

    public double getMultiply() {
        return multiply;
    }

    public void setMultiply(double multiply) {
        this.multiply = multiply;
    }

    public double getAdd() {
        return add;
    }

    public void setAdd(double add) {
        this.add = add;
    }
}
