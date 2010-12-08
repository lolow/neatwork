package neatwork.gui.makedesign;

import neatwork.Messages;

import java.util.*;

import javax.swing.*;


/**
 * modele de liste
 * @author L. DROUET
 * @version 1.0
 */
public class ConstraintsListModel extends AbstractListModel {
    Vector constraints;

    public ConstraintsListModel(Vector constraints) {
        this.constraints = constraints;
    }

    public int getSize() {
        return constraints.size();
    }

    public Object getElementAt(int i) {
        Vector line = (Vector) constraints.get(i);
        int type = ((Integer) line.get(0)).intValue();

        switch (type) {
        case 0:
            return "[ " + line.get(1) +
            Messages.getString(
                "ConstraintsListModel.]_pipe(s)_must_be_greater_than") + //$NON-NLS-1$ //$NON-NLS-2$
            line.get(2);

        case 1:
            return "[ " + line.get(1) +
            Messages.getString(
                "ConstraintsListModel.]_pipe(s)_must_be_lower_than") + //$NON-NLS-1$ //$NON-NLS-2$
            line.get(2);

        case 2:
            return "[ " + line.get(1) +
            Messages.getString(
                "ConstraintsListModel.]_pipe(s)_must_be_equal_to") + //$NON-NLS-1$ //$NON-NLS-2$
            line.get(2);

        case 3:
            return "[ " + line.get(1) +
            Messages.getString("ConstraintsListModel.]_pipe_1_is_(diam") +
            line.get(2) + //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("ConstraintsListModel._and_length") +
            line.get(3) +
            Messages.getString("ConstraintsListModel.)_and_pipe_2_is_(diam") + //$NON-NLS-1$ //$NON-NLS-2$
            line.get(4) + ")"; //$NON-NLS-1$
        }

        return Messages.getString("ConstraintsListModel.bad_constraint"); //$NON-NLS-1$
    }

    public void addConstraints() {
        fireIntervalAdded(this, 0, constraints.size());
    }

    public void removeConstraints() {
        fireIntervalRemoved(this, 0, constraints.size());
    }
}
