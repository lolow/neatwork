package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * Classe d'\u00E9dition du topo par table
 * @author L. DROUET
 * @version 1.0
 */
public class TopoTablePane extends JPanel implements Observer {
    private Topographie topo;
    private TopoNodeTableModel nodesTableModel;
    private TopoPipeTableModel pipesTableModel;
    private FancyTable jTablePipe;
    private FancyTable jTableNode;
    private JSplitPane jSplitPane;
    String pathImage = "/neatwork/gui/images/"; //$NON-NLS-1$

    public TopoTablePane(Topographie topo0) {
        this.topo = topo0;
        topo.addObserver(this);
        nodesTableModel = new TopoNodeTableModel(topo);
        pipesTableModel = new TopoPipeTableModel(topo);
        jTablePipe = new FancyTable(pipesTableModel, true);
        jTableNode = new FancyTable(nodesTableModel, true);

        setLayout(new BorderLayout());
        jSplitPane = new JSplitPane();

        //TOP
        Action[] actions = new Action[8];

        //AddNode
        actions[0] = jTableNode.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Insert_a_new_node")); //$NON-NLS-1$

        //DeleteNode
        actions[1] = jTableNode.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Delete_selected_node(s)")); //$NON-NLS-1$

        //espace
        actions[2] = null;

        //undo
        actions[6] = jTableNode.updateAction;
        actions[6].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Undo_modifications")); //$NON-NLS-1$

        //check
        Icon icon = new ImageIcon(getClass().getResource(pathImage +
                    "Check.gif")); //$NON-NLS-1$
        actions[7] = new NeatworkAction(Messages.getString(
                    "TopoTablePane.Apply"), icon,
                Messages.getString("TopoTablePane.Apply_modifications"), //$NON-NLS-1$ //$NON-NLS-2$
                'A') {
                    public void actionPerformed(ActionEvent e) {
                        apply();
                    }
                };

        //espace
        actions[5] = null;

        //undo
        actions[3] = jTableNode.copyAction;

        //paste
        actions[4] = jTableNode.pasteAction;
        jSplitPane.setTopComponent(new FancyTablePanel(Messages.getString(
                    "TopoTablePane.Node_list"), actions, //$NON-NLS-1$
                jTableNode));

        //BOTTOM
        actions = new Action[8];

        //AddPipe
        actions[0] = jTablePipe.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Insert_a_new_pipe")); //$NON-NLS-1$

        //DeletePipe
        actions[1] = jTablePipe.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Delete_selected_pipe(s)")); //$NON-NLS-1$

        //espace
        actions[2] = null;

        //undo
        actions[3] = jTablePipe.copyAction;

        //paste
        actions[4] = jTablePipe.pasteAction;

        //espace
        actions[5] = null;

        //undo
        actions[6] = jTablePipe.updateAction;
        actions[6].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("TopoTablePane.Undo_modifications")); //$NON-NLS-1$

        //check
        icon = new ImageIcon(getClass().getResource(pathImage + "Check.gif")); //$NON-NLS-1$
        actions[7] = new NeatworkAction(Messages.getString(
                    "TopoTablePane.Apply"), icon,
                Messages.getString("TopoTablePane.Apply_modifications"), //$NON-NLS-1$ //$NON-NLS-2$
                'A') {
                    public void actionPerformed(ActionEvent e) {
                        apply();
                    }
                };
        jSplitPane.setBottomComponent(new FancyTablePanel(Messages.getString(
                    "TopoTablePane.Arc_list"), //$NON-NLS-1$
                actions, jTablePipe));

        //TOTAL
        jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setContinuousLayout(false);
        jSplitPane.setResizeWeight(0.5);
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setDividerLocation(200);

        /*//TreeView
        JSplitPane jSplitPane2 = new JSplitPane();
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setOneTouchExpandable(true);
        jSplitPane2.setDividerLocation(400);
        jSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane2.setLeftComponent(jSplitPane);
        TreeNetwork treeNode = new TreeNetwork(topo);
        jSplitPane2.setRightComponent(new JScrollPane(treeNode));
        */
        add(jSplitPane, BorderLayout.CENTER);

        updateNodesTable();
        updatePipesTable();
    }

    private void updateNodesTable() {
        jTableNode.updateData();
    }

    private void updatePipesTable() {
        jTablePipe.updateData();
    }

    private void apply() {
        topo.freeInfoModif();

        String content = topo.getPropertiesContent() +
            nodesTableModel.getContent() + pipesTableModel.getContent();
        topo.setContent(content);

        if (!topo.getInfoModif().equals("")) { //$NON-NLS-1$
            JOptionPane.showMessageDialog(null,
                Messages.getString("TopoTablePane.error_in_your_project")); //$NON-NLS-1$
            JOptionPane.showMessageDialog(null, topo.getInfoModif());
        }
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(topo)) {
            switch (((Integer) param).intValue()) {
            case Topographie.MODIF_CONTENT:

                //do change
                updateNodesTable();
                updatePipesTable();

                break;
            }
        }
    }
}
