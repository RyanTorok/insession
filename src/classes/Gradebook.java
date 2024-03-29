package classes;

import main.Student;
import main.User;

import java.util.ArrayList;
import java.util.HashMap;

public class Gradebook {

    private ArrayList<HashMap<User, StudentGrades>> gradebook;
    private ArrayList<GradeCategory> categories;
    private HashMap<String, Grade> mnemonics;

    public Gradebook() {
        gradebook = new ArrayList<>();
        categories = new ArrayList<>();
        mnemonics = new HashMap<>();
    }

    public ArrayList<HashMap<User, StudentGrades>> getGradebook() {
        return gradebook;
    }

    public HashMap<User, StudentGrades> getMPGradebook(int markingPeriod) {
        return gradebook.get(markingPeriod);
    }

    public StudentGrades get(Integer markingPeriod, User activeUser) {
        if (activeUser == null)
            return null;
        HashMap<User, StudentGrades> periodMap = null;
        if (gradebook.size() > markingPeriod)
        periodMap = gradebook.get(markingPeriod);
        if (periodMap != null)
            return periodMap.get(activeUser);
        return null;
    }

    public ArrayList<GradeCategory> getCategories() {
        return categories;
    }

    public HashMap<String, Grade> getMnemonics() {
        return mnemonics;
    }
    
    public void addMnemonic(String mnemonic, Grade value) {
        mnemonics.put(mnemonic, value);
    }

    public void addMnemonic(String mnemonic, Double value) {
        mnemonics.put(mnemonic, new Grade(value));
    }
}
