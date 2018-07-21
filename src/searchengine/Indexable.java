package searchengine;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface Indexable {

    int TITLE_RELEVANCE = 5;
    int HEADER_RELEVANCE = 3;
    int TEXT_RELEVANCE = 1;
    int DETAIL_STEM_LENGTH = 80;
    String[] FALSE_ENDINGS = new String[]{" mr.", " mrs.", " ms.", " dr", " rev.", " prof.", " messrs.", " mmes.", " hon.", " st.", " gen.", " col.", " lt.", " capt."};

    Timestamp lastIndexed();
    List<RankedString> getIndexTextSets();

    Identifier getUniqueIdentifier();

    default boolean containsString(String word) {
        for (RankedString s: getIndexTextSets()) {
            if (s.getString().toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    default TextFlow getDetailText(List<String> wordsToMatch) {
        String previewString = "";
        boolean first = true;
        List<RankedString> strings = getIndexTextSets();
        strings = strings.size() == 1 ? new ArrayList<>() : strings.subList(1, strings.size());
        //replace < with \< and \ with \\
        strings.forEach(rankedString -> rankedString.setString(rankedString.getString().replaceAll("<", "\\<").replaceAll("\\\\", "\\\\")));
        wordsToMatch = wordsToMatch.stream().map(s -> s.replaceAll("<", "\\<").replaceAll("\\\\", "\\\\")).collect(Collectors.toList());
        for (RankedString pageStringR : strings) {
            if (!first)
                previewString += " ... ";
            first = false;
            String pageString = pageStringR.getString();
            String pageStringLC = pageString.toLowerCase();
            List<Integer[]> intervals = new ArrayList<>();
            for (String word : wordsToMatch) {
                int index = pageStringLC.indexOf(word);
                //make sure the word isn't just a stem in another word
                while (index != -1 && ((index != 0 && Character.isLetterOrDigit(pageStringLC.charAt(index - 1)))
                        || (index != pageStringLC.length() - word.length() && Character.isLetterOrDigit(pageStringLC.charAt(index + word.length()))))) {
                    index = pageStringLC.indexOf(word, index + 1);
                }
                if (index != -1) {
                    Integer[] interval = new Integer[4]; //beginning, end, word index, word length
                    interval[0] = Math.max(0, index - DETAIL_STEM_LENGTH);
                    interval[2] = Math.min(index, DETAIL_STEM_LENGTH);
                    interval[3] = word.length();
                    //find the beginning of a sentence if we can (unless we back up to the start of the page)
                    if (interval[2] == DETAIL_STEM_LENGTH) {
                        for (int i = interval[0]; i < index - 1; i++) {
                            if (Character.isWhitespace(pageString.charAt(i + 1)) && Character.toString(pageString.charAt(i)).matches("[.?!\"\\s]")) {
                                boolean badEnding = false;
                                for (String falseEnding :
                                        FALSE_ENDINGS) {
                                    int start = i + 1 - falseEnding.length();
                                    if (start >= 0 && pageString.substring(start, i + 1).equalsIgnoreCase(falseEnding)) {
                                        badEnding = true;
                                    }
                                }
                                if (!badEnding) {
                                    interval[0] = i + 2;
                                    interval[2] = index - interval[0];
                                    break;
                                }
                            }
                        }
                    }
                    interval[1] = Math.min(index + interval[3] + DETAIL_STEM_LENGTH, pageString.length());
                    //find the end of a sentence if we can (unless we reach the end of the page in the character allotment)
                    if (!(interval[1] == pageString.length()))
                        for (int i = interval[1] - 1; i >= index + interval[3]; i--) {
                            if (Character.isWhitespace(pageString.charAt(i + 1)) && Character.toString(pageString.charAt(i)).matches("[.?!\"\\s]")) {
                                boolean badEnding = false;
                                for (String falseEnding :
                                        FALSE_ENDINGS) {
                                    int start = i + 1 - falseEnding.length();
                                    if (start >= 0 && pageString.substring(start, i + 1).equalsIgnoreCase(falseEnding)) {
                                        badEnding = true;
                                    }
                                }
                                if (!badEnding) {
                                    interval[1] = i + 1;
                                    break;
                                }
                            }
                        }
                    intervals.add(interval);
                }
            }

            //sort by earliest start index
            intervals.sort(Comparator.comparing(o -> o[0]));
            for (int i = 0; i < intervals.size(); i++) {
                boolean mergeConflictCase = false;
                Integer[] interval = intervals.get(i);
                int startIndex = interval[0];
                if (i > 0 && startIndex < intervals.get(i - 1)[1]) {
                    startIndex = intervals.get(i - 1)[1];
                    //check if next word came in previous interval
                    int newWordIndexInExisting = previewString.length() - (startIndex - interval[0]) + interval[2];
                    int existingChars = previewString.length() - newWordIndexInExisting;
                    if (newWordIndexInExisting < previewString.length()) {
                        previewString = previewString.substring(0, newWordIndexInExisting) + "<b>"
                                + previewString.substring(newWordIndexInExisting, newWordIndexInExisting + Math.min(existingChars, interval[3]))
                                + pageString.substring(startIndex, startIndex + interval[3] - Math.min(interval[3], existingChars)) + "</b>"
                                + pageString.substring(startIndex + interval[3] - existingChars, interval[1]);
                        mergeConflictCase = true;
                    }
                }
                if (!mergeConflictCase) {
                    //add ellipsis unless the new intervals overlap or border each other
                    if (i > 0 && startIndex > intervals.get(i - 1)[1])
                        previewString += " ... ";
                    previewString += pageString.substring(startIndex, interval[0] + interval[2])
                            + "<b>" + pageString.substring(interval[0] + interval[2], interval[0] + interval[2] + interval[3]) + "</b>"
                            + pageString.substring(interval[0] + interval[2] + interval[3], interval[1]);
                }
            }
        }
        if (previewString.length() == 0) {
            //none of the words were found, just use the beginning of the index set (means all the words are in the title)
            first = true;
            for (RankedString indexed : strings) {
                if (!first) {
                    previewString += " ... ";
                } else first = false;
                previewString += indexed.getString().substring(0, Math.min(DETAIL_STEM_LENGTH * 5 - previewString.length(), indexed.getString().length()));
                if (previewString.length() >= DETAIL_STEM_LENGTH * 5 - 5)
                    break;
            }
        }
        TextFlow returnVal = new TextFlow();
        boolean switchType = false;
        while (previewString.length() > 0) {
            if (switchType) getBold(returnVal, previewString);
            else getNormal(returnVal, previewString);
            switchType = !switchType;
        }
        return returnVal;
    }

    private String getBold(TextFlow flow, String previewString) {
        int index = previewString.indexOf("</b>");
        if (index == -1)
            index = previewString.length();
        while (index != 0 && previewString.charAt(index - 1) == '\\' && index != previewString.length()) {
            index = previewString.indexOf("</b>", index);
            if (index == -1) index = previewString.length();
        }
        flow.getChildren().add(new Text(previewString.substring(0, index)) {{setFont(Font.font(getFont().getFamily(), FontWeight.BOLD, getFont().getSize()));}});
        return previewString.substring(index + 3);
    }

    private String getNormal(TextFlow flow, String previewString) {
            int index = previewString.indexOf("<b>");
            if (index == -1)
                index = previewString.length();
            while (index != 0 && previewString.charAt(index - 1) == '\\' && index != previewString.length()) {
                index = previewString.indexOf("<b>", index);
                if (index == -1) index = previewString.length();
            }
            flow.getChildren().add(new Text(previewString.substring(0, index).replaceAll("\\\\\\\\", "\\").replaceAll("\\\\<", "<")));
            return previewString.substring(index + 3);
    }

    void launch();
}
