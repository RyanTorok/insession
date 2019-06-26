package classes;

import java.util.List;
import java.util.Objects;

public class WeightedSum implements AbstractFunction<List<Double>, Double> {
    private double[] weights;

    public WeightedSum(double[] weights) {
        this.weights = weights;
    }

    @Override
    public Double evaluate(List<Double> input) {
        Objects.requireNonNull(weights);
        if (input.size() != weights.length)
            throw new IllegalArgumentException("wrong input length");
        double sum = 0;
        int i = 0;
        for (Double d : input) {
            sum += weights[i] * d;
            i++;
        }
        return sum;
    }
}
