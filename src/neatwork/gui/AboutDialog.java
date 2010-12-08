package neatwork.gui;

import neatwork.Messages;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;


/**
 * Boite de dialogue
 * @author L. DROUET
 * @version 1.0
 */
public class AboutDialog extends JDialog implements ActionListener {
    Properties properties;
    private JPanel jPanel4 = new JPanel();
    private JLabel jLabelImage;
    private JTable jTable;
    private JLabel jLabel = new JLabel();
    private JButton jButton = new JButton(Messages.getString("AboutDialog.Ok")); //$NON-NLS-1$

    public AboutDialog(JFrame frame, Properties properties) {
        super(frame, true);
        setTitle(Messages.getString("AboutDialog.About...")); //$NON-NLS-1$
        this.properties = properties;
        setModal(true);
        setSize(800, 450);

        //centrage
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = getSize().width;
        int height = getSize().height;
        setBounds((screen.width - width) / 2, (screen.height - height) / 2,
            width, height);

        //Mise en place des composants
        Icon image = new ImageIcon(getClass().getResource("/neatwork/gui/images/logo.jpg")); //$NON-NLS-1$
        jLabelImage = new JLabel(image);

        Container content = getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new BorderLayout(5, 5));
        content.add(jLabelImage, BorderLayout.WEST);

        //Panel avec le texte d'explication
        JLabel jLabelText = new JLabel();
        jLabelText.setText(
            "<HTML> <center><h2>"+ //$NON-NLS-1$
           Messages.getString("AboutDialog.APLV")+ //$NON-NLS-1$
            "</h2></center>" +       //$NON-NLS-1$
            Messages.getString("PresentationPanel.text1") + //$NON-NLS-1$
            Messages.getString("PresentationPanel.text2") + //$NON-NLS-1$
            Messages.getString("PresentationPanel.text3") + //$NON-NLS-1$
            Messages.getString("PresentationPanel.text4") + //$NON-NLS-1$
            Messages.getString("PresentationPanel.text5") + //$NON-NLS-1$
            "<br><br><br>"+Messages.getString("AboutDialog.version")+properties.getProperty("appli.version") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            " (" + properties.getProperty("appli.releasedate") + ")" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "</HTML>"); //$NON-NLS-1$
        jLabelText.setAutoscrolls(true);
        jPanel4.add(jLabelText);
        content.add(new JScrollPane(jPanel4), BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
    }
}
