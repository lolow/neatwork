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
	private static final long serialVersionUID = 245617727939983669L;
	private JTextField textField;
    private TreeNetwork treeNetwork;

    public TreePane(Network network) {
        //composant de recherche
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(Messages.getString("TreePane.Node_Search")),
            BorderLayout.WEST); 
        textField = new JTextField();
        textField.setActionCommand("go"); 
        textField.addActionListener(this);
        panel.add(textField, BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString("TreePane.Go")); 
        button.setActionCommand("go"); 
        button.addActionListener(this);
        panel.add(button, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(panel, BorderLayout.NORTH);
        treeNetwork = new TreeNetwork(network);
        add(new JScrollPane(treeNetwork));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) { 

            if (textField.getText().length() > 0) {
                treeNetwork.search(textField.getText());
            }
        }
    }
}
