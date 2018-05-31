package filesystem;

import java.util.Stack;

public class FileSystemParser {

    Stack<FileSystemElement> activePath; //stores how we got where we are so we can know what parent to remove or add if necessary
    FileSystemElement currentElement;

    public void navigate(String cd) throws FileSystemParseException {
        String[] path = cd.split("/");
        for (String item : path) {
            if (item.length() >= 2 && item.substring(0, 2).equals("..")) {
                if (item.equals("..")) {
                    assert activePath.pop().equals(currentElement);
                    currentElement = activePath.peek();
                } else {
                    String upDirName = item.substring(2);
                    boolean found = false;
                    for (Directory d : currentElement.getParents()) {
                        if (d.getName().equals(upDirName)) {
                            found = true;
                            currentElement = d;
                            break;
                        }
                    }
                    if (!found) {
                        throw new FileSystemParseException("No such parent directory '" + upDirName + "'.");
                    }
                }
            } else {
                boolean found = false;
                currentElement = parseChildren(item);
                if (currentElement == null) {
                    throw new FileSystemParseException("No such child element '" + item + "' in '" + currentElement.getName() + "'.");
                }
                activePath.push(currentElement);
            }
        }
    }

    public void makeDirectory(String name) throws FileSystemParseException {

        if (!(currentElement instanceof Directory)) {
            throw new FileSystemParseException("Cannot create subdirectory within non-directory element " + currentElement.getName());
        }
        ((Directory) currentElement).addSubElement(new Directory(name));
    }

    public void makeDirectory(String name, FileSystemElement addIn, boolean copyOf) throws FileSystemParseException {
        if (!(currentElement instanceof Directory)) {
            throw new FileSystemParseException("Cannot create subdirectory within non-directory element " + currentElement.getName());
        }
        Directory newDirectory = (Directory) ((Directory) currentElement).addSubElement(new Directory(name));
        if (copyOf) {
            newDirectory.addSubElement(addIn.clone());
        } else {
            newDirectory.addSubElement(addIn);
        }
    }

    public void delete(String name) throws FileSystemParseException {
        FileSystemElement del = parseChildren(name);
        if (del == null) {
            throw new FileSystemParseException("No such child element '" + name + "' in '" + currentElement.getName() + "'.");
        }
        currentElement.getChildren().remove(del);
    }

    public void move(Directory target) {
        activePath.pop();
        currentElement.getParents().set(currentElement.getParents().indexOf(activePath.peek()), target);
        activePath.push(currentElement);
        //TODO reset activePath stack to reflect new position.
    }

    public String ls() {
        return currentElement.ls();
    }

    private FileSystemElement parseChildren(String name){
        for (FileSystemElement fse : currentElement.getChildren()) {
            if (fse.getName().equals(name)) {
                return fse;
            }
        }
        return null;
    }
}
