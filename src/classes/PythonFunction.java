package classes;

import org.json.JSONObject;
import terminal.Address;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PythonFunction implements AbstractFunction<Object[], Object> {

    private String name;
    private static final long TIMEOUT = 5000;

    //existing function
    public PythonFunction(String name) {
        name = name.replaceAll("\\s+", "_");
        this.name = name;
    }

    //new function
    public PythonFunction(String name, String... arguments) throws IOException {
        name = name.replaceAll("\\s+", "_");
        File dir = new File(Address.fromRootAddr("scripts", "usr", name));
        StringBuilder nameBuilder = new StringBuilder(name);
        while(dir.exists()) {
            nameBuilder.append("_1");
            dir = new File(Address.fromRootAddr("scripts", "usr", nameBuilder.toString()));
        }
        name = nameBuilder.toString();
        this.name = name;
        final boolean newDir = dir.mkdir();
        if (!newDir)
            throw new IOException("failed to create new directory");
        if (!dir.canWrite())
            throw new IOException("cannot write to new file");
        File f = new File(dir, "__init__.py");
        final boolean newFile = f.createNewFile();
        if (!newFile)
            throw new IOException("failed to create new file");
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        StringBuilder header = new StringBuilder("def " + name + "(");
        boolean first = true;
        for (String arg : arguments) {
            if (!first)
                header.append(", ");
            header.append(arg);
            first = false;
        }
        header.append("):\n");
        header.append("\treturn 0\n");
        writer.write(header.toString());
        writer.close();
        Desktop.getDesktop().open(f);
    }

    @Override
    public Object evaluate(Object... params) {
        for (int i = 0; i < params.length; i++) {
            params[i] = JSONObject.wrap(params[i]);
        }
        try {
            final String command = "python3 " + Address.fromRootAddr("scripts", "run.py") + " " + name + " " + Arrays.stream(params).map(Object::toString).collect(Collectors.joining(" "));
            final Process exec = Runtime.getRuntime().exec(command);
            final int exitCode = exec.waitFor();
            if (exitCode == -1)
                return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            final String output = reader.lines().collect(Collectors.joining("\n"));
            if (!output.startsWith("{")) {
                if (output.matches("[0-9]+"))
                    return new BigInteger(output);
                return output;
            }
            return new JSONObject(output);
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return name;
    }
}
