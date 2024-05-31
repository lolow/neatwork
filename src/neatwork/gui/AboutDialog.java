package neatwork.gui;

import neatwork.Messages;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;

/**
 * Boite de dialogue
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class AboutDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 8316618159909794433L;
	Properties properties;
	private JPanel jPanel4 = new JPanel();
	private JLabel jLabelImage;

	public AboutDialog(JFrame frame, Properties properties) {
		super(frame, true);
		setTitle(Messages.getString("AboutDialog.About..."));
		this.properties = properties;
		setModal(true);
		setSize(800, 450);

		// centrage
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int width = getSize().width;
		int height = getSize().height;
		setBounds((screen.width - width) / 2, (screen.height - height) / 2, width, height);

		// Mise en place des composants
		Icon image = new ImageIcon(getClass().getResource("/neatwork/gui/images/logo.jpg"));
		jLabelImage = new JLabel(image);

		Container content = getContentPane();
		content.setBackground(Color.white);
		content.setLayout(new BorderLayout(5, 5));
		content.add(jLabelImage, BorderLayout.WEST);

		// Panel avec le texte d'explication
		JLabel jLabelText = new JLabel();
		jLabelText.setText("<HTML> <center><h2>" + Messages.getString("AboutDialog.APLV") + "</h2></center>"
				+ Messages.getString("PresentationPanel.text1") + Messages.getString("PresentationPanel.text2")
				+ Messages.getString("PresentationPanel.text3") + Messages.getString("PresentationPanel.text4")
				+ Messages.getString("PresentationPanel.text5") + "<br><br><br>"
				+ Messages.getString("AboutDialog.version") + properties.getProperty("appli.version") + //$NON-NLS-2$
				" (" + properties.getProperty("appli.releasedate") + ")" + //$NON-NLS-3$
				"</HTML>");
		jLabelText.setAutoscrolls(true);
		jPanel4.add(jLabelText);
		content.add(new JScrollPane(jPanel4), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}
