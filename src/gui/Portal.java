/*package gui;

import classes.School;
import db.SQLMaster;
import exceptions.SoftlockException;
import main.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by 11ryt on 4/15/2017.
 */
/*public class Portal extends JPanel {

    private Integer state; // 0: Loading, 1: Main Screen, 2: Test
    private BufferedImage loadingImg;
    private Portal portal;
    private Color fadeOutColor = new Color(0, 0, 0, 0);
    private User newUser = null;
    private boolean[] displayingNAFields = {false, false, false, false, false, false, false, false, false, false, false};

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getState() == 0) { //loading screen?
            g.drawImage(loadingImg, getWidth() / 4, getHeight() / 2 - getHeight() / 5, this);
        }
    }

    public Portal(Graphics g) {
        setPortal(this);
        setState(0);
        try {
            String fn = "MainLogo.png";
            BufferedImage bf = ImageIO.read(new File(fn));
            if (bf == null) {
                throw new IOException();
            } else {
                loadingImg = bf;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    public User newUser() {
        Thread newUserThread = new Thread(new newUserWindow());
        try {
            newUserThread.start();
            newUserThread.join();
            newUserThread.run();
        } catch (InterruptedException e) {
            System.exit(0);
        }
        if (getNewUser() == null) {
            System.exit(0);
        }
        return getNewUser();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public Color getFadeOutColor() {
        return fadeOutColor;
    }

    public void setFadeOutColor(Color fadeOutColor) {
        this.fadeOutColor = fadeOutColor;
    }

    public User getNewUser() {
        return newUser;
    }

    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }

    public boolean[] getDisplayingNAFields() {
        return displayingNAFields;
    }

    public void setDisplayingNAFields(boolean[] displayingNAFields) {
        this.displayingNAFields = displayingNAFields;
    }

    private class newUserWindow extends JFrame implements Runnable {
        private ArrayList utAndSchool;
        private boolean displayingNASubmit = false;
        private boolean setupFormActive = true;

        @Override
        public void run() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (getNewUser() == null) {
                        Thread.currentThread().interrupt();
                        try {
                            throw new SoftlockException("Dependent Window Closed");
                        } catch (SoftlockException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
            try {
                setIconImage(ImageIO.read(new File("SubLogo.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setTitle("First Time Setup");
            setBounds(new Rectangle(getPortal().getWidth() / 4, getPortal().getHeight() / 8, getPortal().getWidth() / 2, getPortal().getHeight() * 4 / 5));
            LocalJPanel portal = new LocalJPanel(new GridBagLayout());
            portal.setBackground(new Color(233, 233, 255));
            portal.setVisible(true);
            this.add(portal);
            String[] accountTypes = {"Student", "Parent", "Teacher", "Administrator"};
            JTextField activationKeyField = new JTextField();
            activationKeyField.setColumns(20);
            JLabel activKeyFieldLabel = new JLabel("Enter Your Activation Key: ");
            activKeyFieldLabel.setLabelFor(activationKeyField);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 10, 10, 0);
            c.gridx = 0;
            c.gridy = 1;
            portal.add(activKeyFieldLabel, c);
            c.gridx = 1;
            portal.add(activationKeyField, c);
            activKeyFieldLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            activKeyFieldLabel.setVerticalTextPosition(SwingConstants.TOP);
            JButton submitActivationKey = new JButton("Submit");
            JButton schoolSelector = new JButton("Undefined School");
            JLabel schoolSelectionPrompt = new JLabel("Select your school:");
            JLabel invalidAKMessage = new JLabel("The activation key you entered does not match our records. Please check your user guide.");
            ArrayList<JComponent> newAccountFields = new ArrayList();
            ActionListener sSAL = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int usertype;
                    try {
                        usertype = (Integer) utAndSchool.get(1);
                    } catch (ClassCastException ex) {
                        usertype = 0; // sets to student by default if the received value from the database is improperly set.
                    }
                    portal.removeAll();
                    editTypeSelector(usertype, newAccountFields, portal);
                    portal.validate();
                    portal.repaint();
                }
            };
            submitActivationKey.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        SQLMaster.connectToOverallServer();
                    } catch (IllegalStateException ex) {
                        c.gridx = 0;
                        c.gridy = 2;
                        c.gridwidth = 3;
                        JLabel serverErrorMsg = new JLabel("Unable to connect to the server. Please try again later.");
                        serverErrorMsg.setFont(new Font("Segoe UI", Font.BOLD, 15));
                        portal.add(serverErrorMsg, c);
                        portal.validate();
                    }
                    utAndSchool = SQLMaster.lookUpActivationKey(activationKeyField.getText());
                    // the arraylist returned by the above call has the School object in position 0 and the usertype code in position 1. In the event an invalid code is entered by the user, the arraylist is set to [null, -1].
                    classes.School selectedSchool = (School) utAndSchool.get(0);
                    if (selectedSchool == null) {
                        invalidAKMessage.setLabelFor(activationKeyField);
                        invalidAKMessage.setFont(new Font("Segoe UI", Font.BOLD, 15));
                        c.gridwidth = 3;
                        c.gridx = 0;
                        c.gridy = 2;
                        portal.add(invalidAKMessage, c);
                        portal.validate();
                    } else {
                        GridBagConstraints c = new GridBagConstraints();
                        schoolSelectionPrompt.setFont(new Font("Segoe UI", Font.BOLD, 20));
                        schoolSelector.setText(selectedSchool.getName());
                        c.gridx = 0;
                        c.gridy = 2;
                        portal.add(schoolSelectionPrompt, c);
                        c.gridx = 1;
                        portal.add(schoolSelector, c);
                        schoolSelector.addActionListener(sSAL);
                        portal.validate();
                    }
                }
            });
            c.gridx = 2;
            portal.add(submitActivationKey, c);
            activationKeyField.setFont(new Font("Segoe UI", Font.BOLD, 20));
            activKeyFieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            activationKeyField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    schoolSelector.removeActionListener(sSAL);
                    portal.remove(schoolSelector);
                    portal.remove(invalidAKMessage);
                    portal.remove(schoolSelectionPrompt);
                    portal.repaint();
                    portal.validate();
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });


            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("First Name"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Middle Name"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Last Name"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Student ID"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Address"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Email"));
            newAccountFields.add(new PhoneField(0));
            newAccountFields.add(new JLabel("Home Phone"));
            newAccountFields.add(new PhoneField(1));
            newAccountFields.add(new JLabel("Cell Phone"));
            newAccountFields.add(new JTextField());
            newAccountFields.add(new JLabel("Create Username"));
            newAccountFields.add(new JPasswordField());
            newAccountFields.add(new JLabel("Create Password"));
            newAccountFields.add(new JPasswordField());
            newAccountFields.add(new JLabel("Confirm Password"));

            for (int i = 0; i < newAccountFields.size() / 2; i++) {
                newAccountFields.get(i).setEnabled(true);
                ((JTextField) newAccountFields.get(2 * i)).setEditable(true);
                ((JTextField) newAccountFields.get(2 * i)).setColumns(20);
                newAccountFields.get(2 * i).setFont(new Font("Segoe UI", Font.BOLD, 20));
            }
            this.setVisible(true);
            while (setupFormActive) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }

        private void editTypeSelector(int usertype, ArrayList<JComponent> fields, JPanel portal) {
            /*
                Indexes for tentative items:
                0: First Name
                1: Middle Name
                2: Last Name
                3: Student ID
                4: Address
                5: Email
                6: Home Phone
                7: Cell Phone
                8: Create Username
                9: Create Password
               10: Confirm Password
             */
            /*boolean[][] displaywhat = {
                    {true, true, true, true, true, true, true, true, true, true, true},
                    {true, true, true, true, true, true, true, true, true, true, true},
                    {true, true, true, false, true, true, true, true, true, true, true},
                    {true, true, true, false, true, true, true, true, true, true, true}
            };
            GridBagConstraints c = new GridBagConstraints();
            for (int i = 0; i < displaywhat[0].length; i++) {
                c.insets = new Insets(0, 0, 10, 20);
                c.gridx = 0;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                if (displaywhat[usertype][i] && !getDisplayingNAFields()[i]) {
                    portal.add(fields.get(2 * i + 1), c);
                    ((JLabel) fields.get(2 * i + 1)).setLabelFor(fields.get(2 * i));
                    fields.get(2 * i + 1).setFont(new Font("Segoe UI", Font.BOLD, 20));
                    fields.get(2 * i + 1).setBounds(portal.getX() + portal.getWidth() / 10, (i == 0) ? portal.getY() + portal.getHeight() / 20 : fields.get(2 * i - 1).getY() + fields.get(2 * i - 1).getHeight() * 3 / 2, 75, 20);
                    c.gridx = 1;
                    c.anchor = GridBagConstraints.CENTER;
                    portal.add(fields.get(2 * i), c);
                    portal.validate();
                }
                if (!displaywhat[usertype][i] && getDisplayingNAFields()[i]) {
                    portal.remove(fields.get(i));
                    portal.validate();
                }
            }
            JButton jb = new JButton("Submit");
            jb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setupFormActive = false;
                    if (!((JTextField) fields.get(18)).getText().equals(((JTextField) fields.get(20)).getText())) {

                    } else {
                        ArrayList<String> elem = new ArrayList();
                        elem.add(null); // there used to be two places for new/existing school code in the form, but these were taken out, and placeholders added to avoid changing all the numbers.
                        elem.add(null);
                        for (int i = 0; i < fields.size() / 2; i++) {
                            try {
                                elem.add(((JTextField) fields.get(2 * i)).getText());
                            } catch (ClassCastException ex) {
                                elem.add(fields.get(2 * i).toString());
                            }
                        }
                        switch (usertype) {
                            case 0:
                                setNewUser(new Student(-1, Root.getMACAddress(), elem.get(10), elem.get(11), elem.get(2), elem.get(3), elem.get(4), elem.get(7), elem.get(8), elem.get(9), elem.get(6), elem.get(5), new Timestamp(System.currentTimeMillis())));
                                break;
                            case 1:
                                setNewUser(new Teacher(-1, Root.getMACAddress(), elem.get(10), elem.get(11), elem.get(2), elem.get(3), elem.get(4), elem.get(7), elem.get(8), elem.get(9), elem.get(6), null, new Timestamp(System.currentTimeMillis())));
                                break;
                            case 2:
                                setNewUser(new Administrator(-1, Root.getMACAddress(), elem.get(10), elem.get(11), elem.get(2), elem.get(3), elem.get(4), elem.get(7), elem.get(8), elem.get(9), elem.get(6), elem.get(5), new Timestamp(System.currentTimeMillis())));
                                break;
                            default:
                                setNewUser(null);
                        }
                    }
                }
            });
            if (!displayingNASubmit) {
                displayingNASubmit = true;
                c.gridx = 1;
                c.gridy = 12;
                portal.add(jb, c);
                portal.validate();
            }
        }
    }

    private class LocalJPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage bf = null;
            try {
                String fn = "MainLogo.png";
                bf = ImageIO.read(new File(fn));
                if (bf == null) {
                    throw new IOException();
                }
            } catch (IOException e) {
                g.drawString("Paintbrush", this.getWidth() / 2, this.getHeight() / 20);
            }
            JLabel welcomeMS = new JLabel("Welcome to Paintbrush");
            welcomeMS.setFont(new Font("Segoe UI", Font.BOLD, 40));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.PAGE_START;
            c.gridwidth = 3;
            c.insets = new Insets(10, 10, 10, 10);
            add(welcomeMS, c);
            validate();
        }

        protected LocalJPanel(GridBagLayout gbl) {
            super(gbl);
        }
    }
}*/
