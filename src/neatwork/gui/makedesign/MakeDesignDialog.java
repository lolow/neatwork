package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;


/**
 * Boite de dialogue pour construire un design
 * @author L. DROUET
 * @version 1.0
 */
public class MakeDesignDialog extends JDialog implements ActionListener {
    private ChooseMaterialPane materialPane;
    private ParametersPane parametersPane;
    private LoadFactorPane loadFactorPane;
    private ConstraintsPane constraintsPane;
    private Topographie topographie;
    private double hsource;
    private boolean canceled = true;

    public MakeDesignDialog(JFrame frame, Database database,
        Topographie topographie) {
        super(frame, Messages.getString("MakeDesignDialog.Make_Design"), true); 
        setSize(600, 400);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //calcul la topographie etendue
        this.topographie = topographie;
        hsource = topographie.setHauteurSource(0);
        topographie.makeExpandedTopo();

        materialPane = new ChooseMaterialPane(database, topographie);
        parametersPane = new ParametersPane(topographie);
        loadFactorPane = new LoadFactorPane(topographie);
        constraintsPane = new ConstraintsPane(topographie, database);

        Container container = getContentPane();
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(Messages.getString("MakeDesignDialog.Hardware"),
            materialPane); 
        tabbedPane.addTab(Messages.getString("MakeDesignDialog.Parameters"),
            parametersPane); 
        tabbedPane.addTab(Messages.getString("MakeDesignDialog.Constraints"),
            constraintsPane); 
        tabbedPane.addTab(Messages.getString("MakeDesignDialog.Load_Factors"),
            loadFactorPane); 

        container.setLayout(new BorderLayout(5, 5));
        container.add(tabbedPane, BorderLayout.CENTER);

        //boutons
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton button = new JButton(Messages.getString(
                    "MakeDesignDialog.Run_Make_Design")); 
        button.setActionCommand(Messages.getString(
                "MakeDesignDialog.Make_Design")); 
        button.addActionListener(this);
        button.setMnemonic('M');

        JButton button0 = new JButton(Messages.getString(
                    "MakeDesignDialog.Abort")); 
        button0.setActionCommand(Messages.getString("MakeDesignDialog.Close")); 
        button0.addActionListener(this);
        button0.setMnemonic('A');

        panelBottom.add(button0);
        panelBottom.add(button);

        container.add(panelBottom, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Messages.getString(
                        "MakeDesignDialog.Close"))) { 
            this.dispose();
        }

        if (e.getActionCommand().equals(Messages.getString(
                        "MakeDesignDialog.Make_Design"))) { 

            if (getDiametersContent().equals("")) { 
                JOptionPane.showMessageDialog(this,
                    Messages.getString(
                        "MakeDesignDialog.You_must_choose_at_least_one_diameter")); 
            } else {
                canceled = false;
                this.dispose();
            }
        }
    }

    public boolean getCanceled() {
        return canceled;
    }

    public Topographie getTopographie() {
        return topographie;
    }

    public String getOrificesContent() {
        return materialPane.getOrificesContent();
    }

    public String getDiametersContent() {
        return materialPane.getDiametersContent();
    }

    public Hashtable getLoadFactors() {
        return loadFactorPane.getLoadFactor();
    }

    public Vector getConstraints() {
        return constraintsPane.getContraints();
    }
    
    public double getHSource(){
    	return hsource;
    }
}
