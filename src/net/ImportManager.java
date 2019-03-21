package net;

import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class ImportManager implements Serializable {
    private static final long serialVersionUID = 40001;

    private TreeMap<String, ExternalPackage> currentPackages;
    private TreeMap<String, ExternalPackage> pastPackages;

    public ImportManager() {
        currentPackages = new TreeMap<>();
        pastPackages = new TreeMap<>();
    }

    public TreeMap<String, ExternalPackage> getCurrentPackages() {
        return currentPackages;
    }

    public TreeMap<String, ExternalPackage> getPastPackages() {
        return pastPackages;
    }

    public void importPackage(String nickname) throws IOException {
        ServerSession serverSession = new ServerSession();
        serverSession.open();
        boolean success = serverSession.sendOnly("import", nickname);
        serverSession.close();
        if (success) currentPackages.put(nickname, new ExternalPackage(nickname));
    }

    public ExternalPackage get(String nickname) {
        return currentPackages.get(nickname);
    }

    public void unimportPackage(String nickname) throws IOException {
        ExternalPackage toRemove = currentPackages.remove(nickname);
        ServerSession serverSession = new ServerSession();
        serverSession.open();
        boolean success = serverSession.sendOnly("unimport", nickname);
        serverSession.close();
        if (success) pastPackages.put(nickname, toRemove);
        else currentPackages.put(nickname, toRemove);
    }
}
