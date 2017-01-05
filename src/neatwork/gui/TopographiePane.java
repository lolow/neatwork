package neatwork.gui;

import neatwork.Messages;

import neatwork.gui.topographie.*;

import neatwork.gui.tree.*;

import neatwork.project.*;

import java.awt.*;

import javax.swing.*;

/**
 * Panel d'affichage d'une topographie
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class TopographiePane extends JPanel {
	Topographie topo;
	JTabbedPane jTabbedPane = new JTabbedPane();

	public TopographiePane(Topographie topo) {
		this.topo = topo;
		setLayout(new BorderLayout());
		jTabbedPane.setTabPlacement(JTabbedPane.TOP);

		// topotable pane
		jTabbedPane.addTab(Messages.getString("TopographiePane.Tables"), new TopoTablePane(topo));

		// tree pane
		jTabbedPane.addTab(Messages.getString("TopographiePane.Tree_View"), new TreePane(topo));

		// text pane
		jTabbedPane.addTab(Messages.getString("TopographiePane.Text"), new TextPane(topo));

		add(jTabbedPane, BorderLayout.CENTER);
	}
}
