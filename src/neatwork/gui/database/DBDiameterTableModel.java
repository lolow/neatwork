package neatwork.gui.database;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * modele de donnee pour les diametres
 * @author L. DROUET
 * @version 1.0
 */
public class DBDiameterTableModel extends FancyTableModel {
    private String[] myHeader = {
        Messages.getString("DBDiameterTableModel.Nominal"),
        Messages.getString("DBDiameterTableModel.SDR"),
        Messages.getString("DBDiameterTableModel.Diameter"),
        Messages.getString("DBDiameterTableModel.Cost/m"),
        Messages.getString("DBDiameterTableModel.Max_Pressure"),
        Messages.getString("DBDiameterTableModel.Type"),   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        Messages.getString("DBDiameterTableModel.Roughness") 
    };
    private int[] myWhidthHeader = { 75, 75, 75, 75, 75, 80, 75 };
    protected Database database;

    public DBDiameterTableModel(Database database) {
        this.database = database;
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Enumeration iter = database.getDiameters();

        while (iter.hasMoreElements()) {
            Diameter d = (Diameter) iter.nextElement();
            Vector line = new Vector();
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
        switch (col) {
        case 0:
            return String.class;

        case 1:
            return Double.class;

        case 2:
            return Double.class;

        case 3:
            return Double.class;

        case 4:
            return Double.class;

        case 5:
            return Integer.class;

        case 6:
            return Double.class;

        default:
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        Vector line = new Vector();
        line.add(""); 
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Double(0));
        line.add(new Integer(1));
        line.add(new Double(0));

        return line;
    }
}
