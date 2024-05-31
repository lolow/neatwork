package neatwork.utils;

import java.awt.*;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * horizontal progress bar dans tableau
 * @author L. DROUET
 * @version 1.0
 */
public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
	private static final long serialVersionUID = -1351372939724673847L;
	private Hashtable limitColors;
	private int[] limitValues;

	public ProgressCellRenderer() {
		super(JProgressBar.HORIZONTAL, 0, 100);
		setBorderPainted(false);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		int n = 0;
		this.setStringPainted(true);

		if (!(value instanceof Number)) {
			String str;

			if (value instanceof String) {
				str = (String) value;
				this.setStringPainted(false);
			} else {
				str = value.toString();
			}

			try {
				n = Integer.valueOf(str).intValue();
			} catch (NumberFormatException ex) {
			}
		} else {
			n = ((Number) value).intValue();
		}

		Color color = getColor(n);

		if (color != null) {
			setForeground(color);
		}

		setValue(n);

		return this;
	}

	public void setLimits(Hashtable limitColors) {
		this.limitColors = limitColors;

		int i = 0;
		int n = limitColors.size();
		limitValues = new int[n];

		Enumeration e = limitColors.keys();

		while (e.hasMoreElements()) {
			limitValues[i++] = ((Integer) e.nextElement()).intValue();
		}

		sort(limitValues);
	}

	private Color getColor(int value) {
		Color color = null;

		if (limitValues != null) {
			int i;

			for (i = 0; i < limitValues.length; i++) {
				if (limitValues[i] < value) {
					color = (Color) limitColors.get(new Integer(limitValues[i]));
				}
			}
		}

		return color;
	}

	private void sort(int[] a) {
		int n = a.length;

		for (int i = 0; i < (n - 1); i++) {
			int k = i;

			for (int j = i + 1; j < n; j++) {
				if (a[j] < a[k]) {
					k = j;
				}
			}

			int tmp = a[i];
			a[i] = a[k];
			a[k] = tmp;
		}
	}
}
