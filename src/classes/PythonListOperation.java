package classes;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PythonListOperation implements NumberListOperation {

    private PythonFunction wraps;

    public PythonListOperation(String pythonFilename) {
        wraps = new PythonFunction(pythonFilename);
    }

    @Override
    public List<Double> evaluate(List<Double> input) {
        final Object result = wraps.evaluate(input);
        if (result instanceof JSONArray) {
            List l = ((JSONArray) result).toList();
            for (Object o : l) {
                if (!(o instanceof Double))
                    return null;
            }
            //this is fine, we checked if everything is a Double above
            return new ArrayList<Double>(l);
        }
        return null;
    }
}
