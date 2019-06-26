package classes;

import java.util.*;

public class TestRandomization {

    // the ith element of this list contains the ORIGINAL question number which will appear at positon i in the ACTUAL ordering.
    private final List<Integer> order;

    public TestRandomization randomize(Test t, RandomizationMode mode) {
        final int numberOfQuestions = t.getQuestions().size();
        if (mode == RandomizationMode.NONE)
            return identity(numberOfQuestions);
        List<List<Integer>> dependencyMatrix = new ArrayList<>();
        for (int i = 0; i < numberOfQuestions; i++) {
          //  dependencyMatrix.add(t.getQuestions().get(i).numberDependencies());
        }

        if (mode == RandomizationMode.RANDOMIZE_GROUPS) {

        }
        if (mode == RandomizationMode.RANDOMIZE_WITHIN_GROUPS) {

        }
        if (mode == RandomizationMode.FULLY_RANDOM) {
            //we generate a completely random list and then fix the dependencies
            final List<Integer> workspace = identity(numberOfQuestions).order;
            Collections.shuffle(workspace);

            Integer[] dependers = new Integer[numberOfQuestions];
            boolean[] fixed = new boolean[numberOfQuestions];
            int index = 0;

            //identity map each question that is depended on
            for (List<Integer> column : dependencyMatrix) {
                int lastentry = -1;
                for (Integer entry : column) {
                    if (!fixed[entry]) {
                        Collections.swap(workspace, entry, workspace.indexOf(entry));
                        fixed[entry] = true;
                    }
                    lastentry = entry;
                }
                if (lastentry != -1)
                    dependers[index] = lastentry;
                index++;
            }

            //place each depender question as close as possible after the last question it depends on
            for (int i = 0; i < dependers.length; i++) {
                Integer target = dependers[i];
                if (target != null) {
                    int attempt = target + 1;
                    while (attempt < numberOfQuestions && fixed[attempt]) {
                        attempt++;
                    }
                    if (attempt < numberOfQuestions) {
                        Collections.swap(workspace, i, attempt);
                        fixed[attempt] = true;
                    } else {
                        //everything after the target was locked, stabilize by leaving in original randomized location
                        //this case should not happen in practice, as long as there are no backward dependencies in the original test
                        //TODO we might want to place the question in the latest non-fixed position we can. This would make the case
                        // less awkward, but would hurt attempts to "debug" the test.
                        System.err.println("Got over-fixed case in test randomizer");
                    }
                }
            }
            return new TestRandomization(workspace);
        }
        throw new IllegalArgumentException("Unknown randomization type");
    }

    public enum  RandomizationMode {
        NONE, RANDOMIZE_GROUPS, RANDOMIZE_WITHIN_GROUPS, FULLY_RANDOM
    }

    public TestRandomization(List<Integer> order) {

        this.order = order;
    }

    private static TestRandomization identity(int size) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return new TestRandomization(list);
    }

    public Integer getOriginal(int actual) {
        return order.get(actual);
    }

    public Integer getActual(int original) {
        return order.indexOf(original);
    }

}
