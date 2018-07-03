package searchengine;

import main.Root;

import java.util.*;
import java.util.stream.Collectors;

public class QueryEngine {

    private Index index;
    private static final String[] ignoredWords = {"where", "what", "how", "and", "or", "a", "an", "of", "i", "the"};
    //avert your eyes!
    public static final String[] safeSearchFilterWords = {"shit", "crap", "dick", "dickhead", "shithead", "ass", "asshole", "damn", "fuck", "motherfucker", "shithead", "dammit", "bitch", "hell", "goddamn", "goddammit"};
    private boolean safeSearchOn = false;
    private Set<ItemNode> filterItems;
    long lastQueryTimeNanos;
    HashSet<String> allMeaningfulWords;
    List<String> itemTitles;
    List<String> textExcerpts;
    StemIndex stemIndex;

    //performs a tentative query based on the most likely auto-completion of the last word
    public List<Identifier> incompleteQuery(String query) {
        int lastSpace = query.lastIndexOf("\\s+");
        String lastWord = query.substring(lastSpace + 1);
        String replace = stemIndex.getBestMatch(lastWord);
        if (replace == null || replace.length() == 0)
            return query(query);
        else return query(query.substring(0, lastSpace + 1) + replace);
    }

    public List<Identifier> query(String query) {
        long startTime = System.currentTimeMillis();
        ParseTree parseTree = ParseTree.fromQuery(query);
        Root.getActiveUser().search(query);
        Result result = getResults(parseTree);
        if (result.isNegative()) {
            lastQueryTimeNanos = System.currentTimeMillis() - startTime;
            return new ArrayList<>();
        } else {
            List<Identifier> results = result.getResults().stream()
                    .sorted(Comparator.comparing(ItemNode::getRelevance))
                    .map(node -> node.identifier)
                    .collect(Collectors.toList());
            lastQueryTimeNanos = System.currentTimeMillis() - startTime;
            return results;
        }
    }

    private Result getResults(ParseTree parseTree) {
        switch (parseTree.getType()) {
            case OR: {
                HashSet<ItemNode> combined = new HashSet<>();
                Result left = getResults(parseTree.getLeft()), right = getResults(parseTree.getRight());

                //sort result sets to speed up relevance merges to O(n log n) instead of O(n^2)
                left.results = left.results.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)).collect(Collectors.toSet());
                right.results = right.results.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)).collect(Collectors.toSet());

                //case 1: both positive queries
                if (!left.isNegative() && !right.isNegative()) {
                    Set<ItemNode> merge = orMerge(left.results, right.results, true);
                    return new Result(merge, false);
                }

                //case 2: left is negative and right is positive
                if (left.isNegative() && !right.isNegative()) {
                    combined.addAll(left.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> matchesQuery(result, parseTree.getRight()) == 0).collect(Collectors.toSet());
                    return new Result(filter, true);
                }

                //case 3: left is positive and right is negative
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(right.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> matchesQuery(result, parseTree.getLeft()) == 0).collect(Collectors.toSet());
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

                //case 2: left is negative and right is positive
                if (left.isNegative() && !right.isNegative()) {
                    combined.addAll(right.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> matchesQuery(result, parseTree.getLeft()) == 0).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 3: left is positive and right is negative
                if (!left.isNegative() && right.isNegative()) {
                    combined.addAll(left.results);
                    Set<ItemNode> filter = combined.stream().filter(result -> matchesQuery(result, parseTree.getRight()) == 0).collect(Collectors.toSet());
                    return new Result(filter, false);
                }

                //case 4: both negative queries (perform OR merge and percolate negation)
                if (left.isNegative() && right.isNegative()) {
                    Set<ItemNode> merge = orMerge(left.results, right.results, false);
                    return new Result(merge, true);
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

    private Set<ItemNode> orMerge(Set<ItemNode> left, Set<ItemNode> right, boolean preserveRankings) {
        if (left.isEmpty())
            return right;
        if (right.isEmpty())
            return left;
        if (preserveRankings) {
            HashSet<ItemNode> combined = new HashSet<>();

            //sort result sets to speed up relevance merges to O(n log n) instead of O(n^2)
            left = left.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)).collect(Collectors.toSet());
            right = right.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)).collect(Collectors.toSet());

            Iterator<ItemNode> leftIter = left.iterator(), rightIter = right.iterator();
            ItemNode leftCurrent = advance(leftIter), rightCurrent = advance(rightIter);

            //combine like identifiers
            while (leftCurrent != null && rightCurrent != null) {
                if (leftCurrent.identifier.equals(rightCurrent.identifier)) {
                    leftCurrent.merge(rightCurrent);
                    combined.add(leftCurrent);
                    leftCurrent = advance(leftIter);
                    rightCurrent = advance(rightIter);
                } else if (leftCurrent.identifier.compareTo(rightCurrent.identifier) < 0) {
                    combined.add(leftCurrent);
                    leftCurrent = advance(leftIter);
                } else {
                    combined.add(rightCurrent);
                    rightCurrent = advance(rightIter);
                }
            }

            //get the rest from whichever one is not done yet
            while (leftCurrent != null) {
                combined.add(leftCurrent);
                leftCurrent = advance(leftIter);
            }

            while (rightCurrent != null) {
                combined.add(rightCurrent);
                rightCurrent = advance(rightIter);
            }

            return combined;

        } else {
            HashSet<ItemNode> combined = new HashSet<>();
            combined.addAll(left);
            combined.addAll(right);
            return combined;
        }
    }

    private ItemNode advance(Iterator<ItemNode> iterator) {
        if (iterator.hasNext())
            return iterator.next();
        return null;
    }

    private Set<ItemNode> andMerge(Set<ItemNode> left, ParseTree right) {
        Set<ItemNode> combined = new HashSet<>();
        combined.addAll(left);
        return combined.stream().filter(result -> matchesQuery(result, right) == 1).collect(Collectors.toSet());
    }

    private int matchesQuery(ItemNode item, ParseTree tree) {
        if (tree == null) {
            if (tree.getLeft() == null)
                throw new IllegalStateException("null check on matchesQuery");
            else return matchesQuery(item, tree.getLeft());
        }
        switch (tree.getType()) {
            case OR:
                return matchesQuery(item, tree.getLeft()) + matchesQuery(item, tree.getRight());
            case AND: {
                int leftValue = matchesQuery(item, tree.getLeft()), rightValue = matchesQuery(item, tree.getRight());
                if (leftValue > 0 && rightValue > 0) {
                    return leftValue + rightValue;
                } else {
                    return 0;
                }
            }
            case PHRASE:
            case WORD:
                if (tree.isNegative()) {
                    int result = index.find(tree.getWord(), item.identifier);
                    if (result > 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (!tree.isPlus()) {
                    for (String s : ignoredWords) {
                        if (s.equals(tree.getWord().toString())) {
                            //common word. ignore element
                            return 0;
                        }
                    }
                }

                return index.find(tree.getWord(), item.identifier);
            default:
                throw new IllegalStateException("Illegal token in tree");
        }

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
