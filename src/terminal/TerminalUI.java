package terminal;

import main.Root;

public class TerminalUI {
    public String getPrompt() {
        if (Root.getActiveUser() == null) {
            return "null@null >";
        }
        if (Root.getActiveUser().getUsername() == null) {
            return "guest > ";
        }
        String toReturn = null;
        if (Root.getActiveUser().getSchoolCode() == null)
            toReturn =  Root.getActiveUser().getUsername() + " >";
        else
            toReturn = Root.getActiveUser().getUsername() + "@" + Root.getActiveUser().getSchoolCode() + " > ";
        if (toReturn.length() > 30)
            return toReturn.substring(0, 31) + "...";
        return toReturn;
    }

    public TerminalRet command(String input, int columns) {
        //resolve carriage returns
        TerminalRet ret = null;
        try {
            ret = Command.command(input);
        } catch (TerminalException e) {
            ret = new TerminalRet("Terminal: " + e.getMessage());
        }
        if (ret.getText().length() == 0)
            return ret;
        String split[] = ret.getText().split(" ");
        int runningtotal = 0;
        String finalStr = "";
        for (String s: split) {
            int len = s.length() + 1;
            if (runningtotal + len > columns) {
                if (len > columns) {
                    s += " ";
                    int index = s.indexOf("\n");
                    int firstchars = Math.min(columns - runningtotal, index > -1 ? index : Integer.MAX_VALUE);
                    System.out.println(firstchars + "first");
                    finalStr += (s.substring(0, firstchars));
                    int i;
                    for (i = firstchars; i < s.length(); i+= columns) {
                        finalStr += ("\n" + s.substring(i, Math.min(i + columns, s.length())));
                    }
                    runningtotal = Math.min(i - s.length(), s.lastIndexOf("\n"));
                }
                else {
                finalStr += "\n" + s + " ";
                runningtotal = Math.min(len - s.lastIndexOf("\n") - 1, len);
                }
            } else {
                runningtotal += len;
                int lio = s.lastIndexOf("\n");
                if (lio > -1)
                    runningtotal = s.length() - lio - 1;
                finalStr += s + " ";
            }
        }
        ret.setText(finalStr);
        return ret;
    }

}
