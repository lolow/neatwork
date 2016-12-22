package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * boite de dialogue des statistiques d'un topo
 * @author L. DROUET
 * @version 1.0
 */
public class TopoStatDialog extends JDialog implements ActionListener {
    public TopoStatDialog(Topographie topo, JFrame frame) {
        super(frame,
            Messages.getString("TopoStatDialog.Topography_information"), true); 
        setSize(400, 300);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        JLabel main = new JLabel();
        main.setText(Messages.getString(
                "TopoStatDialog.<HTML>_<blockquote><B>Topography_name__</B>") + 
            topo.getName() + " <BR><BR>" +
            Messages.getString("TopoStatDialog.<B>Number_of_Nodes__</B>") +  
            topo.getNbNodes() + "<UL>" +
            Messages.getString("TopoStatDialog.<LI>_<i>Branching_nodes__</i>") +  
            topo.getNbNodes(Node.TYPE_DISPATCH) +
            Messages.getString("TopoStatDialog.<LI>_<i>Faucet_nodes__</i>") + 
            topo.getNbNodes(Node.TYPE_FAUCET) +
            Messages.getString("TopoStatDialog._(with") + 
            topo.getNbTotalTaps() +
            Messages.getString("TopoStatDialog._individual_faucets)") +
            "</UL>" +  
            Messages.getString("TopoStatDialog.<B>Total_height_change__</B>") + 
            Tools.doubleFormat("#", topo.getTotalHeightChange()) + 
            " m<BR><BR>" +
            Messages.getString("TopoStatDialog.<B>Number_of_Pipes__</B>") +
            topo.getNbPipes() +  
            " <BR>" +
            Messages.getString("TopoStatDialog.<B>Total_length__</B>") +  
            Tools.doubleFormat("#", topo.getTotalLength()) + " m <BR>");  
        pane.add(new JScrollPane(main), BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString("TopoStatDialog.Close")); 
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
