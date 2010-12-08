package neatwork;

import neatwork.gui.*;

import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;


/**
 * Classe qui lance l'application, charge les propri�t�s, les ressources selon la langue
 * @author L. DROUET
 * @version 1.0
 */
public class Loader extends JApplet {
	private static final long serialVersionUID = -6286912387170434615L;
	private Properties properties;
    private FrameNeatwork frame;
    private JButton button;

    /** Initialize the applet */
    public void init() {
        System.getProperties().setProperty("entityExpansionLimit", "100000"); //$NON-NLS-1$ //$NON-NLS-2$
        readDefaultProperties();
        properties.setProperty("appli.standalone", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.setProperty("solver.distant", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.setProperty("file.distant", "true"); //$NON-NLS-1$ //$NON-NLS-2$

        String user = ""; //$NON-NLS-1$
        user = getParameter("user"); //$NON-NLS-1$

        if (user != null) {
            properties.setProperty("appli.user", user); //$NON-NLS-1$
        } else {
            System.err.println(Messages.getString(
                    "Loader.Error__no_user_parameter")); //$NON-NLS-1$
        }

        button = new JButton(Messages.getString("Loader.show_NeatWork")); //$NON-NLS-1$
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (button.getText().equals(Messages.getString(
                                    "Loader.show_NeatWork"))) { //$NON-NLS-1$

                        if (frame == null) {
                            frame = new FrameNeatwork(properties);
                        }

                        frame.setVisible(true);
                        button.setText(Messages.getString(
                                "Loader.hide_NeatWork")); //$NON-NLS-1$
                    } else {
                        frame.setVisible(false);
                        button.setText(Messages.getString(
                                "Loader.show_NeatWork")); //$NON-NLS-1$
                    }
                }
            });
        getContentPane().add(button);
    }

    /**Start the applet*/
    public void start() {
    }

    /**Stop the applet*/
    public void stop() {
    }

    /**Destroy the applet*/
    public void destroy() {
    }

    /**Get Applet information*/
    public String getAppletInfo() {
        return properties.getProperty("appli.name",
            Messages.getString("Loader.NeatWork")) + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        properties.getProperty("appli.version", "beta"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**Get parameter info*/
    public String[][] getParameterInfo() {
        String[][] pinfo = {
            { "param0", "String", "" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            { "param1", "String", "" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        };

        return pinfo;
    }

    /* PARTIE STANDALONE*/
    public static void main(String[] args) {
        Loader loader = new Loader();
        loader.readDefaultProperties();
        loader.properties.setProperty("appli.standalone", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        loader.properties.setProperty("solver.distant", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        loader.properties.setProperty("file.distant", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        loader.properties.setProperty("file.path", //$NON-NLS-1$
            System.getProperty("user.dir")); //$NON-NLS-1$

        Properties po = new Properties();
        File f = new File("neatwork.locale");
        Locale l = Locale.getDefault();

        //load
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

        //definition de la frame
        FrameNeatwork frame = new FrameNeatwork(loader.properties);
    }

    private void readDefaultProperties() {
        Properties p = new Properties();

        try {
            p.load(getClass().getResourceAsStream("default.properties")); //$NON-NLS-1$
        } catch (IOException e) {
        }

        properties = p;
    }
}
