package neatwork.file;

import java.io.*;

import java.util.*;


/**
 * fournit le service web de file manager
 * methodes : getListFile readFile writeFile deleteFile readDbFile writeDbFile
 * @author L. DROUET
 * @version 1.0
 */
public class FileManagerServer {
    static String defaultUser = "guest"; 
    AbstractFileManager fm;

    //liste de fichiers
    public Vector getListFile(String user, String type)
        throws Exception {
        fm = getFileManager();

        String[] fileList = fm.getListFile(Integer.parseInt(type));
        Vector v = new Vector();

        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].startsWith(user + "_")) { 
                    v.add(fileList[i].substring(user.length() + 1));
                }
            }
        }

        if (!user.equalsIgnoreCase(defaultUser)) {
            Vector v2 = getListFile(defaultUser, type);

            for (Enumeration e = v2.elements(); e.hasMoreElements();) {
                String s = e.nextElement().toString();

                if (!v.contains(s)) {
                    v.add(s);
                }
            }
        }

        return v;
    }

    //read file
    public String readFile(String user, String fileName, int type) {
        fm = getFileManager();

        String data = fm.readFile(user + "_" + fileName, type); 

        if (data.equals("")) { 
            data = fm.readFile(defaultUser + "_" + fileName, type); 
        }

        return data;
    }

    //write file
    public boolean writeFile(String user, String fileName, String content,
        int type) {
        fm = getFileManager();

        return fm.writeFile(user + "_" + fileName, content, type); 
    }

    //delete file
    public boolean deleteFile(String user, String fileName, int type) {
        fm = getFileManager();

        return fm.deleteFile(user + "_" + fileName, type); 
    }

    //read databaseFile
    public String readDbFile(String user, String fileName) {
        fm = getFileManager();

        return fm.readDbFile(user + "_" + fileName); 
    }

    //write file
    public boolean writeDbFile(String user, String fileName, String content) {
        fm = getFileManager();

        return fm.writeDbFile(user + "_" + fileName, content); 
    }

    private AbstractFileManager getFileManager() {
        Properties p = new Properties();

        try {
            p.load(getClass().getResourceAsStream("/neatwork/default.properties")); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileManagerDisk fm = new FileManagerDisk(p);

        return fm;
    }
}
