package neatwork.gui.tree;

import neatwork.project.Network;
import neatwork.project.Node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * renderer de la table tree
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class TreeCellRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = -7469726100044419820L;
	public static Hashtable color;
	private Network network;
	private int type = -1;
	private Map nodePath;
	private boolean isPath;

	public TreeCellRenderer(Network network, Map nodePath) {
		super("");
		this.network = network;
		setBackground(Color.white);
		setForeground(Color.black);
		setHorizontalAlignment(SwingConstants.CENTER);
		this.nodePath = nodePath;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setText(value.toString());
		type = -1;

		if ((!getText().equals("")) && (!getText().startsWith("_"))) {
			type = network.getNode(getText()).getType();
		}

		Object z = nodePath.get(value.toString());
		isPath = (z != null) && (((Integer) z).intValue() > 0);

		return this;
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// dessin de
		if (isPath) {
			g2.setColor(new Color(24, 216, 249));
			g2.fillOval(0, 0, getWidth(), getHeight());
		}

		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.black);

		if ((getText().equals("_*I")) || (getText().equals("_*T"))) {

			Line2D l = new Line2D.Double(getWidth() / 2, 0, getWidth() / 2, getHeight());
			g2.draw(l);
		}

		if ((getText().equals("_*L")) || (getText().equals("_*T"))) {

			RoundRectangle2D l = new RoundRectangle2D.Double(getWidth() / 2.0, -getHeight() / 2.0, getWidth() * 1.0,
					getHeight() * 1.0, 10.0, 10.0);
			g2.draw(l);
		}

		if (type > -1) {
			if (type == Node.TYPE_RESERVOIR) {
				Line2D l = new Line2D.Double(getWidth() / 2, getHeight() / 2, getWidth(), getHeight() / 2);
				g2.draw(l);
			}

			if (type == Node.TYPE_DISPATCH) {
				Line2D l = new Line2D.Double(0, getHeight() / 2, getWidth(), getHeight() / 2);
				g2.draw(l);
			}

			if (type == Node.TYPE_FAUCET) {
				Line2D l = new Line2D.Double(0, getHeight() / 2, getWidth() / 2, getHeight() / 2);
				g2.draw(l);
			}

			g2.setColor((Color) color.get(new Integer(type)));
			g2.setStroke(new BasicStroke(1));

			FontMetrics fm = g2.getFontMetrics();
			int w = fm.stringWidth(getText()) + 10;

			// int h = fm.getHeight() + 4;
			int h = getHeight() - 2;
			int x = getWidth() / 2;
			int y = (getHeight() / 2) + 1;
			g2.fillRect(x - (w / 2), y - (h / 2), w, h);
			g2.setColor(Color.black);
			g2.drawRect(x - (w / 2), y - (h / 2), w - 1, h - 1);
			g2.drawString(getText(), x - ((w - 10) / 2), ((y - ((h - 4) / 2)) + fm.getAscent()) - 3);
		}

		// super.paint(g);
	}
}
