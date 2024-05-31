package neatwork.gui.database;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.util.*;

/**
 * modele de donnee pour les orifices
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DBOrificeTableModel extends FancyTableModel {
	private String[] myHeader = { Messages.getString("DBOrificeTableModel.Diameter") };
	private int[] myWhidthHeader = { 100 };
	protected Database database;

	public DBOrificeTableModel(Database database) {
		this.database = database;
		header = myHeader;
		widthHeader = myWhidthHeader;
		updateData();
	}

	public void updateData() {
		data = new Vector();

		Iterator iter = database.getOrifices().iterator();

		while (iter.hasNext()) {
			Orifice d = (Orifice) iter.next();
			Vector line = new Vector();
			line.add(new Double(d.getDiameter()));
			data.add(line);
		}

		fireTableDataChanged();
	}

	public Class getColumnClass(int col) {
		switch (col) {
		case 0:
			return Double.class;

		default:
			return Object.class;
		}
	}

	protected Vector getNewVector() {
		Vector line = new Vector();
		line.add(new Double(0));

		return line;
	}
}
