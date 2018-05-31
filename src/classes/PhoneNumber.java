package classes;

/**
 * Created by S507098 on 4/27/2017.
 */
public class PhoneNumber {
    private long number;
    private int type;

    public PhoneNumber(long number, int type) {
        this.number = number;
        this.type = type;
    }

    public long returnAsLong() {
        return number;
    }

    public String returnWithType() {
        return type + ": " + returnAsString();
    }

    public String returnAsStringWithoutPunctuation() {
        return Long.toString(number);
    }

    public PhoneNumber(String pn, int type) {
        pn = removePunctuation(pn);
        try{
            number = Long.parseLong(pn);
        } catch (NumberFormatException ex){
            if(pn.length() == 0){
                number = 0;
            } else {
                ex.printStackTrace();
            }
        }
    }

    private String removePunctuation(String pn) {
        for (int i = pn.length()-1; i >= 0; i--) {
            try{
                if(pn.charAt(i) > 57 || pn.charAt(i) < 48){
                    pn = pn.substring(0, i);
                }
            } catch (IndexOutOfBoundsException e){
                continue;
            }
        }
        return pn;
    }


    public String returnAsString() {
        String temp = returnAsStringWithoutPunctuation();
        String newString = "";
        int digitct = 0;
        boolean checked1stdigit = false;
        for (int i = 0; i < temp.length(); i++) {
            newString += temp.charAt(i);
            switch (digitct) {
                case 0: {
                    checked1stdigit = true;
                    if (newString.charAt(0) == '1' && !checked1stdigit) {
                        newString += "-";
                        digitct--;
                    }
                }
                case 2:
                    newString = "(" + newString + ") ";
                    break;
                case 5:
                    newString += "-";

            }
            digitct++;
        }
        return newString;
    }
}
