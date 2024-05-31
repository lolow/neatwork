package neatwork.gui.design;

import neatwork.project.*;

import java.awt.*;

import javax.swing.*;

/**
 * jcombobox qui contient la base de donnee
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DiametersEditor extends DefaultCellEditor {
	private Design design;
	private ComboDatabase combo;

	public DiametersEditor(ComboDatabase combo, Design design) {
		super(combo);
		this.combo = combo;
		this.design = design;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Diameter diam = (Diameter) design.getDiamTable().get(value);

		if (diam != null) {
			combo.setSelectedItem(combo.getEnonce(diam));
		} else {
			combo.setSelectedIndex(0);
		}

		return combo;
	}
}
