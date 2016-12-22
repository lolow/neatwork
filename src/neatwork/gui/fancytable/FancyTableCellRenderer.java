package neatwork.gui.fancytable;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;


/**
 * Render de cellule
 * @author L. DROUET
 * @version 1.0
 */
public class FancyTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof Double) {
            double z = Double.parseDouble(value.toString());
            ((JLabel) cell).setText("" + z); 
            ((JLabel) cell).setHorizontalAlignment(JLabel.RIGHT);
            ((JLabel) cell).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            cell.setFont(table.getFont());
        }

        if (value instanceof String) {
            ((JLabel) cell).setHorizontalAlignment(JLabel.RIGHT);
            ((JLabel) cell).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            cell.setFont(table.getFont());
        }

        if (value instanceof Integer) {
            ((JLabel) cell).setHorizontalAlignment(JLabel.RIGHT);
            ((JLabel) cell).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            cell.setFont(table.getFont());
        }

        return cell;
    }
}
