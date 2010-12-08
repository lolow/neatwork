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
            Messages.getString("TopoStatDialog.Topography_information"), true); //$NON-NLS-1$
        setSize(400, 300);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        JLabel main = new JLabel();
        main.setText(Messages.getString(
                "TopoStatDialog.<HTML>_<blockquote><B>Topography_name__</B>") + //$NON-NLS-1$
            topo.getName() + " <BR><BR>" +
            Messages.getString("TopoStatDialog.<B>Number_of_Nodes__</B>") + //$NON-NLS-1$ //$NON-NLS-2$
            topo.getNbNodes() + "<UL>" +
            Messages.getString("TopoStatDialog.<LI>_<i>Branching_nodes__</i>") + //$NON-NLS-1$ //$NON-NLS-2$
            topo.getNbNodes(Node.TYPE_DISPATCH) +
            Messages.getString("TopoStatDialog.<LI>_<i>Faucet_nodes__</i>") + //$NON-NLS-1$
            topo.getNbNodes(Node.TYPE_FAUCET) +
            Messages.getString("TopoStatDialog._(with") + //$NON-NLS-1$
            topo.getNbTotalTaps() +
            Messages.getString("TopoStatDialog._individual_faucets)") +
            "</UL>" + //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("TopoStatDialog.<B>Total_height_change__</B>") + //$NON-NLS-1$
            Tools.doubleFormat("#", topo.getTotalHeightChange()) + //$NON-NLS-1$
            " m<BR><BR>" +
            Messages.getString("TopoStatDialog.<B>Number_of_Pipes__</B>") +
            topo.getNbPipes() + //$NON-NLS-1$ //$NON-NLS-2$
            " <BR>" +
            Messages.getString("TopoStatDialog.<B>Total_length__</B>") + //$NON-NLS-1$ //$NON-NLS-2$
            Tools.doubleFormat("#", topo.getTotalLength()) + " m <BR>"); //$NON-NLS-1$ //$NON-NLS-2$
        pane.add(new JScrollPane(main), BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString("TopoStatDialog.Close")); //$NON-NLS-1$
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
