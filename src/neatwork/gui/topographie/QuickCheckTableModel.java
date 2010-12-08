package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * Modele de donnee pour quickCheck
 * @author L. DROUET
 * @version 1.0
 */
public class QuickCheckTableModel extends FancyTableModel {
    private int[] myWhidthHeader = { 100, 100, 100 };
    private double seuil;
    private double targetflow;
    private double alpha;
    private Topographie topo;

    public QuickCheckTableModel(Topographie topo) {
        this.topo = topo;
        targetflow = Double.parseDouble(topo.getProperties().getProperty("topo.targetflow.value")); //$NON-NLS-1$
        alpha = Double.parseDouble(topo.getProperties().getProperty("topo.faucetcoef.value")); //$NON-NLS-1$

        String[] myHeader = {
            Messages.getString("QuickCheckTableModel.Faucet_ID"),
            Messages.getString("QuickCheckTableModel.flow_min_(") + seuil +
            Messages.getString("QuickCheckTableModel._l/s)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Messages.getString("QuickCheckTableModel.target_flow_(") +
            targetflow +
            Messages.getString("QuickCheckTableModel._l/s)") //$NON-NLS-1$ //$NON-NLS-2$
        };
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        seuil = Double.parseDouble(topo.getProperties().getProperty("topo.seuil.value", //$NON-NLS-1$
                    "0.1")); //$NON-NLS-1$
        alpha = Double.parseDouble(topo.getProperties().getProperty("topo.faucetcoef.value")); //$NON-NLS-1$

        String[] myHeader = {
            Messages.getString("QuickCheckTableModel.Faucet_ID"),
            Messages.getString("QuickCheckTableModel.flow_min_(") + seuil +
            Messages.getString("QuickCheckTableModel._l/s)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Messages.getString("QuickCheckTableModel.target_flow_(") +
            targetflow +
            Messages.getString("QuickCheckTableModel._l/s)") //$NON-NLS-1$ //$NON-NLS-2$
        };
        header = myHeader;
        data = new Vector();

        Iterator iter = topo.getNodeIterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();

            if (node.getType() == Node.TYPE_FAUCET) {
                Vector line = new Vector();
                line.add(node.getName());
                line.add((-node.getHeight() > (Math.pow(seuil / 1000, 2) / alpha))
                    ? Messages.getString("QuickCheckTableModel.Ok")
                    : Messages.getString("QuickCheckTableModel.No")); //$NON-NLS-1$ //$NON-NLS-2$
                line.add((-node.getHeight() > (Math.pow(targetflow / 1000, 2) / alpha))
                    ? Messages.getString("QuickCheckTableModel.Ok")
                    : Messages.getString("QuickCheckTableModel.No")); //$NON-NLS-1$ //$NON-NLS-2$
                data.add(line);
            }
        }

        //informe la table de la mise a jour;
        fireTableStructureChanged();
    }

    public Class getColumnClass(int col) {
        switch (col + 1) {
        case 1:
            return String.class;

        case 2:
            return String.class;

        case 3:
            return String.class;

        default:
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        return null;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
