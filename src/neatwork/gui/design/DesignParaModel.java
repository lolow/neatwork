package neatwork.gui.design;

import neatwork.gui.makedesign.*;

import neatwork.project.*;

/**
 * modele de donnees pour les parametres de la table
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DesignParaModel extends ParametersTableModel {
	public DesignParaModel(Design design) {
		super(design.getProperties());
	}

	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}
}
