package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.util.*;

/**
 * Modele de donnees pour les tuyaux
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DesignPipeModel extends FancyTableModel {
	private String[] headerNode = { Messages.getString("DesignPipeModel.Begin"),
			Messages.getString("DesignPipeModel.End"), Messages.getString("DesignPipeModel.Length"),
			Messages.getString("DesignPipeModel.Length_1"), Messages.getString("DesignPipeModel.diam_1"),
			Messages.getString("DesignPipeModel.Length_2"), Messages.getString("DesignPipeModel.diam_2") };
	private int[] myWhidthHeader = { 80, 80, 70, 80, 80, 80, 80 };
	private Design design;
	private Database database;

	public DesignPipeModel(Design design, Database database, FancyTable table) {
		this.design = design;
		this.database = database;
		header = headerNode;
		widthHeader = myWhidthHeader;
		updateData();
	}

	public void updateData() {
		data = new Vector();

		Iterator iter = design.getPipeIterator();

		while (iter.hasNext()) {
			Pipe pipe = (Pipe) iter.next();
			Vector line = new Vector();
			line.add(pipe.getBegin());
			line.add(pipe.getEnd());
			line.add(new Double(pipe.getLength()));
			line.add(new Double(pipe.getLength1()));
			line.add(pipe.getRefDiam1());
			line.add(new Double(pipe.getLength2()));
			line.add(pipe.getRefDiam2());
			line.add("N");
			data.add(line);
		}
	}

	public Class getColumnClass(int col) {
		switch (col + 1) {
		case 1:
			return String.class;

		case 2:
			return String.class;

		case 3:
			return Double.class;

		case 4:
			return Double.class;

		case 5:
			return String.class;

		case 6:
			return Double.class;

		case 7:
			return String.class;

		default:
			return Object.class;
		}
	}

	protected Vector getNewVector() {
		Vector line = new Vector();
		line.add(design.getLastNode().getName());
		line.add(design.getLastNode().getName());
		line.add(new Double(0));
		line.add(new Double(0));
		line.add("0");
		line.add(new Double(0));
		line.add("0");
		line.add("N");

		return line;
	}

	public Object getValueAt(int row, int col) {
		Object o = super.getValueAt(row, col);

		if (col == 6) {
			if (((Double) getValueAt(row, 5)).doubleValue() == 0) {
				return "-";
			}
		}

		return o;
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 6) {
			if (getValueAt(row, 6).toString().equals("-")) {

				return false;
			}
		}

		return super.isCellEditable(row, col);
	}

	public void setValueAt(Object obj, int row, int col) {
		if (((col == 4) || (col == 6)) && (obj != null)) {
			String diam = ((String) obj).substring(5, obj.toString().indexOf(":"));
			String sdr = ((String) obj).substring(obj.toString().indexOf(":") + 5, obj.toString().lastIndexOf(":"));
			String type = ((String) obj).substring(obj.toString().lastIndexOf(":"));
			int ttype = 1;

			for (int i = 1; i < Diameter.typeName.length; i++) {
				if (type.equals(Diameter.typeName[i])) {
					ttype = i;
				}
			}

			Diameter temp = new Diameter();
			temp.setDiameter(Double.parseDouble(diam));
			temp.setSdr(Double.parseDouble(sdr));
			temp.setType(ttype);

			// ce diametre existe-t-il dans la base de design ?
			String ref = getRef(design.getDiamTable(), temp);

			if (ref != null) {
				obj = ref;
			} else {
				// ajout du diametre
				ref = getRef(database.getDiametersTable(), temp);
				obj = "D" + (design.getDiamTable().size() + 1);
				design.getDiamTable().put(obj.toString(), database.getDiametersTable().get(ref));
			}
		}

		super.setValueAt(obj, row, col);
	}

	public String getContent() {
		// copy de la table
		String content = super.getContent();
		content += design.getDiametersContent();

		return content;
	}

	// renvoie la reference correspondante au diametre dans la hashtable, null
	// sinon
	private String getRef(Hashtable htable, Diameter diam) {
		String ref = null;
		Enumeration e = htable.keys();

		while ((ref == null) && (e.hasMoreElements())) {
			String s = e.nextElement().toString();

			if (((Same) htable.get(s)).isSame(diam)) {
				ref = s;
			}
		}

		return ref;
	}

	/**
	 * paste le contenu renvoie true si ca c'est bien passe
	 */
	public boolean setPaste(String clip) {
		// verifie le format
		StringTokenizer st1 = new StringTokenizer(clip, "\n");
		boolean isOk = true;

		while ((st1.hasMoreTokens()) && (isOk)) {
			StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "\t");
			isOk = st2.countTokens() == getColumnCount();
		}

		// paste dans le data
		if (isOk) {
			data.clear();

			Vector nline = getNewVector();
			st1 = new StringTokenizer(clip, "\n");

			while (st1.hasMoreTokens()) {
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

				line.add("N");
				data.add(line);
			}

			fireTableDataChanged();
		}

		return isOk;
	}
}
