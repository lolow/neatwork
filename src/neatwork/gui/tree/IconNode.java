package neatwork.gui.tree;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Definition d'un noeud icone
 * @author L. DROUET
 * @version 1.0
 */
public class IconNode extends DefaultMutableTreeNode {
    protected Icon icon;

    public IconNode(Object userObject) {
        super(userObject);
        this.icon = null;
    }

    public IconNode(Object userObject, Icon icon) {
        super(userObject);
        this.icon = icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }
}
