package neatwork.gui.fancytable;

import neatwork.utils.*;

import java.util.*;

import javax.swing.table.*;


/**
 * Table Model pour une fancyTable
 * @author L. DROUET
 * @version 1.0
 */
public abstract class FancyTableModel extends AbstractTableModel {
    protected Vector data = new Vector();
    protected String[] header = {  };
    protected int[] widthHeader = {  };
    protected int row;

    public FancyTableModel() {
    }

    /** mets a jour les donn�es*/
    public abstract void updateData();

    public String getColumnName(int i) {
        return header[i];
    }

    public void setValueAt(Object object, int row, int col) {
        ((Vector) data.get(row)).set(col, object);
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public abstract Class getColumnClass(int col);

    public Object getValueAt(int row, int col) {
        return ((Vector) data.get(row)).get(col);
    }

    public int getColumnCount() {
        return header.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getContent() {
        return Tools.getTxt(data);
    }

    public void addNewLine() {
        addNewLine(data.size());
    }

    public void addNewLine(int i) {
        if ((i < 1) || (i > data.size())) {
            i = data.size();
        }

        data.insertElementAt(getNewVector(), i);
        row = i;
        fireTableDataChanged();
    }

    public void deleteLines(int[] rows) {
        Arrays.sort(rows);

        for (int i = rows.length - 1; i > -1; i--) {
            data.remove(rows[i]);
            row = rows[i];
        }

        fireTableDataChanged();
    }

    public int getRow() {
        return row;
    }

    protected abstract Vector getNewVector();

    public int[] getWidthHeader() {
        return widthHeader;
    }

    /**
     * copy le contenu de la table dans un string
     */
    public String getCopy() {
        String copy = ""; 

        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                copy += ((Vector) data.get(i)).get(j).toString();

                if (j < (getColumnCount() - 1)) {
                    copy += "\t"; 
                }
            }

            if (i < getRowCount()) {
                copy += "\n"; 
            }
        }

        return copy;
    }

    /**
     * paste le contenu
     * renvoie true si ca c'est bien pass�
     */
    public boolean setPaste(String clip) {
        //v�rifie le format
        StringTokenizer st1 = new StringTokenizer(clip, "\n"); 
        boolean isOk = true;

        while ((st1.hasMoreTokens()) && (isOk)) {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "\t"); 
            isOk = st2.countTokens() == getColumnCount();
        }

        //paste dans le data
        if (isOk) {
            data.clear();

            Vector nline = getNewVector();
            st1 = new StringTokenizer(clip, "\n"); 

            for (int i = 0; st1.hasMoreTokens(); i++) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "\t"); 
                Vector line = new Vector();

                for (int j = 0; st2.hasMoreTokens(); j++) {
                    String cell = st2.nextToken();

                    if (nline.get(j) instanceof String) {
                        line.add(cell);
                    } else if (nline.get(j) instanceof Double) {
                        line.add(new Double(cell));
                    } else if (nline.get(j) instanceof Integer) {
                        line.add(new Integer(cell));
                    }
                }

                data.add(line);
            }

            fireTableDataChanged();
        }

        return isOk;
    }
}
