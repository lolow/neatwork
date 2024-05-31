package neatwork.gui.simu;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * Boite de dialogue de selection des robinets
 * @author L. DROUET
 * @version 1.0
 */
public class TapSelectionDialog extends JDialog implements ActionListener {
    TapSelectionModel tableModel;
    FancyTable table;

    public TapSelectionDialog(Design design, JDialog dialog) {
        super(dialog,
            Messages.getString("TapSelectionDialog.Faucets_selection"), true); 
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(300, 200);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);

        tableModel = new TapSelectionModel(design);
        table = new FancyTable(tableModel);

        //none
        Icon icon = null;
        Action[] actions = new Action[2];
        actions[1] = new AbstractAction(Messages.getString(
                    "TapSelectionDialog.None"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        tableModel.setall(table.getSelectedRows(), false);
                    }
                };
        actions[1].putValue(Action.SHORT_DESCRIPTION, "Select none"); 

        //all
        icon = null;
        actions[0] = new AbstractAction(Messages.getString(
                    "TapSelectionDialog.All"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        tableModel.setall(table.getSelectedRows(), true);
                    }
                };
        actions[0].putValue(Action.SHORT_DESCRIPTION, "Select all"); 

        JPanel tapPanel = new FancyTablePanel("", actions, table, true); 

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        pane.add(tapPanel, BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString("TapSelectionDialog.OK")); 
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }

    public Hashtable getFaucetRef() {
        return tableModel.getFaucetRef();
    }
}
