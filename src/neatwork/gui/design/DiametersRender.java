package neatwork.gui.design;

import neatwork.project.*;

import java.awt.Component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;


/**
 * renderer pour les diameters
 * @author L. DROUET
 * @version 1.0
 */
public class DiametersRender extends JLabel implements TableCellRenderer {
    private Design design;

    public DiametersRender(Design design) {
        this.design = design;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        Diameter diam = (Diameter) design.getDiamTable().get(value);
        setText(((diam != null) ? ("" + diam.getDiameter()) : "-"));  

        return this;
    }
}
