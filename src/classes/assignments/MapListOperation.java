package classes.assignments;

import classes.AbstractFunction;
import classes.Grade;
import classes.NumberListOperation;
import main.User;

import java.util.*;
import java.util.stream.Collectors;

public class MapListOperation<K extends Comparable<K>> {

    private TreeMap<K, Double> result;

    public MapListOperation(TreeMap<K, Double> input) {
        result = new TreeMap<>(input);
    }

    public void apply(NumberListOperation e) {
        final List<Double> resultList = e.evaluate(new ArrayList<>(result.values()));
        final Iterator<Double> iterator = resultList.iterator();
        final TreeMap<K, Double> result = new TreeMap<>();
        for (K key : result.keySet()){
            result.put(key, iterator.next());
        }
        this.result = result;
    }

    //applyEach() performs operations in place, so we do this so returned values aren't altered later.
    public TreeMap<K, Double> getResult() {
        return new TreeMap<>(result);
    }
}
