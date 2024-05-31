package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * Modele de donnees pour les nodes d'une topographie
 * @author L. DROUET
 * @version 1.0
 */
public class TopoNodeTableModel extends FancyTableModel {
    private String[] headerNode = {
        Messages.getString("TopoNodeTableModel._ID"),
        Messages.getString("TopoNodeTableModel._Height"),
        Messages.getString("TopoNodeTableModel._X"),
        Messages.getString("TopoNodeTableModel._Y"),
        Messages.getString("TopoNodeTableModel.__#_of_faucets"),
        Messages.getString("TopoNodeTableModel._Nature")   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    };
    private int[] myWhidthHeader = { 100, 100, 50, 50, 75, 150 };
    private Topographie topo;

    public TopoNodeTableModel(Topographie topo) {
        this.topo = topo;
        header = headerNode;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Iterator iter = topo.getNodeIterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();
            Vector line = new Vector();
            line.add(node.getName());
            line.add(new Double(node.getHeight()));
            line.add(new Double(node.getCoordX()));
            line.add(new Double(node.getCoordY()));
            line.add(new Integer(node.getNbTaps()));
            line.add(new Integer(node.getType()));
            data.add(line);
        }

        //informe la table de la mise a jour;
        fireTableDataChanged();
    }

    public Class getColumnClass(int col) {
        switch (col + 1) {
        case 1:
            return String.class;

        case 2:
            return Double.class;

        case 3:
            return Double.class;

        case 4:
            return Double.class;

        case 5:
            return Integer.class;

        case 6:
            return String.class;

        default:
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        Vector line = new Vector();
        line.add(getFirstPossibleName("N")); 
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Integer(0));
        line.add(new Integer(1));

        return line;
    }

    public boolean isCellEditable(int row, int col) {
        return (col != 5);
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            if (value.toString().indexOf("_") > 0) { 
                value = value.toString().substring(0,
                        value.toString().indexOf("_")); 
            }
        }

        super.setValueAt(value, row, col);
    }

    public Object getValueAt(int row, int col) {
        Object o = super.getValueAt(row, col);

        return ((col != 5) ? o : Node.getNameType(((Integer) o).intValue()));
    }

    private String getFirstPossibleName(String name) {
        String old = name;
        int cpt = 0;

        while (isExist(name)) {
            name = old + (++cpt);
        }

        return name;
    }

    private boolean isExist(String name) {
        boolean find = false;
        Enumeration e = data.elements();

        while ((!find) && e.hasMoreElements()) {
            find = ((Vector) e.nextElement()).get(0).toString().equals(name);
        }

        return find;
    }
}
