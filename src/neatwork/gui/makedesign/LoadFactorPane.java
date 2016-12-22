package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * panel qui affiche les loadfactors
 * @author L. DROUET
 * @version 1.0
 */
public class LoadFactorPane extends JPanel implements Observer {
    private Topographie topo;
    private LoadFactorTableModel tableModel;
    private FancyTable table;

    public LoadFactorPane(Topographie topo) {
        this.topo = topo;
        topo.addObserver(this);

        tableModel = new LoadFactorTableModel(topo);
        table = new FancyTable(tableModel);

        Action[] actions = new Action[1];

        //check
        Icon icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/Undo.png")); 
        actions[0] = new NeatworkAction(Messages.getString(
                    "LoadFactorPane.Apply"), icon,
                Messages.getString("LoadFactorPane.Reset"), 'R') {  
                    public void actionPerformed(ActionEvent e) {
                        tableModel.reset();
                        updateTable();
                    }
                };
        setLayout(new BorderLayout());
        add(new FancyTablePanel(Messages.getString(
                    "LoadFactorPane.Load_factors"), actions, table, true), 
            BorderLayout.CENTER);
        updateTable();
    }

    public void updateTable() {
        table.updateData();
    }

    public Hashtable getLoadFactor() {
        return tableModel.getLoadFactor();
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(topo)) {
            switch (((Integer) param).intValue()) {
            case Topographie.MODIF_PROPERTIES:

                //do change
                updateTable();

                break;
            }
        }
    }
}
