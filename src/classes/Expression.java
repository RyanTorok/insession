package classes;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

/**
 * Created by S507098 on 4/17/2017.
 */
public class Expression {

    private ArrayList<String> splitexp;

    public Expression(String s) {
        splitexp = new ArrayList();
        s = s.replaceAll("sqrt", "q");
        s = s.replaceAll("sin", "s");
        s = s.replaceAll("cos", "c");
        s = s.replaceAll("tan", "t");
        s = s.replaceAll("pi", "p");
        s = s.replaceAll("log", "l");
        s = s.replaceAll("ln", "n");
        s = s.replaceAll("abs", "a");
        for (int i = 0; i < s.length(); ) {
            String temp = "";
            while (i < s.length() && s.charAt(i) != 'x' && s.charAt(i) != '+' && s.charAt(i) != '-' && s.charAt(i) != '*' && s.charAt(i) != '/' && s.charAt(i) != '^' && s.charAt(i) != '(' && s.charAt(i) != ')' && s.charAt(i) != 'q' && s.charAt(i) != 's' && s.charAt(i) != 'c' && s.charAt(i) != 't' && s.charAt(i) != 'l' && s.charAt(i) != ')' && s.charAt(i) != 'n' && s.charAt(i) != 'a' && s.charAt(i) != 'p' && s.charAt(i) != 'e') {
                temp += s.charAt(i);
                i++;
            }
            splitexp.add(temp);
            removeSpaces();
            if (i >= s.length()) {
                break;
            }
            splitexp.add(Character.toString(s.charAt(i++)));
        }
    }

    private void removeSpaces() {
        for (int i = 0; i < splitexp.size(); i++) {
            if (splitexp.get(i).length() == 0) {
                splitexp.remove(i);
            }
        }
    }

    private Expression(ArrayList<String> s) {
        splitexp = s;
    }

    public double eval(double x) { //Do not attempt to understand this method.
        for (int i = 0; i < splitexp.size(); i++) {
            if (splitexp.get(i).equals("x")) {
                splitexp.set(i, Double.toString(x));
            }
            if (splitexp.get(i).equals("p")) {
                splitexp.set(i, Double.toString(Math.PI));
            }
        }
        if (splitexp.get(0).equals("-")) { //prevents exception for negative numbers in parenthesis
            splitexp.add(0, "0");
        }
        while (splitexp.indexOf("(") != -1) {
            ArrayList<String> sub = new ArrayList();
            int paren = 0;
            int endIndex = -1;
            for (int i = splitexp.indexOf("("); i < splitexp.size(); i++) {
                if (splitexp.get(i).equals("(")) {
                    paren++;
                }
                if (splitexp.get(i).equals(")")) {
                    paren--;
                }
                if (paren == 0) {
                    endIndex = i;
                    break;
                }
            }
            for (int i = splitexp.indexOf("(") + 1; i < endIndex; i++) {
                sub.add(splitexp.get(i));
            }
            Double temp = new Expression(sub).evaluate(x);
            splitexp.add(splitexp.indexOf("("), Double.toString(temp));
            for (int i = endIndex + 1; i >= splitexp.indexOf("(") && splitexp.indexOf("(") > -1; i--) {
                splitexp.remove(i);
            }
        }
        boolean number = false;
        for (int i = 0; i < splitexp.size(); i++) {
            try {
                Double.parseDouble(splitexp.get(i));
                if (number) {
                    splitexp.add(i, "*");
                    i++;
                }
                number = true;
            } catch (NumberFormatException e) {
                number = false;
            }
        }
        char[] precedence = {'s', 'c', 't', 'l', 'n', '^', 'q'};
        for (int i = 0; i < precedence.length; i++) {
            for (int j = splitexp.size() - 1; j >= 0; j--) {
                if (splitexp.get(j).charAt(0) == precedence[i]) {
                    switch (splitexp.get(j).charAt(0)) {
                        case 's':
                            splitexp.set(j + 1, Double.toString(Math.sin(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                        case 'c':
                            splitexp.set(j + 1, Double.toString(Math.cos(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                        case 't':
                            splitexp.set(j + 1, Double.toString(Math.tan(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                        case 'l':
                            splitexp.set(j + 1, Double.toString(Math.log10(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                        case 'n':
                            splitexp.set(j + 1, Double.toString(Math.log(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                        case '^':
                            splitexp.set(j + 1, Double.toString(Math.pow(Double.parseDouble(splitexp.get(j - 1)), Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            splitexp.remove(j - 1);
                            break;
                        case 'q':
                            splitexp.set(j + 1, Double.toString(Math.sqrt(Double.parseDouble(splitexp.get(j + 1)))));
                            splitexp.remove(j);
                            break;
                    }
                }
            }
        }
        for (int i = 0; i < splitexp.size(); i++) {
            if (splitexp.get(i).charAt(0) == '*') {
                splitexp.set(i - 1, Double.toString(Double.parseDouble(splitexp.get(i - 1)) * Double.parseDouble(splitexp.get(i + 1))));
                splitexp.remove(i + 1);
                splitexp.remove(i);
                i--;
                continue;
            }
            if (splitexp.get(i).charAt(0) == '/') {
                splitexp.set(i - 1, Double.toString(Double.parseDouble(splitexp.get(i - 1)) / Double.parseDouble(splitexp.get(i + 1))));
                splitexp.remove(i + 1);
                splitexp.remove(i);
                i--;
                continue;
            }
            if (splitexp.get(i).charAt(0) == '%') {
                splitexp.set(i - 1, Double.toString(Double.parseDouble(splitexp.get(i - 1)) % Double.parseDouble(splitexp.get(i + 1))));
                splitexp.remove(i + 1);
                splitexp.remove(i);
                i--;
            }
        }
        for (int i = 0; i < splitexp.size(); i++) {
            if (splitexp.get(i).charAt(0) == '+') {
                splitexp.set(i - 1, Double.toString(Double.parseDouble(splitexp.get(i - 1)) + Double.parseDouble(splitexp.get(i + 1))));
                splitexp.remove(i + 1);
                splitexp.remove(i);
                i--;
                continue;
            }
            if (splitexp.get(i).equals("-")) {
                splitexp.set(i - 1, Double.toString(Double.parseDouble(splitexp.get(i - 1)) - Double.parseDouble(splitexp.get(i + 1))));
                splitexp.remove(i + 1);
                splitexp.remove(i);
                i--;
            }
        }
        //splitexp.size() is 1 at this point.
        return Double.parseDouble(splitexp.get(0));
    }

    public double evaluate(double x) {
        try {
            return eval(x);
        } catch (Throwable e) {
            return Double.MIN_VALUE;
        }
    }

    public int evaluateFloor(double x) {
        return (int) evaluate(x);
    }

    public int evaluateCeil(double x) {
        return (int) (Math.ceil(evaluate(x)));
    }

    public int evaluateRound(double x) {
        return (int) (Math.round(evaluate(x)));
    }

    public double evaluate(double[] vals) {
        if(vals.length >= 6){
            throw new UnsupportedOperationException("May use up to five variables.");
        }
        char[] arr = {'x', 'y', 'z', 'w', 'v'};
        for (int j = 0; j < splitexp.size(); j++) {
            for (int k = 0; k < arr.length; k++) {
                if (splitexp.get(j).equals(Character.toString(arr[k]))) {
                        splitexp.set(j, Double.toString(vals[k]));
                }
            }
        }
        return evaluate(0); //all variables are replaced, so x could now have any value without affecting the result.
    }
}
