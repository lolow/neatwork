package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.database.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.util.*;


/**
 * modele de table de selection de diametre
 * @author L. DROUET
 * @version 1.0
 */
public class ChooseDiameterTableModel extends DBDiameterTableModel {
    private String[] myHeader = {
        " ", Messages.getString("ChooseDiameterTableModel.Nominal"),
        Messages.getString("ChooseDiameterTableModel.SDR"),
        Messages.getString("ChooseDiameterTableModel.Diam"),
        Messages.getString("ChooseDiameterTableModel.Cost"),
        Messages.getString("ChooseDiameterTableModel.Max_Pressure"),
        Messages.getString("ChooseDiameterTableModel.Type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        Messages.getString("ChooseDiameterTableModel.Roughness") //$NON-NLS-1$
    };
    private int[] myWhidthHeader = { 15, 55, 45, 45, 45, 65, 80, 75 };

    public ChooseDiameterTableModel(Database database) {
        super(database);
        header = myHeader;
        widthHeader = myWhidthHeader;
    }

    public void updateData() {
        data = new Vector();

        Enumeration iter = database.getDiameters();

        while (iter.hasMoreElements()) {
            Diameter d = (Diameter) iter.nextElement();
            Vector line = new Vector();
            line.add(new Boolean(false));
            line.add(d.getNominal());
            line.add(new Double(d.getSdr()));
            line.add(new Double(d.getDiameter()));
            line.add(new Double(d.getCost()));
            line.add(new Double(d.getMaxLength()));
            line.add(new Integer(d.getType()));
            line.add(new Double(d.getRoughness()));
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
                Vector diam = new Vector(item);
                diam.remove(0);
                v.add(diam);
            }
        }

        return Tools.getTxt(v);
    }
}
