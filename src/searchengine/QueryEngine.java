package searchengine;

import main.Root;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryEngine {

    private static final long MAX_STEM_QUERY_TIME_NANOS = 1000000; // 1 ms
    private Index index;
    private static final String[] ignoredWords = {"where", "what", "how", "a", "an", "of", "i", "the"};
    //"encrypted" for code censorship (i.e. characters adjusted by one letter)
    private static String[] safeSearchFilterWords = {"tiju", "dsbq", "ejdl", "eidlifbe", "tijuifbe", "btt", "bttipmf", "ebno", "gvdl", "npuifsgvdlfs", "tijuifbe", "ebnnju", "cjudi", "ifmm", "hpeebno", "hpeebnoju"};
    private boolean safeSearchOn = false;
    private Set<ItemNode> filterItems;
    private long lastQueryTimeNanos;
    private HashSet<String> allMeaningfulWords;
    private List<String> itemTitles;
    private List<String> textExcerpts;
    private WeightedPredictor weightedPredictor;
    private List<Tag> tags;
    private FilterSet filters; //temporary field during queries

    public QueryEngine(Index index) {
        this.index = index;
        for (int i = 0; i < safeSearchFilterWords.length; i++) {
            safeSearchFilterWords[i] = decCharacter(safeSearchFilterWords[i]);
        }
        lastQueryTimeNanos = 0;
        weightedPredictor = WeightedPredictor.read();
    }

    public static Collection<? extends Collection<Indexable>> getPrimaryIndexSets() {
        HashSet<Collection<Indexable>> set = new HashSet<>();
        return set; //TODO
    }

    private String decCharacter(String encoding) {
        char[] arr = encoding.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c :
                arr) {
            builder.append(c == 'a' ? 'z' : c - 1);
        }
        return builder.toString();
    }

    public static String[] getIgnoredWords() {
        return ignoredWords;
    }

    public static String[] getSafeSearchFilterWords() {
        return safeSearchFilterWords;
    }

    //performs a tentative query based on the most likely auto-completions of the last word
    public TreeSet<Identifier> incompleteQuery(ArrayList<String> textFillerStrings, FilterSet filters) {
        long queryTime = 0;
        TreeSet<Identifier> allResults = new TreeSet<>();
        for (String stem : textFillerStrings) {
            allResults.addAll(query(stem, filters));
            queryTime += getLastQueryTimeNanos();
            if (queryTime > MAX_STEM_QUERY_TIME_NANOS) {
                lastQueryTimeNanos = queryTime;
                return allResults;
            }
        }
        lastQueryTimeNanos = queryTime;
        return allResults;
    }

    public List<Identifier> query(String query, FilterSet filters) {
        this.filters = filters;
        if (query.length() == 0) {
            lastQueryTimeNanos = 0;
            tags = new ArrayList<>();
            return new ArrayList<>();
        }
        long startTime = System.nanoTime();
        Tokenizer tok = new Tokenizer(query);
        ParseTree parseTree = ParseTree.fromQuery(tok);
        tags = tok.getTags();
        Root.getActiveUser().search(query);
        Result result = getResults(parseTree);
        assert result != null;
        if (result.isNegative()) {
            lastQueryTimeNanos = System.nanoTime() - startTime;
            return new ArrayList<>();
        } else {
            List<Identifier> results = result.getResults().stream()
                    .sorted(Comparator.comparing(ItemNode::getRelevance))
                    .map(node -> node.identifier)
                    .collect(Collectors.toList());
            lastQueryTimeNanos = System.nanoTime() - startTime;
            return results;
        }
    }

    private Result getResults(ParseTree parseTree) {
        if (parseTree == null || parseTree.getType() == null) return new Result(new HashSet<>(), false);
        switch (parseTree.getType()) {
            case OR: {
                HashSet<ItemNode> combined = new HashSet<>();
                Result left = getResults(parseTree.getLeft()), right = getResults(parseTree.getRight());
                //sort result sets to speed up relevance merges to O(n log n) instead of O(n^2)
                //casts are necessary to avoid generification to Stream<Object> during sort
                left.results = ((Stream<ItemNode>) (left.results.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)))).collect(Collectors.toSet());
                right.results = ((Stream<ItemNode>) (right.results.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)))).collect(Collectors.toSet());

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
                Set<ItemNode> results = getIndex().getItems(parseTree.getWord()).stream().filter(this::matchesFilter).collect(Collectors.toSet());
                return new Result(results, parseTree.isNegative());
            }

            case PHRASE: {
                Set<ItemNode> results = getIndex().getItems(parseTree.getWord()).stream().filter(this::matchesFilter).collect(Collectors.toSet());
                results = results.stream().filter(itemNode -> itemNode.identifier.find(getIndex()).containsString(parseTree.getWord())).collect(Collectors.toSet());
                return new Result(results, parseTree.isNegative());
            }

            default: throw new IllegalStateException("Non-query element escaped parse tree");
        }
    }

    private boolean matchesFilter(ItemNode itemNode) {
        Identifier id = itemNode.identifier;

        //class and type lists contain what should be EXCLUDED

        //check type filter
        if (filters.types.contains(id.getType()))
            return false;

        //check class filter
        if (filters.classPds.contains(id.getBelongsTo()))
            return false;

        int secs = 86400; //seconds in a day

        //check time filter
        switch (filters.dateConstraint) {
            case TODAY: {
                long now = System.currentTimeMillis();
                long today = now - now % secs;
                long diff1 = id.getTime1() - today;
                long diff2 = id.getTime2() - today;
                return (diff1 > 0 && diff1 < secs) || (diff2 > 0 && diff2 < secs);
            }
            case PAST_WEEK: {
                Date idDate = new Date(id.getTime1() - id.getTime1() % secs);
                long now = System.currentTimeMillis();
                Date lastWeek = new Date(now - now % secs - secs * 7 - 1);
                return idDate.after(lastWeek) || new Date(id.getTime2() - id.getTime2() % secs).after(lastWeek);
            }
            case ON:
                long diff1 = id.getTime1() - filters.dateRestriction.getTime();
                long diff2 = id.getTime2() - filters.dateRestriction.getTime();
                return (diff1 > 0 && diff1 < secs) || (diff2 > 0 && diff2 < secs);
            case AFTER:
                return new Date(id.getTime1()).after(filters.dateRestriction) || new Date(id.getTime2()).after(filters.dateRestriction);
            case BEFORE:
                return (new Date(id.getTime1()).before(filters.dateRestriction) && (id.getTime1() != 0)) || (new Date(id.getTime2()).before(filters.dateRestriction) && (id.getTime2() != 0));
            case NONE:
                return true;
        }
        return true;
    }

    private Set<ItemNode> orMerge(Set<ItemNode> left, Set<ItemNode> right, boolean preserveRankings) {
        if (left.isEmpty())
            return right;
        if (right.isEmpty())
            return left;
        if (preserveRankings) {
            HashSet<ItemNode> combined = new HashSet<>();

            //sort result sets to speed up relevance merges to O(n log n) instead of O(n^2)
            //casts are necessary to avoid generification to Stream<Object> during sort
            left = ((Stream<ItemNode>) (left.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)))).collect(Collectors.toSet());
            right = ((Stream<ItemNode>) (right.stream().sorted(Comparator.comparing(itemNode -> itemNode.identifier)))).collect(Collectors.toSet());

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
        if (tree == null || tree.getType() == null) {
            if (tree != null && tree.getLeft() == null)
                return 0;
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
                    int result = getIndex().find(tree.getWord(), item.identifier);
                    if (result > 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (!tree.isPlus()) {
                    for (String s : getIgnoredWords()) {
                        if (s.equals(tree.getWord().toString())) {
                            //common word. ignore element
                            return 0;
                        }
                    }
                }

                return getIndex().find(tree.getWord(), item.identifier);
            default:
                throw new IllegalStateException("Illegal token in tree");
        }

    }

    public Index getIndex() {
        return index;
    }

    public boolean isSafeSearchOn() {
        return safeSearchOn;
    }

    public Set<ItemNode> getFilterItems() {
        return filterItems;
    }

    public long getLastQueryTimeNanos() {
        return lastQueryTimeNanos;
    }

    public HashSet<String> getAllMeaningfulWords() {
        return allMeaningfulWords;
    }

    public List<String> getItemTitles() {
        return itemTitles;
    }

    public List<String> getTextExcerpts() {
        return textExcerpts;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public WeightedPredictor getWeightedPredictor() {
        return weightedPredictor;
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
