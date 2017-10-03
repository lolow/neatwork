package neatwork.file;

import neatwork.Messages;

import java.util.*;

/**
 * Client pour le service de FileManager
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class FileManagerClient extends AbstractFileManager {

	public FileManagerClient(Properties prop) {
	}

	// liste de fichiers
	public String[] getListFile(int type) {
		Vector param = new Vector();
		Vector listFile = (Vector) sendMsg("getListFile", param);
		String[] s = new String[listFile.size()];

		for (int i = 0; i < s.length; i++) {
			s[i] = listFile.get(i).toString();
		}

		return s;
	}

	// read file
	public String readFile(String fileName, int type) {
		Vector param = new Vector();

		// param.addElement(new Parameter("user", String.class, user, null));
		// param.addElement(new Parameter("filename", String.class, fileName,
		// null));
		// param.addElement(new Parameter("type", Integer.class, new
		// Integer(type), null));
		return sendMsg("readFile", param).toString();
	}

	// write file
	public boolean writeFile(String fileName, String content, int type) {
		Vector param = new Vector();
		Boolean b = (Boolean) sendMsg("writeFile", param);

		return b.booleanValue();
	}

	// delete file
	public boolean deleteFile(String fileName, int type) {
		Vector param = new Vector();
		Boolean b = (Boolean) sendMsg("deleteFile", param);

		return b.booleanValue();
	}

	// read databaseFile
	public String readDbFile(String fileName) {
		Vector param = new Vector();
		return sendMsg("readDbFile", param).toString();
	}

	// write file
	public boolean writeDbFile(String fileName, String content) {
		Vector param = new Vector();
		return ((Boolean) sendMsg("writeDbFile", param)).booleanValue();
	}

	private Object sendMsg(String methode, Vector param) {
		setFileManagerStatus(Messages.getString("FileManagerClient.Invoking_server"));
		setFileManagerStatus(Messages.getString("FileManagerClient.Analyzing_response"));
		return null;
	}
}
