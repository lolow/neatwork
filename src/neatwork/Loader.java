package neatwork;

import neatwork.gui.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Application launcher
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class Loader extends JApplet {

	private static final long serialVersionUID = -6286912387170434615L;
	private Properties properties;
	private FrameNeatwork frame;
	private JButton button;

	@Override
	public void init() {
		System.getProperties().setProperty("entityExpansionLimit", "100000");
		readDefaultProperties();
		properties.setProperty("appli.standalone", "false");
		properties.setProperty("solver.distant", "true");
		properties.setProperty("file.distant", "true");

		String user = "";
		user = getParameter("user");

		if (user != null) {
			properties.setProperty("appli.user", user);
		} else {
			System.err.println(Messages.getString("Loader.Error__no_user_parameter"));
		}

		button = new JButton(Messages.getString("Loader.show_NeatWork"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (button.getText().equals(Messages.getString("Loader.show_NeatWork"))) {

					if (frame == null) {
						frame = new FrameNeatwork(properties);
					}

					frame.setVisible(true);
					button.setText(Messages.getString("Loader.hide_NeatWork"));
				} else {
					frame.setVisible(false);
					button.setText(Messages.getString("Loader.show_NeatWork"));
				}
			}
		});
		getContentPane().add(button);
	}

	@Override
	public String getAppletInfo() {
		return properties.getProperty("appli.name", Messages.getString("Loader.NeatWork")) + " "
				+ properties.getProperty("appli.version", "beta");
	}

	@Override
	public String[][] getParameterInfo() {
		String[][] pinfo = { { "param0", "String", "" }, { "param1", "String", "" }, };

		return pinfo;
	}

	public static void main(String[] args) {
		Loader loader = new Loader();
		loader.readDefaultProperties();
		loader.properties.setProperty("appli.standalone", "true");
		loader.properties.setProperty("solver.distant", "false");
		loader.properties.setProperty("file.distant", "false");
		loader.properties.setProperty("file.path", System.getProperty("user.dir"));

		Properties po = new Properties();
		File f = new File("neatwork.locale");
		Locale l = Locale.getDefault();

		// load
		try {
			InputStream is = new FileInputStream(f);
			po.load(is);
			is.close();

			String value = po.getProperty("appli.locale", "null");

			if (!value.equals("null")) {
				l = new Locale(value, "US");
			}
		} catch (IOException e) {
		}

		Messages.setLocale(l);
		po.setProperty("appli.locale", l.getLanguage());
		loader.properties.putAll(po);

		// frame definition
		new FrameNeatwork(loader.properties);
	}

	private void readDefaultProperties() {
		Properties p = new Properties();

		try {
			p.load(getClass().getResourceAsStream("default.properties"));
		} catch (IOException e) {
		}

		properties = p;
	}
}