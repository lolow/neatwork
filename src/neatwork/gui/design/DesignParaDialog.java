package neatwork.gui.design;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * boite de dialogue d'affichage des parametres d'un design
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class DesignParaDialog extends JDialog implements ActionListener {
	public DesignParaDialog(JFrame frame, Design design) {
		super(frame, Messages.getString("DesignParaDialog.Building_Parameters"), true);
		setSize(400, 400);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		FancyTableModel tableModel = new DesignParaModel(design);
		FancyTable table = new FancyTable(tableModel);

		Container pane = getContentPane();
		pane.setLayout(new BorderLayout(5, 5));

		pane.add(
				new FancyTablePanel(Messages.getString("DesignParaDialog.These_are_the_parameters_used_by")
						+ Messages.getString("DesignParaDialog.the_MAKE_DESIGN_operation_for_building")
						+ Messages.getString("DesignParaDialog._this_design"), null, table, true),
				BorderLayout.CENTER);

		JButton button = new JButton(Messages.getString("DesignParaDialog.Close"));
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(button);
		button.addActionListener(this);
		pane.add(panel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}
}
