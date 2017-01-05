package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.makedesign.*;

import java.util.*;

/**
 * modele de donnees pour les parametres de la table
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DesignLoadFactorModel extends ParametersTableModel {
	private String[] myHeader = { Messages.getString("DesignLoadFactorModel.Ending_Node"),
			Messages.getString("DesignLoadFactorModel.Load_Factor") };

	public DesignLoadFactorModel(Properties propLoadFactor) {
		super(propLoadFactor);
		header = myHeader;
	}

	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}
}
