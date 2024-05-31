package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.database.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.util.*;


/**
 * modele de donnees pour l'orifice
 * @author L. DROUET
 * @version 1.0
 */
public class ChooseOrificeTableModel extends DBOrificeTableModel {
    private String[] myHeader = {
        " ", Messages.getString("ChooseOrificeTableModel.Diameter")
    };  
    private int[] myWhidthHeader = { 15, 100 };

    public ChooseOrificeTableModel(Database database) {
        super(database);
        header = myHeader;
        widthHeader = myWhidthHeader;
    }

    public void updateData() {
        data = new Vector();

        Iterator iter = database.getOrifices().iterator();

        while (iter.hasNext()) {
            Orifice d = (Orifice) iter.next();
            Vector line = new Vector();
            line.add(new Boolean(false));
            line.add(new Double(d.getDiameter()));
            data.add(line);
        }

        fireTableDataChanged();
    }

    public Class getColumnClass(int col) {
        return (col == 0) ? Boolean.class : super.getColumnClass(col - 1);
    }

    public boolean isCellEditable(int row, int col) {
        return (col == 0);
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

    public String getContent() {
        Vector v = new Vector();
        Enumeration e = data.elements();

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();

            if (item.get(0).equals(new Boolean(true))) {
                Vector orif = new Vector(item);
                orif.remove(0);
                v.add(orif);
            }
        }

        return Tools.getTxt(v);
    }
}
