package neatwork.gui.simu;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * modele de donnees pour la selection des robinets
 * @author L. DROUET
 * @version 1.0
 */
public class TapSelectionModel extends FancyTableModel {
    private String[] myHeader = {
        " ", Messages.getString("TapSelectionModel.Faucet")
    };  
    private int[] myWhidthHeader = { 15, 55 };
    private Design design;

    public TapSelectionModel(Design design) {
        this.design = design;
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Iterator iter = design.getNodeIterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();

            if (node.getType() == Node.TYPE_FAUCET) {
                Vector line = new Vector();
                line.add(new Boolean(false));
                line.add(node.getName());
                data.add(line);
            }
        }

        fireTableDataChanged();
    }

    protected Vector getNewVector() {
        return null;
    }

    public Class getColumnClass(int col) {
        return (col == 0) ? Boolean.class : String.class;
    }

    public void setall(int[] row, boolean b) {
        if (row.length == 0) {
            Enumeration e = data.elements();

            while (e.hasMoreElements()) {
                ((Vector) e.nextElement()).set(0, new Boolean(b));
            }
        } else {
            for (int i = 0; i < row.length; i++) {
                Vector v = (Vector) data.get(row[i]);
                v.set(0, new Boolean(b));
            }
        }

        fireTableDataChanged();
    }

    public Hashtable getFaucetRef() {
        Hashtable table = new Hashtable();
        Enumeration e = data.elements();

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();
            table.put(item.get(1), item.get(0));
        }

        return table;
    }

    public boolean isCellEditable(int row, int col) {
        return (col == 0);
    }
}
