package neatwork.gui.tree;

import neatwork.Messages;

import neatwork.project.Network;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * JPanel pour l'arbre avec recherche
 * @author L. DROUET
 * @version 1.0
 */
public class TreePane extends JPanel implements ActionListener {
    private Network network;
    private JTextField textField;
    private TreeNetwork treeNetwork;

    public TreePane(Network network) {
        //composant de recherche
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(Messages.getString("TreePane.Node_Search")),
            BorderLayout.WEST); //$NON-NLS-1$
        textField = new JTextField();
        textField.setActionCommand("go"); //$NON-NLS-1$
        textField.addActionListener(this);
        panel.add(textField, BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString("TreePane.Go")); //$NON-NLS-1$
        button.setActionCommand("go"); //$NON-NLS-1$
        button.addActionListener(this);
        panel.add(button, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(panel, BorderLayout.NORTH);
        treeNetwork = new TreeNetwork(network);
        add(new JScrollPane(treeNetwork));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) { //$NON-NLS-1$

            if (textField.getText().length() > 0) {
                treeNetwork.search(textField.getText());
            }
        }
    }
}
