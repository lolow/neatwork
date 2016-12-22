package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * Modele de donnees pour les noeuds d'un design
 * @author L. DROUET
 * @version 1.0
 */
public class DesignNodeModel extends FancyTableModel {
    private String[] headerNode = {
        Messages.getString("DesignNodeModel.ID"),
        Messages.getString("DesignNodeModel.Height"),
        Messages.getString("DesignNodeModel.X"),
        Messages.getString("DesignNodeModel.Y"),
        Messages.getString("DesignNodeModel.Ideal_Orifice"),
        Messages.getString("DesignNodeModel.Commercial_Orifice"),   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        Messages.getString("DesignNodeModel.Nature") 
    };
    private int[] myWhidthHeader = { 100, 100, 50, 50, 120, 120, 100 };
    private Design design;

    public DesignNodeModel(Design design) {
        this.design = design;
        header = headerNode;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Iterator iter = design.getNodeIterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();
            Vector line = new Vector();
            line.add(node.getName());
            line.add(new Double(node.getHeight()));
            line.add(new Double(node.getCoordX()));
            line.add(new Double(node.getCoordY()));
            line.add(new Double(node.getOrifice()));
            line.add(new Double(node.getComercialOrifice()));
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
            return Double.class;

        case 6:
            return Double.class;

        case 7:
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
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Integer(1));

        return line;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 6) {
            return false;
        }

        return true;
    }

    public Object getValueAt(int row, int col) {
        Object o = super.getValueAt(row, col);

        if (col == 6) {
            return Node.getNameType(((Integer) o).intValue());
        }

        return o;
    }

    public void setValueAt(Object o, int row, int col) {
        if ((col == 4) || (col == 5)) {
            if (o.toString().indexOf("-") > -1) { 
                o = new Double(10000);
            }
        }

        super.setValueAt(o, row, col);
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
