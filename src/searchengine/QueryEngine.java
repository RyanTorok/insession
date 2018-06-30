package searchengine;

import main.Root;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryEngine {

    public Collection<Indexable> query(String query) {
        ParseTree parseTree = ParseTree.fromQuery(query);
        Root.getActiveUser().search(query);
        Result result = getResults(parseTree);
        if (result.isNegative()) {
            return new HashSet<>();
        } else return result.getResults();
    }

    private Result getResults(ParseTree parseTree) {
        switch (parseTree.getType()) {
            case OR: {
                HashSet<Indexable> combined = new HashSet<>();
                Result left = getResults(parseTree.getLeft()), right = getResults(parseTree.getRight());

                //case 1: both positive queries
                if (!left.isNegative() && !right.isNegative()) {
                    combined.addAll(left.results);
                    combined.addAll(right.results);
                    return new Result(combined, false);
                }

                //case 2: left is negative and right is positive
                if (left.isNegative() && !right.isNegative()) {
                    combined.addAll(left.results);
                    Set<Indexable> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getRight())).collect(Collectors.toSet());
                    return new Result(filter, true);
                }

                //case 3: left is positive and right is negative
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(right.results);
                    Set<Indexable> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getLeft())).collect(Collectors.toSet());
                    return new Result(filter, true);
                }

                //case 4: both negative queries (perform AND merge and percolate negation)
                if (left.isNegative() && right.isNegative()) {
                    return new Result(andMerge(left.results, parseTree.getRight()), true);
                }
                break; //shouldn't get here, just for aesthetic purposes
            }
            case AND: {

                HashSet<Indexable> combined = new HashSet<>();
                Result left = getResults(parseTree.getLeft()), right = getResults(parseTree.getRight());

                //case 1: both positive queries
                if (!left.isNegative() && !right.isNegative()) {
                    return new Result(andMerge(left.results, parseTree.getRight()), false);
                }

                //case 2: left is negative and right is positive - TODO
                if (left.isNegative() && !right.isNegative()) {
                    combined.addAll(right.results);
                    Set<Indexable> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getLeft())).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 3: left is positive and right is negative - TODO
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(left.results);
                    Set<Indexable> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getRight())).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 4: both negative queries (perform OR merge and percolate negation)
                if (left.isNegative() && right.isNegative()) {
                    combined.addAll(left.results);
                    combined.addAll(right.results);
                    return new Result(combined, true);
                }
                break; //shouldn't get here, just for aesthetic purposes

            }
            case WORD: {

            }
        }
    }

    private boolean matchesQuery(Indexable item, ParseTree tree) {
        return true;
    }

    private Set<Indexable> andMerge(Set<Indexable> left, ParseTree right) {
        Set<Indexable> combined = new HashSet<>();
        combined.addAll(left);
        return combined.stream().filter(result -> matchesQuery(result, right)).collect(Collectors.toSet());
    }

    class Result {
        private boolean negative;
        private Set<Indexable> results;

        public Result(Set<Indexable> results, boolean negative) {

            this.results = results;
            this.negative = negative;
        }


        public boolean isNegative() {
            return negative;
        }

        public void setNegative(boolean negative) {
            this.negative = negative;
        }

        public Set<Indexable> getResults() {
            return results;
        }

        public void setResults(Set<Indexable> results) {
            this.results = results;
        }
    }
}
