package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * modele de donnees pour les facteurs de charge
 * @author L. DROUET
 * @version 1.0
 */
public class LoadFactorTableModel extends FancyTableModel {
    private String[] myHeader = {
        Messages.getString("LoadFactorTableModel.Begin"),
        Messages.getString("LoadFactorTableModel.End_2"),
        Messages.getString("LoadFactorTableModel.__#_of_faucets"),
        Messages.getString("LoadFactorTableModel.Theoric_load_factor"),   //$NON-NLS-3$ //$NON-NLS-4$
        Messages.getString("LoadFactorTableModel.Modified_load_factor") 
    };
    private int[] myWhidthHeader = { 50, 50, 100, 120, 120 };
    private Topographie topo;
    private Hashtable loadTaps;
    private Hashtable loadFactors;
    private Hashtable modifLoadFactor;
    private double targetflow;
    private double servicequal;
    private double opentaps;

    public LoadFactorTableModel(Topographie topographie) {
        this.topo = topographie;
        header = myHeader;
        widthHeader = myWhidthHeader;
        targetflow = -99;
        opentaps = -99;
        servicequal = -99;
        updateData();
    }

    public void updateData() {
        boolean changed = propertiesHasChanged();

        if (changed) {
            loadTaps = topo.getLoadTaps();
            loadFactors = topo.getLoadFactor(loadTaps);
            modifLoadFactor = new Hashtable(loadFactors);
        }

        data = new Vector();

        Iterator iter = topo.getExpandedNodeIterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();

            if (node.getType() != Node.TYPE_RESERVOIR) {
                Vector line = new Vector();
                line.add(topo.getExpandedPredPipe(node).getBegin());
                line.add(node.getName());
                line.add(new Integer(loadTaps.get(node.getName()).toString()));
                line.add(new Double(loadFactors.get(node.getName()).toString()));
                line.add(new Double(modifLoadFactor.get(node.getName())
                                                   .toString()));
                data.add(line);
            }
        }

        fireTableDataChanged();
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case 0:
            return String.class;

        case 1:
            return String.class;

        case 2:
            return Integer.class;

        case 3:
            return Double.class;

        case 4:
            return Double.class;

        default:
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        Vector line = new Vector();
        line.add(""); 
        line.add(""); 
        line.add(new Integer(0));
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(""); 

        return line;
    }

    public boolean isCellEditable(int row, int col) {
        return (col == 4);
    }

    public void setValueAt(Object object, int row, int col) {
        super.setValueAt(object, row, col);

        if (col == 4) {
            modifLoadFactor.put(getValueAt(row, 1), object);
        }
    }

    public void reset() {
        modifLoadFactor = new Hashtable(loadFactors);
    }

    private boolean propertiesHasChanged() {
        double targetflow0 = Double.parseDouble(topo.getProperties()
                                                    .getProperty("topo.targetflow.value")); 
        double opentaps0 = Double.parseDouble(topo.getProperties().getProperty("topo.opentaps.value")); 
        double qualite0 = Double.parseDouble(topo.getProperties().getProperty("topo.servicequal.value")); 
        boolean changed;
        changed = ((targetflow != targetflow0) || (opentaps != opentaps0) ||
            (qualite0 != servicequal));
        targetflow = targetflow0;
        opentaps = opentaps0;
        servicequal = qualite0;

        return changed;
    }

    public Hashtable getLoadFactor() {
        return modifLoadFactor;
    }
}
