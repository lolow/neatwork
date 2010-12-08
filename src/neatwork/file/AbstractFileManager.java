package neatwork.file;

import neatwork.project.*;

import java.util.*;


/**
 * Classe qui définit les classes abstraites du FileManager
 * @author L. DROUET
 * @version 1.0
 */
public abstract class AbstractFileManager extends Observable {
    //status
    private String fileManagerStatus;

    //liste de fichiers
    public abstract String[] getListFile(int type);

    //read file
    public abstract String readFile(String fileName, int type);

    //write file
    public abstract boolean writeFile(String fileName, String content, int type);

    //delete file
    public abstract boolean deleteFile(String fileName, int type);

    //read databaseFile
    public abstract String readDbFile(String fileName);

    //write file
    public abstract boolean writeDbFile(String fileName, String content);

    protected void setFileManagerStatus(String status) {
        fileManagerStatus = status;
        setChanged();
        notifyObservers();
    }

    public String getFileManagerStatus() {
        return fileManagerStatus;
    }

    public boolean writeFile(Project p) {
        return writeFile(p.getName(), p.getContent(), p.getType());
    }

    //misc

    /**
     * renvoie le premier nom compatible eu egard a la liste de fichier
     * de meme type
     */
    public String getFirstNameCompatible(Project p) {
        String[] f = getListFile(p.getType());
        String name = p.getName();
        String oldName = name;
        int cpt = 2;

        while (isInList(f, name))
            name = oldName + " (" + String.valueOf(cpt++) + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        return name;
    }

    private boolean isInList(String[] l, String s) {
        int i = 0;
        boolean find = false;

        while ((i < l.length) && (!find)) {
            find = l[i].equals(s);
            i++;
        }

        return find;
    }
}
