package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;


/**
 * Modele de donnees pour les pipes d'une topographie
 * @author L. DROUET
 * @version 1.0
 */
public class TopoPipeTableModel extends FancyTableModel {
    private String[] headerPipe = {
        Messages.getString("TopoPipeTableModel._Begin"),
        Messages.getString("TopoPipeTableModel._End"),
        Messages.getString("TopoPipeTableModel._Length")
    }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private int[] myWhidthHeader = { 100, 100, 100 };
    private Topographie topo;

    public TopoPipeTableModel(Topographie topo) {
        this.topo = topo;
        header = headerPipe;
        widthHeader = myWhidthHeader;
        updateData();
    }

    public void updateData() {
        data = new Vector();

        Iterator iter = topo.getPipeIterator();

        while (iter.hasNext()) {
            Pipe pipe = (Pipe) iter.next();
            Vector line = new Vector();
            line.add(pipe.getBegin());
            line.add(pipe.getEnd());
            line.add(new Double(pipe.getLength()));
            data.add(line);
        }

        //informe la table de la mise a jour;
        fireTableDataChanged();
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case 0:
            return String.class;

        case 1:
            return String.class;

        case 2:
            return Double.class;

        default:
            return Object.class;
        }
    }

    protected Vector getNewVector() {
        Vector line = new Vector();
        line.add(topo.getLastNode().getName());
        line.add(topo.getLastNode().getName());
        line.add(new Double(0));

        return line;
    }
}
