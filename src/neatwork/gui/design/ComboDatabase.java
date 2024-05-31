package neatwork.gui.design;

import neatwork.project.*;

import java.awt.*;

import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

/**
 * jcombobox qui contient la base de donnee
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class ComboDatabase extends JComboBox {
	protected int popupWidth;
	private Database database;

	public ComboDatabase(Database database) {
		this.database = database;
		updateList();
		setUI(new SteppedComboBoxUI());
		popupWidth = 0;
	}

	public String getEnonce(Diameter diam) {
		return "diam " + diam.getDiameter() + ":sdr " + diam.getSdr() + ":(" + //$NON-NLS-3$
				Diameter.typeName[diam.getType()] + ")";
	}

	public Double getSelectedDiameter() {
		String s = getSelectedItem().toString();

		return new Double(s.substring(5, s.indexOf(":")));
	}

	private void updateList() {
		removeAllItems();

		Enumeration e = database.getDiameters();

		while (e.hasMoreElements()) {
			Diameter item = (Diameter) e.nextElement();
			addItem(getEnonce(item));
		}
	}

	public void setPopupWidth(int width) {
		popupWidth = width;
	}

	public Dimension getPopupSize() {
		Dimension size = getSize();

		if (popupWidth < 1) {
			popupWidth = size.width;
		}

		return new Dimension(popupWidth, size.height);
	}
}

class SteppedComboBoxUI extends MetalComboBoxUI {
	protected ComboPopup createPopup() {
		BasicComboPopup popup = new BasicComboPopup(comboBox) {
			public void show() {
				Dimension popupSize = ((ComboDatabase) comboBox).getPopupSize();
				popupSize.setSize(popupSize.width, getPopupHeightForRowCount(comboBox.getMaximumRowCount()));

				Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width,
						popupSize.height);
				scroller.setMaximumSize(popupBounds.getSize());
				scroller.setPreferredSize(popupBounds.getSize());
				scroller.setMinimumSize(popupBounds.getSize());
				list.invalidate();

				int selectedIndex = comboBox.getSelectedIndex();

				if (selectedIndex == -1) {
					list.clearSelection();
				} else {
					list.setSelectedIndex(selectedIndex);
				}

				list.ensureIndexIsVisible(list.getSelectedIndex());
				setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

				show(comboBox, popupBounds.x, popupBounds.y);
			}
		};

		popup.getAccessibleContext().setAccessibleParent(comboBox);

		return popup;
	}
}
