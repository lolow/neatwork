package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * boite de dialogue d'affichage des loadFactor d'un design
 * @author L. DROUET
 * @version 1.0
 */
public class DesignLoadFactorDialog extends JDialog implements ActionListener {
    public DesignLoadFactorDialog(JFrame frame, Design design) {
        super(frame,
            Messages.getString("DesignLoadFactorDialog.Building_Load_Factors"),
            true); 
        setSize(400, 400);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Hashtable table0 = design.getLoadFactorTable();
        Properties prop = new Properties();
        Enumeration e = table0.keys();
        int bip = 0;
        String source = design.getSource().getName();

        while (e.hasMoreElements()) {
            String item = e.nextElement().toString();

            if (!item.equals(source)) {
                prop.setProperty("topo." + bip + ".name", item);  
                prop.setProperty("topo." + bip + ".value",  
                    table0.get(item).toString());
                bip++;
            }
        }

        FancyTableModel tableModel = new DesignLoadFactorModel(prop);
        FancyTable table = new FancyTable(tableModel);
        TableSorter sorter = (TableSorter) table.getModel();
        sorter.sortByColumn(0);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        pane.add(new FancyTablePanel(Messages.getString(
                    "DesignLoadFactorDialog.These_are_the_load_factors_used_by") + 
                Messages.getString(
                    "DesignLoadFactorDialog.the_MAKE_DESIGN_operation_for_building") +
                Messages.getString("DesignLoadFactorDialog._this_design"),  
                null, table, true), BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString(
                    "DesignLoadFactorDialog.Close")); 
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
