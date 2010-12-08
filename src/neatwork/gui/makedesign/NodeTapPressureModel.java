package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import java.util.*;


/**
 * modele de donnees pour le node et tap pressure
 * @author L. DROUET
 * @version 1.0
 */
public class NodeTapPressureModel extends FancyTableModel {
    private String[] myHeader = {
        Messages.getString("NodeTapPressureModel.Node_ID"),
        Messages.getString("NodeTapPressureModel.Height"),
        Messages.getString("NodeTapPressureModel.Forecast_Pressure_Loss"),
        Messages.getString("NodeTapPressureModel.Potential_Problem") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    };
    private int[] myWhidthHeader = { 100, 50, 150, 150 };
    private Vector pressure;

    public NodeTapPressureModel(Vector pressure) {
        this.pressure = pressure;
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Enumeration e = pressure.elements();

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();
            Vector line = new Vector();
            line.add(item.get(0).toString());
            line.add(new Double(item.get(1).toString()));
            line.add(new Double(item.get(2).toString()));
            line.add(new Double(item.get(3).toString()));
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

        default:
            return Object.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    protected Vector getNewVector() {
        return null;
    }
}
