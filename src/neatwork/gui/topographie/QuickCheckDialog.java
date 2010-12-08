package neatwork.gui.topographie;

import neatwork.Messages;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * boite de dialogue QuickCheck
 * @author L. DROUET
 * @version 1.0
 */
public class QuickCheckDialog extends JDialog implements ActionListener {
    private JButton apply = new JButton(Messages.getString(
                "QuickCheckDialog.Apply")); //$NON-NLS-1$
    private JTextField textSeuil;
    private JTextField textAlpha;
    private QuickCheckTableModel tableModel;
    Topographie topo;

    public QuickCheckDialog(JFrame frame, Topographie topo) {
        super(frame, Messages.getString("QuickCheckDialog.Quick_Check"), true); //$NON-NLS-1$
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 300);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);

        this.topo = topo;
        tableModel = new QuickCheckTableModel(topo);

        FancyTable table = new FancyTable(tableModel);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        JPanel top2 = new JPanel(new GridLayout(2, 1));
        JLabel lbl = new JLabel(Messages.getString("topo.seuil.name") + //$NON-NLS-1$
                " :"); //$NON-NLS-1$
        top2.add(lbl);

        JLabel lbl2 = new JLabel(Messages.getString("topo.faucetcoef.name") + //$NON-NLS-1$
                " :"); //$NON-NLS-1$
        top2.add(lbl2);
        top.add(top2, BorderLayout.WEST);

        apply.setActionCommand("apply"); //$NON-NLS-1$
        apply.addActionListener(this);
        top.add(apply, BorderLayout.EAST);

        JPanel top3 = new JPanel(new GridLayout(2, 1));
        textSeuil = new JTextField(topo.getProperties().getProperty("topo.seuil.value", //$NON-NLS-1$
                    "0.1")); //$NON-NLS-1$
        textSeuil.addActionListener(this);
        textSeuil.setActionCommand("apply"); //$NON-NLS-1$
        top3.add(textSeuil);
        textAlpha = new JTextField(topo.getProperties().getProperty("topo.faucetcoef.value", //$NON-NLS-1$
                    "0.1")); //$NON-NLS-1$
        textAlpha.addActionListener(this);
        textAlpha.setActionCommand("apply"); //$NON-NLS-1$
        top3.add(textAlpha);
        top.add(top3, BorderLayout.CENTER);

        pane.add(top, BorderLayout.NORTH);
        pane.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton button = new JButton(Messages.getString(
                    "QuickCheckDialog.Close")); //$NON-NLS-1$
        button.setActionCommand("close"); //$NON-NLS-1$

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(button);
        button.addActionListener(this);
        pane.add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close")) { //$NON-NLS-1$
            this.dispose();
        }

        if (e.getActionCommand().equals("apply")) { //$NON-NLS-1$

            try {
                double seuil = Double.parseDouble(textSeuil.getText());
                Properties p = new Properties();
                p.setProperty("topo.seuil.value", textSeuil.getText()); //$NON-NLS-1$
                topo.setProperties(p);
                tableModel.updateData();
            } catch (NumberFormatException ex) {
            }

            try {
                double alpha = Double.parseDouble(textAlpha.getText());
                Properties p = new Properties();
                p.setProperty("topo.faucetcoef.value", textAlpha.getText()); //$NON-NLS-1$
                topo.setProperties(p);
                tableModel.updateData();
            } catch (NumberFormatException ex) {
            }
        }
    }
}
