package classes;

public class SpellCheckEngine {

    private static final int DEFAULT_TOLERANCE = 3;

    public static boolean effectiveMatch(String expected, String actual) {
        return effectiveMatch(expected, actual, DEFAULT_TOLERANCE);
    }

    public static boolean effectiveMatch(String expected, String actual, int tolerance) {
        return getDistance(expected, actual) <= tolerance;
    }

    public static boolean isValidWord(String s) {
        return true; //TODO
    }

    //Damerau–Levenshtein distance algorithm
    public static int getDistance(final String expected, final String actual) {
        return new DamerauLevenshteinAlgorithm(1, 1, 1, 1).execute(expected, actual);
    }
}