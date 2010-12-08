package neatwork.gui.simu;

import neatwork.Messages;

import neatwork.file.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;


/**
 * Boite de dialogue pour lancer une nouvelle simulation
 * @author L. DROUET
 * @version 1.0
 */
public class NewSimuDialog extends JDialog implements ActionListener {
    //champs d'edition
    private JTextField nbSimField;

    //champs d'edition
    private JTextField openTapsField;

    //champs d'edition
    private JTextField outFlowField;

    //champs d'edition
    private JTextField critFlowLowField;

    //champs d'edition
    private JTextField critFlowHighField;
    private JComboBox typeOrificeCombo;
    private JComboBox typeSimuCombo;
    private Properties prop;
    private Design design;
    private Hashtable table = new Hashtable();
    public boolean runSimu = false;

    public NewSimuDialog(Design design, AbstractFileManager fileManager,
        Properties defProp) {
        setTitle(Messages.getString("NewSimuDialog.Simulation_parameters")); //$NON-NLS-1$
        setModal(true);
        setSize(400, 350);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.design = design;
        prop = new Properties(defProp);
        prop.putAll(design.getProperties());

        //main panel
        //fields
        DoubleInputVerifier dblInput = new DoubleInputVerifier();
        IntegerInputVerifier intInput = new IntegerInputVerifier();
        nbSimField = new JTextField(prop.getProperty("simu.nbsim.value"), 15); //$NON-NLS-1$
        nbSimField.setInputVerifier(intInput);
        openTapsField = new JTextField(prop.getProperty(
                    "simu.simopentaps.value"), 15); //$NON-NLS-1$
        openTapsField.setInputVerifier(dblInput);
        outFlowField = new JTextField(prop.getProperty("simu.targetflow.value"), //$NON-NLS-1$
                15);
        outFlowField.setInputVerifier(dblInput);
        critFlowLowField = new JTextField(prop.getProperty(
                    "simu.mincriticalflow.value"), 15); //$NON-NLS-1$
        critFlowLowField.setInputVerifier(dblInput);
        critFlowHighField = new JTextField(prop.getProperty(
                    "simu.maxcriticalflow.value"), 15); //$NON-NLS-1$
        critFlowHighField.setInputVerifier(dblInput);

        String[] listTypeOrifice = { Messages.getString("NewSimuDialog.ideal"), Messages.getString("NewSimuDialog.commercial") };  //$NON-NLS-1$ //$NON-NLS-2$
        typeOrificeCombo = new JComboBox(listTypeOrifice);

        if (prop.getProperty("simu.typeorifice.value").equals("commercial")) { //$NON-NLS-1$ //$NON-NLS-2$
            typeOrificeCombo.setSelectedIndex(1);
        } else {
            typeOrificeCombo.setSelectedIndex(0);
        }

        String[] listTypeSimu = { Messages.getString("NewSimuDialog.monte-carlo_sampling"), Messages.getString("NewSimuDialog.individual_faucets"), Messages.getString("NewSimuDialog.user-defined") };  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        typeSimuCombo = new JComboBox(listTypeSimu);

        if (prop.getProperty("simu.typesimu.value").equals("random")) { //$NON-NLS-1$ //$NON-NLS-2$
            typeSimuCombo.setSelectedIndex(0);
        } else if (prop.getProperty("simu.typesimu.value").equals("tapbytap")) { //$NON-NLS-1$ //$NON-NLS-2$
            typeSimuCombo.setSelectedIndex(1);
        } else {
            typeSimuCombo.setSelectedIndex(2);
        }

        //layout the fields
        JPanel fieldPane = new JPanel(new GridLayout(0, 2, 2, 2));
        fieldPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        fieldPane.add(new JLabel(Messages.getString("simu.nbsim.name") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
        fieldPane.add(nbSimField);
        fieldPane.add(new JLabel(Messages.getString("simu.simopentaps.name") + //$NON-NLS-1$
                ":")); //$NON-NLS-1$
        fieldPane.add(openTapsField);
        fieldPane.add(new JLabel(Messages.getString("NewSimuDialog.critical_flows"))); //$NON-NLS-1$
        fieldPane.add(new JLabel("")); //$NON-NLS-1$
        fieldPane.add(new JLabel("     " + //$NON-NLS-1$
		Messages.getString("simu.mincriticalflow.name") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
        fieldPane.add(critFlowLowField);
        fieldPane.add(new JLabel("     " + //$NON-NLS-1$
		Messages.getString("simu.maxcriticalflow.name") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
        fieldPane.add(critFlowHighField);
        fieldPane.add(new JLabel(Messages.getString("simu.targetflow.name") + //$NON-NLS-1$
                ":")); //$NON-NLS-1$
        fieldPane.add(outFlowField);
        fieldPane.add(new JLabel(Messages.getString("simu.typeorifice.name") + //$NON-NLS-1$
                ":")); //$NON-NLS-1$
        fieldPane.add(typeOrificeCombo);
        fieldPane.add(new JLabel(Messages.getString("simu.typesimu.name") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
        fieldPane.add(typeSimuCombo);

        //panel sud
        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runSimu = new JButton(Messages.getString(
                    "NewSimuDialog.Run_Simulation...")); //$NON-NLS-1$
        runSimu.setActionCommand("runSimu"); //$NON-NLS-1$
        runSimu.addActionListener(this);

        JButton close = new JButton(Messages.getString("NewSimuDialog.Close")); //$NON-NLS-1$
        close.setActionCommand("close"); //$NON-NLS-1$
        close.addActionListener(this);
        bottomPane.add(close);
        bottomPane.add(runSimu);

        //on mets tout
        JPanel container = (JPanel) getContentPane();
        container.setLayout(new BorderLayout(5, 5));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.add(bottomPane, BorderLayout.CENTER);
        container.add(fieldPane, BorderLayout.NORTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close")) { //$NON-NLS-1$
            dispose();
        }

        if (e.getActionCommand().equals("runSimu")) { //$NON-NLS-1$

            //tapbytap
            if (typeSimuCombo.getSelectedIndex() == 1) {
                int cpt = 0;
                Iterator iter = design.getNodeIterator();

                while (iter.hasNext()) {
                    Node item = (Node) iter.next();
                    cpt += ((item.getType() == Node.TYPE_FAUCET) ? 1 : 0);
                }

                nbSimField.setText("" + cpt); //$NON-NLS-1$
                openTapsField.setText("-"); //$NON-NLS-1$
            }

            //custom
            if (typeSimuCombo.getSelectedIndex() == 2) {
                nbSimField.setText("1"); //$NON-NLS-1$
                openTapsField.setText("-"); //$NON-NLS-1$

                TapSelectionDialog dialog = new TapSelectionDialog(design, this);
                dialog.setVisible(true);
                table = dialog.getFaucetRef();
            }

            runSimu = true;
            dispose();
        }
    }

    public Design getDesign() {
        return design;
    }

    public Properties getParameters() {
        Properties p = new Properties(prop);
        p.setProperty("simu.nbsim.value", nbSimField.getText()); //$NON-NLS-1$
        p.setProperty("simu.simopentaps.value", openTapsField.getText()); //$NON-NLS-1$
        p.setProperty("simu.targetflow.value", outFlowField.getText()); //$NON-NLS-1$
        p.setProperty("simu.mincriticalflow.value", critFlowLowField.getText()); //$NON-NLS-1$
        p.setProperty("simu.maxcriticalflow.value", critFlowHighField.getText()); //$NON-NLS-1$
        p.setProperty("simu.typeorifice.value", //$NON-NLS-1$
            typeOrificeCombo.getSelectedItem().toString());

        String value = ""; //$NON-NLS-1$

        switch (typeSimuCombo.getSelectedIndex()) {
        case 0:
            value = "random"; //$NON-NLS-1$

            break;

        case 1:
            value = "tapbytap"; //$NON-NLS-1$

            break;

        case 2:
            value = "handmade"; //$NON-NLS-1$

            break;
        }

        p.setProperty("simu.typesimu.value", value); //$NON-NLS-1$

        return p;
    }

    public Hashtable getFaucetRef() {
        return table;
    }
}
