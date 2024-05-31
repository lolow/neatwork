/*
 * Created on 10 nov. 2004
 * Filename : LocaleAction.java
 */
package neatwork.gui;

import neatwork.*;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import java.util.*;

/**
 * @author lolow
 *
 *         Cette classe definit une action pour changer la locale de
 *         l'application
 */
public class LocaleAction extends AbstractAction {

	private String locale;
	private Frame frame;

	public LocaleAction(String locale, FrameNeatwork frame) {
		this.locale = locale;
		this.frame = frame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

		// Messages.setLocale(new Locale(locale));
		Properties p = new Properties();

		File f = new File("neatwork.locale");
		// load
		try {
			InputStream is = new FileInputStream(f);
			p.load(is);
			is.close();
		} catch (IOException e) {
		}
		p.setProperty("appli.locale", locale);
		// save
		try {
			OutputStream os = new FileOutputStream(f);
			p.store(os, "#Do not touch this file");
		} catch (IOException e) {
		}
		// Message
		JOptionPane.showMessageDialog(frame, Messages.getString("LocaleAction.MessageRestart"));
	}

}
