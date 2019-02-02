package classes;

import java.util.Set;

class MultipleChoiceAnswer implements Answer {
    private Set<Integer> combination;

    MultipleChoiceAnswer(Set<Integer> combination) {
        this.combination = combination;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MultipleChoiceAnswer && ((MultipleChoiceAnswer) obj).combination.equals(combination);
    }

    @Override
    public boolean verify(Answer response) {
        if (!(response instanceof MultipleChoiceAnswer))
            return false;
        return combination.equals(((MultipleChoiceAnswer) response).combination);
    }
}
