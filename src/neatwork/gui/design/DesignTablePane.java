package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * Panel d'�dition d'un design
 * @author L. DROUET
 * @version 1.0
 */
public class DesignTablePane extends JPanel implements Observer {
    private Design design;
    private DesignNodeModel nodesTableModel;
    private DesignPipeModel pipesTableModel;
    private FancyTable jTablePipe;
    private FancyTable jTableNode;
    private JSplitPane jSplitPane;
    private JLabel costLabel;

    public DesignTablePane(Design design, Database database) {
        this.design = design;

        design.addObserver(this);
        nodesTableModel = new DesignNodeModel(design);
        pipesTableModel = new DesignPipeModel(design, database, jTablePipe);
        jTablePipe = new FancyTable(pipesTableModel, true);
        jTableNode = new FancyTable(nodesTableModel, true);

        //combo
        ComboDatabase combo = new ComboDatabase(database) {
                public void contentsChanged(ListDataEvent e) {
                    selectedItemReminder = null;
                    super.contentsChanged(e);
                }
            };

        LblOrifice lblOrifice = new LblOrifice();

        //lblOrifice.setBackground(jTableNode.getBackground());
        //lblOrifice.setFont(jTableNode.getFont());
        //lblOrifice.setForeground(jTableNode.getForeground());
        jTableNode.getColumnModel().getColumn(4).setCellRenderer(lblOrifice);
        jTableNode.getColumnModel().getColumn(5).setCellRenderer(lblOrifice);

        Dimension d = combo.getPreferredSize();
        combo.setPopupWidth(200 /*d.width*/);
        combo.setBackground(jTablePipe.getBackground());
        combo.setFont(jTablePipe.getFont());
        combo.setForeground(jTablePipe.getForeground());

        DiametersEditor editor = new DiametersEditor(combo, design);
        jTablePipe.getColumnModel().getColumn(4).setCellEditor(editor);
        jTablePipe.getColumnModel().getColumn(6).setCellEditor(editor);

        DiametersRender renderer = new DiametersRender(design);
        renderer.setForeground(jTablePipe.getForeground());
        renderer.setFont(jTablePipe.getFont());
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        jTablePipe.getColumnModel().getColumn(4).setCellRenderer(renderer);
        jTablePipe.getColumnModel().getColumn(6).setCellRenderer(renderer);

        setLayout(new BorderLayout());
        jSplitPane = new JSplitPane();

        //TOP
        Action[] actions = new Action[8];

        //AddNode
        actions[0] = jTableNode.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DesignTablePane.Insert_a_new_node")); 

        //DeleteNode
        actions[1] = jTableNode.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DesignTablePane.Delete_selected_node(s)")); 

        //espace
        actions[2] = null;

        //undo
        actions[3] = jTableNode.copyAction;

        //paste
        actions[4] = jTableNode.pasteAction;

        //espace
        actions[5] = null;

        //undo
        actions[6] = jTableNode.updateAction;
        actions[6].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DesignTablePane.Undo_modifications")); 

        //check
        Icon icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/Check.gif")); 
        actions[7] = new NeatworkAction(Messages.getString(
                    "DesignTablePane.Apply"), icon,
                Messages.getString("DesignTablePane.Apply_modifications"),  
                'A') {
                    public void actionPerformed(ActionEvent e) {
                        apply();
                    }
                };
        jSplitPane.setTopComponent(new FancyTablePanel(Messages.getString(
                    "DesignTablePane.Node_list"), actions, 
                jTableNode));

        //BOTTOM
        actions = new Action[8];

        //AddPipe
        actions[0] = jTablePipe.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DesignTablePane.Insert_a_new_pipe")); 

        //DeletePipe
        actions[1] = jTablePipe.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DesignTablePane.Delete_selected_pipe(s)")); 

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
            Messages.getString("DesignTablePane.Undo_modifications")); 

        //check
        icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/Check.gif")); 
        actions[7] = new NeatworkAction(Messages.getString(
                    "DesignTablePane.Apply"), icon,
                Messages.getString("DesignTablePane.Apply_modifications"),  
                'A') {
                    public void actionPerformed(ActionEvent e) {
                        apply();
                    }
                };
        jSplitPane.setBottomComponent(new FancyTablePanel(Messages.getString(
                    "DesignTablePane.Arc_list"), 
                actions, jTablePipe));

        //TOTAL
        jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setContinuousLayout(false);
        jSplitPane.setResizeWeight(0.5);
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setDividerLocation(200);
        add(jSplitPane, BorderLayout.CENTER);

        //COUT
        costLabel = new JLabel();
        costLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 5));
        add(costLabel, BorderLayout.SOUTH);

        updateNodesTable();
        updatePipesTable();
        updateCostLabel();
    }

    private void updateNodesTable() {
        jTableNode.updateData();
    }

    private void updatePipesTable() {
        jTablePipe.updateData();
    }

    private void updateCostLabel() {
        costLabel.setText(Messages.getString(
                "DesignTablePane.Design_total_cost") + 
            Tools.doubleFormat("#,##0", design.getCost())); 
    }

    private void apply() {
        design.freeInfoModif();

        String content = design.getPropertiesContent() +
            nodesTableModel.getContent() + pipesTableModel.getContent();
        design.setContent(content);

        if (!design.getInfoModif().equals("")) { 
            JOptionPane.showMessageDialog(null,
                Messages.getString("DesignTablePane.error_in_your_project")); 
        }
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(design)) {
            switch (((Integer) param).intValue()) {
            case Design.MODIF_CONTENT:

                //do change
                updateNodesTable();
                updatePipesTable();
                updateCostLabel();

                break;
            }
        }
    }

    //label orifice
    class LblOrifice extends FancyTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            String t = Tools.doubleFormat("0.##########", 
                    Double.parseDouble(value.toString()));

            if (t.equals("10000")) { 
                t = "-"; 
            }

            return super.getTableCellRendererComponent(table, t, isSelected,
                hasFocus, row, column);
        }
    }
}
