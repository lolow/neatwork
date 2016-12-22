package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Panel de choix du mat�riel
 * @author L. DROUET
 * @version 1.0
 */
public class ChooseMaterialPane extends JPanel {
    private ChooseDiameterTableModel diamTableModel;
    private ChooseOrificeTableModel orifTableModel;
    private FancyTable diamTable;
    private FancyTable orifTable;
    private Topographie topo;

    public ChooseMaterialPane(Database database, Topographie topographie) {
        this.topo = topographie;

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.8);
        splitPane.setOneTouchExpandable(true);

        //diametres
        diamTableModel = new ChooseDiameterTableModel(database);
        diamTable = new FancyTable(diamTableModel);

        Action[] actions = new Action[2];

        //none
        Icon icon = null;
        actions[1] = new AbstractAction(Messages.getString(
                    "ChooseMaterialPane.None"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        diamTableModel.setall(diamTable.getSelectedRows(), false);
                    }
                };
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("ChooseMaterialPane.Select_none")); 

        //all
        icon = null;
        actions[0] = new AbstractAction(Messages.getString(
                    "ChooseMaterialPane.All"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        diamTableModel.setall(diamTable.getSelectedRows(), true);
                    }
                };
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("ChooseMaterialPane.Select_all")); 

        JPanel diamPanel = new FancyTablePanel(Messages.getString(
                    "ChooseMaterialPane.Diameters"), actions, 
                diamTable, false);

        splitPane.setTopComponent(diamPanel);

        //orifice
        orifTableModel = new ChooseOrificeTableModel(database);
        orifTable = new FancyTable(orifTableModel);
        actions = new Action[2];

        //none
        icon = null;
        actions[1] = new AbstractAction(Messages.getString(
                    "ChooseMaterialPane.None"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        orifTableModel.setall(orifTable.getSelectedRows(), false);
                    }
                };
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("ChooseMaterialPane.Select_none")); 

        //all
        icon = null;
        actions[0] = new AbstractAction(Messages.getString(
                    "ChooseMaterialPane.All"), icon) { 
                    public void actionPerformed(ActionEvent e) {
                        orifTableModel.setall(orifTable.getSelectedRows(), true);
                    }
                };
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("ChooseMaterialPane.Select_all")); 

        JPanel orifPanel = new FancyTablePanel(Messages.getString(
                    "ChooseMaterialPane.Orifices"), actions, 
                orifTable, false);

        splitPane.setBottomComponent(orifPanel);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    public String getDiametersContent() {
        return diamTableModel.getContent();
    }

    public String getOrificesContent() {
        return orifTableModel.getContent();
    }
}
