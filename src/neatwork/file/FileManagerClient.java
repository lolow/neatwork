package neatwork.file;

import neatwork.Messages;

import java.net.*;

import java.util.*;


//import org.apache.soap.*;
//import org.apache.soap.rpc.*;

/**
 * Client pour le service de FileManager
 * @author L. DROUET
 * @version 1.0
 */
public class FileManagerClient extends AbstractFileManager {
    private String user;
    private String uri;
    private URL url;

    public FileManagerClient(Properties prop) {
        this.user = prop.getProperty("appli.user", "guest"); //$NON-NLS-1$ //$NON-NLS-2$
        this.uri = prop.getProperty("appli.urifile"); //$NON-NLS-1$

        try {
            this.url = new URL(prop.getProperty("appli.server")); //$NON-NLS-1$
        } catch (MalformedURLException e) {
        }
    }

    //liste de fichiers
    public String[] getListFile(int type) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("type", String.class, "" + type, null));
        Vector listFile = (Vector) sendMsg("getListFile", param); //$NON-NLS-1$
        String[] s = new String[listFile.size()];

        for (int i = 0; i < s.length; i++) {
            s[i] = listFile.get(i).toString();
        }

        return s;
    }

    //read file
    public String readFile(String fileName, int type) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("filename", String.class, fileName, null));
        //    param.addElement(new Parameter("type", Integer.class, new Integer(type), null));
        return sendMsg("readFile", param).toString(); //$NON-NLS-1$
    }

    //write file
    public boolean writeFile(String fileName, String content, int type) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("filename", String.class, fileName, null));
        //    param.addElement(new Parameter("content", String.class, content, null));
        //    param.addElement(new Parameter("type", Integer.class, new Integer(type), null));
        Boolean b = (Boolean) sendMsg("writeFile", param); //$NON-NLS-1$

        return b.booleanValue();
    }

    //delete file
    public boolean deleteFile(String fileName, int type) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("filename", String.class, fileName, null));
        //    param.addElement(new Parameter("type", Integer.class, new Integer(type), null));
        Boolean b = (Boolean) sendMsg("deleteFile", param); //$NON-NLS-1$

        return b.booleanValue();
    }

    //read databaseFile
    public String readDbFile(String fileName) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("filename", String.class, fileName, null));
        return sendMsg("readDbFile", param).toString(); //$NON-NLS-1$
    }

    //write file
    public boolean writeDbFile(String fileName, String content) {
        Vector param = new Vector();

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("filename", String.class, fileName, null));
        //    param.addElement(new Parameter("content", String.class, content, null));
        return ((Boolean) sendMsg("writeDbFile", param)).booleanValue(); //$NON-NLS-1$
    }

    private Object sendMsg(String methode, Vector param) {
        //    Parameter result = null;
        //
        //    Call remoteMethod = new Call();
        //    remoteMethod.setTargetObjectURI(uri);
        //    remoteMethod.setMethodName(methode);
        //    remoteMethod.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
        //    remoteMethod.setParams(param);
        //    try {
        setFileManagerStatus(Messages.getString(
                "FileManagerClient.Invoking_server")); //$NON-NLS-1$

        //      Response reponse = remoteMethod.invoke(url, "");
        setFileManagerStatus(Messages.getString(
                "FileManagerClient.Analyzing_response")); //$NON-NLS-1$

        return null;
    }
}
