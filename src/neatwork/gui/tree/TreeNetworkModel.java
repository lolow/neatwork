package neatwork.gui.tree;

import java.awt.Point;

import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.table.DefaultTableModel;


/**
 * modele de donne pour l'arbre
 * @author L. DROUET
 * @version 1.0
 */
public class TreeNetworkModel extends DefaultTableModel {
    public Hashtable tableCoord;
    private int nbrow = 0;
    private int nbcol = 0;

    public TreeNetworkModel(Hashtable coord) {
        this.tableCoord = coord;

        Iterator iter = tableCoord.keySet().iterator();

        while (iter.hasNext()) {
            Point item = (Point) iter.next();
            nbcol = Math.max(nbcol, item.x + 1);
            nbrow = Math.max(nbrow, item.y + 1);
        }
    }

    public String getColumnName(int i) {
        return "" + i; //$NON-NLS-1$
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public Object getValueAt(int row, int col) {
        Point p = new Point(col, row);
        Object o = tableCoord.get(p);

        if (o != null) {
            return o.toString();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public int getColumnCount() {
        return nbcol;
    }

    public int getRowCount() {
        return nbrow;
    }
}
