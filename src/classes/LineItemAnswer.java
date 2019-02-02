package classes;

import java.util.Set;

public class LineItemAnswer implements Answer {

    private final Set<String> correctAnswers;
    private boolean caseSensitive = false;
    private boolean enableSpellCheck = true;      //User level spell checker
    private boolean enableSpellCorrection = true; //Automatic correction of spelling errors when answer is checked
    private boolean ignoreNonAlphanumeric = true;
    private String response;

    LineItemAnswer(String response) {
        correctAnswers = null;
        this.response = response;
    }

    public LineItemAnswer(Set<String> correctAnswers, boolean caseSensitive, boolean enableSpellCheck, boolean enableSpellCorrection, boolean ignoreNonAlphanumeric) {
        this.correctAnswers = correctAnswers;
        this.setCaseSensitive(caseSensitive);
        this.setEnableSpellCheck(enableSpellCheck);
        this.setEnableSpellCorrection(enableSpellCorrection);
        this.setIgnoreNonAlphanumeric(ignoreNonAlphanumeric);
    }

    @Override
    public boolean verify(Answer response) {
        if (!(response instanceof LineItemAnswer))
            throw new IllegalArgumentException("mismatched answer class - expected Line Item Answer");
        String test = ((LineItemAnswer) response).getResponse();
        if (!caseSensitive) test = test.toLowerCase();
        if (ignoreNonAlphanumeric) test = removeAllNonAlphanumeric(test);
        for (String correctAns : correctAnswers) {
            if (!caseSensitive) correctAns = correctAns.toLowerCase();
            if (ignoreNonAlphanumeric) correctAns = removeAllNonAlphanumeric(correctAns);
            if (test.equals(correctAns))
                return true;
            if (enableSpellCorrection && SpellCheckEngine.effectiveMatch(correctAns, test))
                return true;
        }
        return false;
    }

    private String removeAllNonAlphanumeric(String s) {
        //remove all symbols and collapse multi-spaces to one space
        return s.replaceAll("^[a-zA-Z0-9\\s]", "").replaceAll("\\s+", " ");
    }

    public boolean isIgnoreNonAlphanumeric() {
        return ignoreNonAlphanumeric;
    }

    public void setIgnoreNonAlphanumeric(boolean ignoreNonAlphanumeric) {
        this.ignoreNonAlphanumeric = ignoreNonAlphanumeric;
    }

    public Set<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isEnableSpellCheck() {
        return enableSpellCheck;
    }

    public void setEnableSpellCheck(boolean enableSpellCheck) {
        this.enableSpellCheck = enableSpellCheck;
    }

    public boolean isEnableSpellCorrection() {
        return enableSpellCorrection;
    }

    public void setEnableSpellCorrection(boolean enableSpellCorrection) {
        this.enableSpellCorrection = enableSpellCorrection;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
