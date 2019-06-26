package classes;

import java.util.ArrayList;
import java.util.List;

public class LinearRegressionMap implements NumberListOperation {

    private double largest, average;
    private boolean doNotLower;

    public LinearRegressionMap(double largest, double average, boolean doNotLower) {
        this.largest = largest;
        this.average = average;
        this.doNotLower = doNotLower;
    }

    @Override
    public List<Double> evaluate(List<Double> input) {
        if (input.size() == 0)
            return new ArrayList<>(input);
        //offset by (expected_largest - actual_largest)
        double max = -Double.MAX_VALUE, sum = 0;
        for (Double item : input) {
            sum += item;
            if (item > max)
                max = item;
        }
        List<Double> result = new ArrayList<>(input);
        if (largest - max > 0 || !doNotLower) {
            double finalMax = max;
            result.replaceAll(aDouble -> aDouble + largest - finalMax);
        }

        //make up an imaginary line with x in (0, 2), where the largest value has x = 0, and the average has x = 1.
        double average_actual = sum / input.size();

        double m_actual = -(largest - average_actual);
        double m_target = -(largest - average);

        if (m_actual == 0) {
            //error: the largest equals the average (i.e. all values are the same)
            //just make all the values equal to the desired largest value, and drop the average constraint
            return result;
        }

        result.replaceAll(item -> {
            final double x = (item - largest) / m_actual;
            final double newVal = m_target * x + largest;
            if (doNotLower) return Math.max(item, newVal);
            else return newVal;
        });

        return result;
    }
}
