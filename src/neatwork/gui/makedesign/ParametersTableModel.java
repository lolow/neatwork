package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.utils.*;

import java.util.*;


/**
 * Mod\u00E8le de donnees pour les parametres
 * @author L. DROUET
 * @version 1.0
 */
public class ParametersTableModel extends FancyTableModel {
    private String[] myHeader = {
        Messages.getString("ParametersTableModel.Name"),
        Messages.getString("ParametersTableModel.Value")
    }; //$NON-NLS-1$ //$NON-NLS-2$
    private int[] myWhidthHeader = { 200, 100 };
    private Properties properties;

    public ParametersTableModel(Properties properties) {
        this.properties = properties;
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Enumeration e = properties.propertyNames();

        while (e.hasMoreElements()) {
            String p = e.nextElement().toString();

            if (p.startsWith("topo.") && (p.endsWith(".value"))) { //$NON-NLS-1$ //$NON-NLS-2$

                Vector line = new Vector();
                line.add(Messages.getString(p.substring(0,p.length()-6)+".name"));
                line.add(new Double(properties.getProperty(p, "0"))); //$NON-NLS-1$ //$NON-NLS-2$
                line.add(p); //$NON-NLS-1$
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
            return Double.class;

        default:
            return Object.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        return (col == 1);
    }

    public String getContent() {
        Vector v = new Vector();
        Enumeration e = data.elements();

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();
            Vector line = new Vector();
            line.add(item.get(1));
            line.add(item.get(0));
            v.add(item);
        }

        return Tools.getTxt(v);
    }

    protected Vector getNewVector() {
        Vector line = new Vector();
        line.add(""); //$NON-NLS-1$
        line.add(new Double(0));
        line.add(""); //$NON-NLS-1$

        return line;
    }
}
