package classes;

import main.Root;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by S507098 on 4/28/2017.
 */
public class Form extends Assignment implements canDisplay {

    private ArrayList<FormElement> elements;
    private HashMap<Integer, ArrayList<String>> records;

    public Form(String name, String format) {
        super(name);
        records = new HashMap();
        setElements(new ArrayList<>());
        String split[] = format.split("%1sp");
        for (int i = 0; i < split.length; i++) {
            String[] inner = split[i].split("%2sp");
            String trunc[];
            try {
                trunc = new String[inner.length - 2];
                for (int j = 2; j < inner.length; j++) {
                    trunc[j - 2] = inner[j];
                }
            } catch (NegativeArraySizeException ex) {
                trunc = null;
            }
            getElements().add(new FormElement(inner[1], inner[0], trunc));
        }
        setRecords(new HashMap());
    }

    @Override
    public void addTo(JComponent c) {
        c.getGraphics().drawString(this.getName(), c.getWidth() / 10, c.getHeight() / 10);
        for (int i = 0; i < getElements().size(); i++) {
            c.add(getElements().get(i).getQuestion());
            for (int j = 0; j < getElements().get(i).getAnswerSpace().length; j++) {
                c.add(getElements().get(i).getAnswerSpace()[j]);
            }
        }
        JButton submit = new JButton("Submit");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitRecord();
            }
        });
    }

    private void submitRecord(){
        ArrayList<String> response = new ArrayList<String>();
        for(FormElement jc : this.elements){
            String ans = "";
            for (int i = 0; i < jc.getAnswerSpace().length; i++) {
                ans += (i == 0) ? jc.getAnswerSpace()[i] : "%1sp" + jc.getAnswerSpace()[i];
            }
            response.add(ans);
        }
        records.put(Root.getActiveID(), response);
    }

    public ArrayList<FormElement> getElements() {
        return elements;
    }

    public void setElements(ArrayList<FormElement> elements) {
        this.elements = elements;
    }

    public HashMap<Integer, ArrayList<String>> getRecords() {
        return records;
    }

    public void setRecords(HashMap<Integer, ArrayList<String>> records) {
        this.records = records;
    }
}

class FormElement {
    private JLabel question;
    private Component[] answerSpace;

    public FormElement(String question, String type, String[] options) {
        int length = 1;
        if (options != null) {
            length = Math.max(length, options.length);
        }
        this.setQuestion(new JLabel(question));
        this.setAnswerSpace(new Component[length]);
        if (type.equals("/%text%/")) {
            getAnswerSpace()[0] = new JTextField();
        } else if (type.equals("/%radio%/")) {
            int index = 0;
            for (String option : options) {
                getAnswerSpace()[index] = new JRadioButton(options[index]);
                index++;
            }
        } else if (type.equals("/%check%/")) {
            int index = 0;
            for (String option : options) {
                getAnswerSpace()[index] = new JCheckBox(options[index]);
                index++;
            }
        } else if (type.equals("/%menu%/")) {
            setAnswerSpace(new Component[1]);
            getAnswerSpace()[0] = new JComboBox(options);
        }
    }

    public JLabel getQuestion() {
        return question;
    }

    public void setQuestion(JLabel question) {
        this.question = question;
    }

    public Component[] getAnswerSpace() {
        return answerSpace;
    }

    public void setAnswerSpace(Component[] answerSpace) {
        this.answerSpace = answerSpace;
    }
}