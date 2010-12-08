package neatwork.gui.tree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * renderer d'un jtree avec des IconNode
 * @author L. DROUET
 * @version 1.0
 */
public class IconNodeRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);

        Icon icon = ((IconNode) value).getIcon();

        if (icon != null) {
            setIcon(icon);
        }

        return this;
    }
}
