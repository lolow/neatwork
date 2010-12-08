package neatwork.file;

import neatwork.Messages;

import neatwork.project.*;

import java.io.*;

import java.util.*;


/**
 * Classe qui d\u00E9finit un gestionnaire de fichier disque
 * @author L. DROUET
 * @version 1.0
 */
public class FileManagerDisk extends AbstractFileManager
    implements FilenameFilter {
    private String appliPath;
    private String projectPath;
    private String databasePath;
    private String currentSuffix = ""; //$NON-NLS-1$
    private Properties properties;

    public FileManagerDisk(Properties properties) {
        this.properties = properties;
        appliPath = properties.getProperty("file.path"); //$NON-NLS-1$
        setPathes(appliPath);
    }

    /**
     * affecte les repertoires par defaut
     */
    public void setPathes(String rootPath) {
        properties.setProperty("file.path", rootPath); //$NON-NLS-1$
        appliPath = properties.getProperty("file.path"); //$NON-NLS-1$
        projectPath = appliPath + System.getProperty("file.separator") + //$NON-NLS-1$
            properties.getProperty("file.pathproject"); //$NON-NLS-1$
        databasePath = appliPath + System.getProperty("file.separator") + //$NON-NLS-1$
            properties.getProperty("file.pathdb"); //$NON-NLS-1$
    }

    /**
     * affecte les repertoires de project
     */
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    /** renvoie la liste des fichier du type <i>type</i>*/
    public String[] getListFile(int type) {
        makeDirectoryExists(projectPath);
        setCurrentSuffix(type);

        File f = new File(projectPath);
        String[] fileList = f.list(this);

        if (fileList == null) {
            setFileManagerStatus(Messages.getString(
                    "FileManagerDisk.No_project_directory_found")); //$NON-NLS-1$

            return fileList;
        }

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = fileList[i].substring(0, fileList[i].length() - 4);
        }

        if (fileList.length == 0) {
            setFileManagerStatus(Messages.getString(
                    "FileManagerDisk.No_files_found")); //$NON-NLS-1$
        }

        return fileList;
    }

    public String readFile(String fileName, int type) {
        String line;
        String data = ""; //$NON-NLS-1$
        setFileManagerStatus(Messages.getString("FileManagerDisk.Reading_file")); //$NON-NLS-1$
        makeDirectoryExists(projectPath);
        setCurrentSuffix(type);
        fileName = projectPath + File.separator + fileName + currentSuffix;

        //new
        File file = new File(fileName);

        if (!file.exists()) {
            return ""; //$NON-NLS-1$
        }

        //open
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader f = new BufferedReader(fr);

            while ((line = f.readLine()) != null) {
                data += (line + "\n"); //$NON-NLS-1$
            }

            setFileManagerStatus(" "); //$NON-NLS-1$

            fr.close();
        } catch (IOException e) {
            setFileManagerStatus("IO error : " + e.getMessage()); //$NON-NLS-1$
        }

        return data;
    }

    public boolean writeFile(String fileName, String content, int type) {
        setFileManagerStatus(Messages.getString("FileManagerDisk.Writing_file")); //$NON-NLS-1$
        makeDirectoryExists(projectPath);
        setCurrentSuffix(type);
        fileName = projectPath + File.separator + fileName + currentSuffix;

        boolean isOk = false;

        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(fileName));
            StringTokenizer st = new StringTokenizer(content, "\n\r"); //$NON-NLS-1$

            for (; st.hasMoreTokens();) {
                f.write(st.nextToken());
                f.newLine();
            }

            f.flush();
            f.close();
            isOk = true;
            setFileManagerStatus(" "); //$NON-NLS-1$
        } catch (IOException e) {
            setFileManagerStatus("IO error : " + e.getMessage()); //$NON-NLS-1$
        }

        return isOk;
    }

    public boolean deleteFile(String fileName, int type) {
        setFileManagerStatus(Messages.getString("FileManagerDisk.Deleting_file")); //$NON-NLS-1$
        makeDirectoryExists(projectPath);
        setCurrentSuffix(type);

        String fileName0 = projectPath + File.separator + fileName +
            currentSuffix;
        File f = new File(fileName0);
        boolean isOk = f.delete();

        if (!isOk) {
            setFileManagerStatus(Messages.getString(
                    "FileManagerDisk.Can__t_delete_this_file")); //$NON-NLS-1$
        } else {
            setFileManagerStatus(" "); //$NON-NLS-1$

            //si design efface les simu
            if (type == Project.TYPE_DESIGN) {
                String[] fsim = getListFile(Project.TYPE_SIMU);

                for (int i = 0; i < fsim.length; i++) {
                    if (fsim[i].startsWith(fileName + ".")) { //$NON-NLS-1$
                        deleteFile(fsim[i], Project.TYPE_SIMU);
                    }
                }
            }
        }

        return isOk;
    }

    public boolean accept(File dir, String name) {
        return name.endsWith(currentSuffix);
    }

    private void setCurrentSuffix(int type) {
        switch (type) {
        case Project.TYPE_TOPO:
            currentSuffix = ".tpo"; //$NON-NLS-1$

            break;

        case Project.TYPE_DESIGN:
            currentSuffix = ".dsg"; //$NON-NLS-1$

            break;

        case Project.TYPE_SIMU:
            currentSuffix = ".sim"; //$NON-NLS-1$
        }
    }

    private void makeDirectoryExists(String path) {
        File f = new File(path);

        if (!f.exists()) {
            if (!f.mkdirs()) {
                System.exit(-1);
            }
        }
    }

    //lit un fichier database
    public String readDbFile(String fileName) {
        setFileManagerStatus(Messages.getString(
                "FileManagerDisk.Reading_database_file")); //$NON-NLS-1$
        makeDirectoryExists(databasePath);
        fileName = databasePath + File.separator + fileName;

        //new
        File file = new File(fileName);

        if (!file.exists()) {
            return ""; //$NON-NLS-1$
        }

        String data = ""; //$NON-NLS-1$

        //open
        try {
            BufferedReader f = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = f.readLine()) != null) {
                data += (line + "\n"); //$NON-NLS-1$
            }

            setFileManagerStatus(" "); //$NON-NLS-1$
        } catch (IOException e) {
            setFileManagerStatus("IO error : " + e.getMessage()); //$NON-NLS-1$
        }

        return data;
    }

    /** ecrit un fichier database*/
    public boolean writeDbFile(String fileName, String content) {
        setFileManagerStatus(Messages.getString(
                "FileManagerDisk.Writing_database_file")); //$NON-NLS-1$
        makeDirectoryExists(databasePath);
        fileName = databasePath + File.separator + fileName;

        boolean isOk = false;

        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(fileName));
            StringTokenizer st = new StringTokenizer(content, "\n\r"); //$NON-NLS-1$

            for (; st.hasMoreTokens();) {
                f.write(st.nextToken());
                f.newLine();
            }

            f.flush();
            f.close();
            isOk = true;
            setFileManagerStatus(" "); //$NON-NLS-1$
        } catch (IOException e) {
            setFileManagerStatus("IO error : " + e.getMessage()); //$NON-NLS-1$
        }

        return isOk;
    }
}
