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
public class UnitDialog extends JDialog implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Properties properties;
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JLabel jLabelImage;
    private JTable jTable;
    private JLabel jLabel = new JLabel();
    private JButton jButton = new JButton(Messages.getString("AboutDialog.Ok")); 

    public UnitDialog(JFrame frame, Properties properties) {
        super(frame, true);
        setTitle(Messages.getString("FrameNeatwork.Units"));  
        this.properties = properties;
        setModal(true);
        setSize(400, 200);

        //construction de la JTable
        Vector header = new Vector();
        header.add(Messages.getString("UnitDialog.Measure")); 
        header.add(Messages.getString("FrameNeatwork.Units"));  

        Vector data = new Vector();
        Enumeration e = properties.keys();

        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();

            if (name.startsWith("units.")) {  
         	
                Vector line = new Vector();
                line.add(Messages.getString(name+".name"));
                line.add(Messages.getString(name+".value")); 
                data.insertElementAt(line, 0);
            }
        }

        jTable = new JTable(data, header);
        jButton.addActionListener(this);

        //centrage
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = getSize().width;
        int height = getSize().height;
        setBounds((screen.width - width) / 2, (screen.height - height) / 2,
            width, height);

        //Mise en place des composants
  
        Container content = getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new BorderLayout(5, 5));
        jPanel1.setLayout(new BorderLayout(5, 5));
        jPanel1.setBorder(BorderFactory.createEtchedBorder(Color.white,
                new Color(142, 142, 142)));
        content.add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(new JScrollPane(jTable), BorderLayout.NORTH);
        jPanel3.add(jButton);
        jPanel1.add(jPanel3, BorderLayout.SOUTH);

    }

    public void actionPerformed(ActionEvent e) {
        dispose();
    }
}
