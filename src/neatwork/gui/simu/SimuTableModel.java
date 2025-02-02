package neatwork.gui.simu;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;

import java.util.*;


/**
 * modele de donnees pour les tables de simulation
 * @author L. DROUET
 * @version 1.0
 */
public class SimuTableModel extends FancyTableModel {
    public final static int TYPE_NULL = 0;
    public final static int TYPE_DESIGNDIFF = 1;
    public final static int TYPE_FLOWTAPS = 2;
    public final static int TYPE_QUARTILETAPS = 3;
    public final static int TYPE_SPEEDPIPE = 4;
    public final static int TYPE_NODEPRESSURE = 5;
    private String[] myHeader1 = { Messages.getString("SimuTableModel.Comments") }; 
    private int[] myWhidth1 = { 100 };
    private String[] myHeader2 = {
        Messages.getString("SimuTableModel.Faucet_ID"),
        Messages.getString("SimuTableModel._#_of_occurences"),
        Messages.getString("SimuTableModel.Min"),
        Messages.getString("SimuTableModel.Average"),
        Messages.getString("SimuTableModel.Max"),
        Messages.getString("SimuTableModel.Variability"),   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        "<", ">",
        Messages.getString("SimuTableModel._#_of_failures")   //$NON-NLS-3$
    };
    private int[] myWhidth2 = { 50, 50, 50, 50, 50, 50, 50, 50, 50 };
    private String[] myHeader3 = {
        Messages.getString("SimuTableModel.Faucet_ID"),
        Messages.getString("SimuTableModel._#_of_occurences"),
        Messages.getString("SimuTableModel.Min"),
        Messages.getString("SimuTableModel.<10%"),
        Messages.getString("SimuTableModel.<25%"),
        Messages.getString("SimuTableModel.<50%"),
        Messages.getString("SimuTableModel.<75%"),   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        Messages.getString("SimuTableModel.<90%"),
        Messages.getString("SimuTableModel.Max")  
    };
    private int[] myWhidth3 = { 50, 50, 50, 50, 50, 50, 50, 50, 50 };
    private String[] myHeader4 = {
        Messages.getString("SimuTableModel.Pipe_ID"),
        Messages.getString("SimuTableModel._#_of_simulation"),
        Messages.getString("SimuTableModel.Average"),
        Messages.getString("SimuTableModel.Maximum")   //$NON-NLS-3$ //$NON-NLS-4$
    };
    private int[] myWhidth4 = { 50, 50, 50, 50 };
    private String[] myHeader5 = {
        Messages.getString("SimuTableModel.Node_ID"),
        Messages.getString("SimuTableModel.Minimum"),
        Messages.getString("SimuTableModel.Average"),
        Messages.getString("SimuTableModel.Maximum")
    };   //$NON-NLS-3$ //$NON-NLS-4$
    private int[] myWhidth5 = { 50, 50, 50, 50 };
    private String[] myHeader = { "" }; 
    private int[] myWhidthHeader = { 100 };
    private int currentType = 0;
    private Simulation simu;
    private FancyTable table;

    public SimuTableModel(Design design, Simulation simu, FancyTable table) {
        this.simu = simu;
        this.table = table;
        header = myHeader;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void setModel(int type) {
        currentType = type;
        updateData();
        table.autoFit();
    }


    private Vector<Vector<Object>> replaceValuesWithDashes(Vector<Vector<Object>> originalData) {
        Vector<Vector<Object>> modifiedData = new Vector<>();
        for (Vector<Object> row : originalData) {
            Vector<Object> newRow = new Vector<>();
            for (Object value : row) {
                newRow.add("-");
            }
            modifiedData.add(newRow);
        }
        return modifiedData;
    }

    
    public void updateData() {
        data = new Vector();
    
        switch (currentType) {
            case TYPE_DESIGNDIFF:
                header = myHeader1;
                widthHeader = myWhidth1;
    
                Vector v = new Vector();
                v.add(Messages.getString("SimuTableModel.Not_yet_implemented__!"));
                data.add(v);
                break;
    
            case TYPE_FLOWTAPS:
                header = myHeader2;
                widthHeader = myWhidth2;
                header[6] = "< " + simu.getProperties().getProperty("simu.mincriticalflow.value", "");
                header[7] = "> " + simu.getProperties().getProperty("simu.maxcriticalflow.value", "");
                data = simu.getFlowTaps();
                break;
    
            case TYPE_QUARTILETAPS:
                header = myHeader3;
                widthHeader = myWhidth3;
                data = simu.getQuartileTaps();
                break;
    
            case TYPE_SPEEDPIPE:
                header = myHeader4;
                widthHeader = myWhidth4;
                // data = simu.getSpeedPipe();
                data = replaceValuesWithDashes(simu.getSpeedPipe());
                break;
    
            case TYPE_NODEPRESSURE:
                header = myHeader5;
                widthHeader = myWhidth5;
                data = simu.getNodesPressure();
                // data = replaceValuesWithDashes(simu.getNodesPressure());
                break;
    
            default:
                header = myHeader;
                widthHeader = myWhidthHeader;
                break;
        }
    
        fireTableStructureChanged();
    
        switch (currentType) {
            case TYPE_DESIGNDIFF:
                break;
    
            case TYPE_FLOWTAPS:
                ProgressCellRenderer renderer = new ProgressCellRenderer();
                renderer.setStringPainted(true);
                renderer.setBackground(table.getBackground());
                renderer.setForeground(table.getForeground());
                renderer.setFont(table.getFont());
    
                // set limit value and fill color
                Hashtable limitColors = new Hashtable();
                limitColors.put(new Integer(0), Color.green);
                limitColors.put(new Integer(60), Color.yellow);
                limitColors.put(new Integer(80), Color.red);
                renderer.setLimits(limitColors);
                table.getColumnModel().getColumn(5).setCellRenderer(renderer);
                table.getColumnModel().getColumn(6).setCellRenderer(renderer);
                table.getColumnModel().getColumn(7).setCellRenderer(renderer);
                break;
    
            case TYPE_QUARTILETAPS:
                break;
    
            case TYPE_SPEEDPIPE:
                break;
    
            case TYPE_NODEPRESSURE:
                break;
    
            default:
                break;
        }
    }
    

    public Class getColumnClass(int col) {
        if (data.size() > 0) {
            return ((Vector) data.get(0)).get(col).getClass();
        } else {
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        return null;
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object o, int row, int col) {
    }
}
