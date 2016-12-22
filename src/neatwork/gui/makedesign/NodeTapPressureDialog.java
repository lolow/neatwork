package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * boite de dialogue qui donne les infos de tap et pressure a la suite d'un
 * makedesign.
 * @author L. DROUET
 * @version 1.0
 */
public class NodeTapPressureDialog extends JDialog {
    public NodeTapPressureDialog(Vector data, JFrame frame) {
        super(frame,
            Messages.getString("NodeTapPressureDialog.Nodes_&_Faucets_Pressure"),
            true); 
        setSize(500, 300);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        NodeTapPressureModel model = new NodeTapPressureModel(data);
        FancyTable table = new FancyTable(model);

        Container container = getContentPane();
        container.setLayout(new BorderLayout(5, 5));
        container.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton button = new JButton(Messages.getString(
                    "NodeTapPressureDialog.Close")); 
        panel.add(button);

        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

        container.add(panel, BorderLayout.SOUTH);
    }
}
