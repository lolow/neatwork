package neatwork.gui.design;

import neatwork.Messages;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * boite de dialogue des statistiques d'un design
 * @author L. DROUET
 * @version 1.0
 */
public class DesignStatDialog extends JDialog implements ActionListener {
    public DesignStatDialog(Design design, JFrame frame) {
        super(frame, Messages.getString("DesignStatDialog.Design_information"),
            true); //$NON-NLS-1$
        setSize(400, 300);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        JLabel main = new JLabel();
        main.setText(Messages.getString(
                "DesignStatDialog.<HTML>_<blockquote><B>Design_name__</B>") + //$NON-NLS-1$
            design.getName() + " <BR><BR>" +
            Messages.getString("DesignStatDialog.<B>Number_of_Nodes__</B>") + //$NON-NLS-1$ //$NON-NLS-2$
            design.getNbNodes() + "<UL>" +
            Messages.getString("DesignStatDialog.<LI>_<i>Branching_nodes__</i>") + //$NON-NLS-1$ //$NON-NLS-2$
            design.getNbNodes(Node.TYPE_DISPATCH) +
            Messages.getString("DesignStatDialog.<LI>_<i>Faucet_nodes__</i>") +
            design.getNbNodes(Node.TYPE_FAUCET) + //$NON-NLS-1$
            "</UL>" +
            Messages.getString("DesignStatDialog.<B>Total_height_change__</B>") + //$NON-NLS-1$ //$NON-NLS-2$
            Tools.doubleFormat("#", design.getTotalHeightChange()) + //$NON-NLS-1$
            " m<BR><BR>" +
            Messages.getString("DesignStatDialog.<B>Number_of_Pipes__</B>") +
            design.getNbPipes() + //$NON-NLS-1$ //$NON-NLS-2$
            " <BR>" +
            Messages.getString("DesignStatDialog.<B>Total_length__</B>") + //$NON-NLS-1$ //$NON-NLS-2$
            Tools.doubleFormat("#", design.getTotalLength()) + " m <BR>"); //$NON-NLS-1$ //$NON-NLS-2$
        pane.add(new JScrollPane(main), BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString(
                    "DesignStatDialog.Close")); //$NON-NLS-1$
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
