package parking;

import classes.setbuilder.Set;
import main.User;

import java.util.ArrayList;

public class ParkingSpot implements Comparable<ParkingSpot> {

    private String spotNumber;
    private String lotName;
    private ArrayList<User> occupants;
    private boolean motorcycle;
    private boolean electricCar;
    private boolean handicap;
    private boolean facultyOnly;

    private Set customSetRestriction;
    private ArrayList<String> customPreferenceSortRegex;

    public ParkingSpot(String spotNumber, String lotName, boolean motorcycle, boolean electricCar, boolean handicap, boolean facultyOnly, Set customSetRestriction) {
        this.setSpotNumber(spotNumber);
        this.lotName = lotName;
        this.occupants = new ArrayList<>();
        this.motorcycle = motorcycle;
        this.electricCar = electricCar;
        this.handicap = handicap;
        this.facultyOnly = facultyOnly;
        this.customSetRestriction = customSetRestriction;
        setCustomPreferenceSortRegex(new ArrayList<>());
    }

    public ParkingSpot(String spotNumber, String lotName, boolean motorcycle, boolean electricCar, boolean handicap, boolean facultyOnly) {
        this.setSpotNumber(spotNumber);
        this.lotName = lotName;
        this.occupants = new ArrayList<>();
        this.motorcycle = motorcycle;
        this.electricCar = electricCar;
        this.handicap = handicap;
        this.facultyOnly = facultyOnly;
    }


    @Override
    public int compareTo(ParkingSpot o) {
        if (getCustomPreferenceSortRegex() == null) {
            return 0;
        }
        int countMatches = 0; //if a space matches more than one regex, throw an error.
        for (String regex :
                getCustomPreferenceSortRegex()) {
            try {
                boolean regexMatchesThis = regexMatches(this, regex);
                boolean regexMatchesOther = regexMatches(o, regex);
                if (regexMatchesThis && regexMatchesOther) {
                    return parseBracketNumbers(this, o);
                }
                if (regexMatchesOther) {
                    return 1;
                }
                if (regexMatchesThis) {
                    return -1;
                }
            } catch (IllegalRegexException e) {
                e.printStackTrace(); //TODO throw exception upwards
            }
        }
        return 0;
    }

    private int parseBracketNumbers(ParkingSpot parkingSpot, ParkingSpot o) {
        return -1;
    }

    private boolean regexMatches(ParkingSpot o, String regex) throws IllegalRegexException {
        String testRegex = "";
        if (testRegex.indexOf("[") == -1) {
            return o.spotNumber.equals(regex);
        } else {
            for (int i = 0; i < regex.length(); i++) {
                if (!(regex.charAt(i) == '[')) {
                    if (regex.charAt(i) != o.spotNumber.charAt(i))
                        return false;
                } else {
                    i++;
                    if (i == regex.length()) {
                        throw new IllegalRegexException("unclosed bracket placeholder");
                    }
                    if (regex.charAt(i) == ']') {
                        testRegex += "[0-9A-Za-z]+";
                    }
                    String lowerBound = "";
                    while (regex.charAt(i) != '-') {
                        if (i == regex.length()) {
                            throw new IllegalRegexException("unclosed bracket placeholder");
                        }
                        if (regex.charAt(i) == ']')
                            throw new IllegalRegexException("only one value specified, requires two values separated by a dash");
                        if (!Character.toString(regex.charAt(i)).matches("[A-Za-z0-9]")) {
                            throw new IllegalRegexException("illegal character in value range");
                        }
                        lowerBound += regex.charAt(i);
                        i++;
                    }
                    assert regex.charAt(i) == '-';
                    i++;
                    String upperBound = "";
                    while (regex.charAt(i) != ']') {
                        if (i == regex.length()) {
                            throw new IllegalRegexException("unclosed bracket placeholder");
                        }
                        if (regex.charAt(i) == '-')
                            throw new IllegalRegexException("too many arguments in value range");
                        if (!Character.toString(regex.charAt(i)).matches("[A-Za-z0-9]")) {
                            throw new IllegalRegexException("illegal character in value range");
                        }
                        upperBound += regex.charAt(i);
                        i++;
                    }
                    if (lowerBound.length() == 0) {
                        throw new IllegalRegexException("no value specified for beginning of range");
                    }
                    if (upperBound.length() == 0) {
                        throw new IllegalRegexException("no value specified for end of range");
                    }
                    if (!lowerBound.matches("[A-Za-z]+|[0-9]+") || !upperBound.matches("^[A-Za-z]*$ | ^[0-9]*$")) {
                        throw new IllegalRegexException("cannot have both letters and numbers in range bounds");
                    }
                    if ((lowerBound.matches("^[A-Za-z]*$") && upperBound.matches("^[0-9]*$")) || (upperBound.matches("^[A-Za-z]*$") && lowerBound.matches("^[0-9]*$")))
                        throw new IllegalRegexException("cannot compare letters to numbers in range check");
                    //range check
                    if (lowerBound.matches("^[0-9]*$")) {
                        assert upperBound.matches("^[0-9]*$");

                    }
                }
            }
        }
        return true;
    }

    public void addRegex(int index, String regex) {

    }

    public void addRegex(String regex) {
        addRegex(getCustomPreferenceSortRegex().size(), regex);
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public ArrayList<String> getCustomPreferenceSortRegex() {
        return customPreferenceSortRegex;
    }

    public void setCustomPreferenceSortRegex(ArrayList<String> customPreferenceSortRegex) {
        this.customPreferenceSortRegex = customPreferenceSortRegex;
    }
}
