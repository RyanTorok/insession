package searchengine;

import main.Root;

import java.util.*;
import java.util.stream.Collectors;

public class QueryEngine {

    private Index index;

    public List<Identifier> query(String query) {
        ParseTree parseTree = ParseTree.fromQuery(query);
        Root.getActiveUser().search(query);
        Result result = getResults(parseTree);
        if (result.isNegative()) {
            return new ArrayList<>();
        } else return result.getResults().stream()
                .sorted(Comparator.comparing(ItemNode::getRelevance))
                .map(node -> node.identifier)
                .collect(Collectors.toList());
    }

    private Result getResults(ParseTree parseTree) {
        switch (parseTree.getType()) {
            case OR: {
                HashSet<ItemNode> combined = new HashSet<>();
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
                    Set<ItemNode> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getRight())).collect(Collectors.toSet());
                    return new Result(filter, true);
                }

                //case 3: left is positive and right is negative
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(right.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getLeft())).collect(Collectors.toSet());
                    return new Result(filter, true);
                }

                //case 4: both negative queries (perform AND merge and percolate negation)
                if (left.isNegative() && right.isNegative()) {
                    return new Result(andMerge(left.results, parseTree.getRight()), true);
                }
                return null; //shouldn't get here, just for aesthetic purposes
            }
            case AND: {

                HashSet<ItemNode> combined = new HashSet<>();
                Result left = getResults(parseTree.getLeft()), right = getResults(parseTree.getRight());

                //case 1: both positive queries
                if (!left.isNegative() && !right.isNegative()) {
                    return new Result(andMerge(left.results, parseTree.getRight()), false);
                }

                //case 2: left is negative and right is positive - TODO
                if (left.isNegative() && !right.isNegative()) {
                    combined.addAll(right.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getLeft())).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 3: left is positive and right is negative - TODO
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(left.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> !matchesQuery(result, parseTree.getRight())).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 4: both negative queries (perform OR merge and percolate negation)
                if (left.isNegative() && right.isNegative()) {
                    combined.addAll(left.results);
                    combined.addAll(right.results);
                    return new Result(combined, true);
                }
                return null; //shouldn't get here, just for aesthetic purposes

            }
            case WORD: {
                HashSet<ItemNode> results = index.getItems(parseTree.getWord());
                return new Result(results, parseTree.isNegative());
            }

            case PHRASE: {
                Set<ItemNode> results = index.getItems(parseTree.getWord());
                results = results.stream().filter(itemNode -> itemNode.identifier.find().containsString(parseTree.getWord())).collect(Collectors.toSet());
                return new Result(results, parseTree.isNegative());
            }

            default: throw new IllegalStateException("Non-query element escaped parse tree");
        }
    }

    private boolean matchesQuery(ItemNode item, ParseTree tree) {
        return true;
    }

    private Set<ItemNode> andMerge(Set<ItemNode> left, ParseTree right) {
        Set<ItemNode> combined = new HashSet<>();
        combined.addAll(left);
        return combined.stream().filter(result -> matchesQuery(result, right)).collect(Collectors.toSet());
    }

    class Result {
        private boolean negative;
        private Set<ItemNode> results;

        public Result(Set<ItemNode> results, boolean negative) {

            this.results = results;
            this.negative = negative;
        }


        public boolean isNegative() {
            return negative;
        }

        public void setNegative(boolean negative) {
            this.negative = negative;
        }

        public Set<ItemNode> getResults() {
            return results;
        }

        public void setResults(Set<ItemNode> results) {
            this.results = results;
        }
    }
}
