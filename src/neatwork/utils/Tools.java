package neatwork.utils;

import neatwork.Messages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.*;

import java.text.*;

import java.util.*;
import java.util.List;

import javax.swing.*;

/**
 * Classe qui est bien utile
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class Tools {
	static String repertoire = "";

	/**
	 * lit un string csv et le met dans un Vector de Vector
	 */
	public static Vector readCSV(String contenu) {
		Vector v = new Vector();
		StringTokenizer stline = new StringTokenizer(contenu, "\n\r");

		while (stline.hasMoreTokens()) {
			String line = stline.nextToken();
			StringTokenizer st = new StringTokenizer(line, ",");
			Vector data = new Vector();

			while (st.hasMoreTokens())
				data.add(st.nextToken());

			v.add(data);
		}

		return v;
	}

	/**
	 * renvoie un string formate d'un nombre.
	 * <p>
	 * exemple:<br>
	 * doubleFormat("0000000000", 12345.0))->0000012345<br>
	 * doubleFormat("00000.00", 456.1))->00456.10<br>
	 * doubleFormat("00000.00", 123456.789)>123456.79<br>
	 * doubleFormat("0.##", 123456.701)>123456.7<br>
	 */
	public static String doubleFormat(String mask, double d) {
		DecimalFormat df = new DecimalFormat(mask);
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

		return df.format(d);
	}

	/** renvoie le txt d'un vector avec un delimiteur */
	public static String getTxt(Vector<Vector<?>> data, String delimiter) {
		String s = "";

		for (int i = 0; i < data.size(); i++) {
			String line = "";
			Vector<?> vline = data.get(i);

			for (int j = 0; j < vline.size(); j++) {
				line += (vline.get(j).toString() + ',');
			}

			line = line.substring(0, line.length() - 1);
			s += (line + "\n");
		}

		return s;
	}

	/** renvoie le txt d'un vector */
	public static String getTxt(Vector<Vector<?>> data) {
		return getTxt(data, ",");
	}

	/** renvoie le txt d'un vector */
	public static String readStream(InputStream input) {
		String data = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String line = "";

		try {
			while ((line = in.readLine()) != null) {
				data += (line + "\n");
			}
		} catch (IOException e) {
		}

		return data;
	}

	/** renvoie -1 si pas trouve ou l'index sinon */
	public static int findIndex(List<?> list, Same object) {
		Iterator<?> iter = list.iterator();
		int index = -1;

		for (int i = 0; (i < list.size()) && (index == -1); i++) {
			if (((Same) iter.next()).isSame(object)) {
				index = i;
			}
		}

		return index;
	}

	/** renvoie un panel avec un jtable orne de bouton et d'un libe */
	public static JPanel getPanelTable(String title, Action[] actions, JTable jTable) {
		JPanel main = new JPanel(new BorderLayout(5, 5));
		main.add(new JLabel(title), BorderLayout.NORTH);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel jPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));

		for (int i = 0; i < actions.length; i++) {
			if (actions[i] == null) {
				jPanelTop.add(new JLabel(" "));
			} else {
				JButton jbutton = new JButton(actions[i]);
				jPanelTop.add(jbutton);

				if (jbutton.getIcon() != null) {
					jbutton.setText("");
				}

				jbutton.setBorder(BorderFactory.createEtchedBorder());
			}
		}

		JPanel jPanel2 = new JPanel(new BorderLayout(2, 2));
		jPanel2.add(jPanelTop, BorderLayout.NORTH);

		JScrollPane jScrollPane = new JScrollPane(jTable);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ToolTipManager.sharedInstance().unregisterComponent(jTable);
		ToolTipManager.sharedInstance().unregisterComponent(jTable.getTableHeader());
		jPanel2.add(jScrollPane, BorderLayout.WEST);
		main.add(jPanel2, BorderLayout.CENTER);

		return main;
	}

	/** enregistre un fichier texte en proposant une boite de dialogue */
	public static void enregFich(String texte) {
		JFileChooser fc;

		if (repertoire.equals("")) {
			fc = new JFileChooser();
		} else {
			fc = new JFileChooser(repertoire);
		}

		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			boolean isok = true;

			if (fc.getSelectedFile().exists()) {
				isok = false;

				if (JOptionPane.showConfirmDialog(null,
						Messages.getString("Tools.Do_you_really_want_to_overwrite_the_existing_file"),
						Messages.getString("Tools.Confirmation_Dialog"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					isok = true;
				}
			}

			if (isok) {
				String f = fc.getSelectedFile().getAbsolutePath();

				if (f.endsWith("html") || f.endsWith("htm")) {
				} else {
					f += ".html";
				}

				try {
					FileWriter fw = new FileWriter(f);
					fw.write(texte);
					fw.flush();
					fw.close();
				} catch (IOException ex) {
				}
			}
		}

		repertoire = fc.getCurrentDirectory().getAbsolutePath();
	}
}
