package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * Panel qui contient les param\u00E8tres d'un Make design
 * @author L. DROUET
 * @version 1.0
 */
public class ParametersPane extends JPanel implements Observer, ActionListener {
    //private ParametersTableModel tableModel;
    //private FancyTable table;
    private Topographie topo;
    private JTextField[] textfield;

    public ParametersPane(Topographie topo0) {
        this.topo = topo0;
        topo.addObserver(this);

        //fields
        DoubleInputVerifier dblInput = new DoubleInputVerifier();
        IntegerInputVerifier intInput = new IntegerInputVerifier();
        textfield = new JTextField[8];

        for (int i = 0; i < textfield.length; i++) {
            textfield[i] = new JTextField(10);
            textfield[i].setInputVerifier(dblInput);
        }

        JPanel fieldPane = new JPanel(new GridLayout(0, 2, 2, 2));
        fieldPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        Properties prop = topo.getProperties();
        fieldPane.add(new JLabel(Messages.getString(
                    "ParametersPane.<html><b>-_Make_Design_Parameters</b>"))); 
        fieldPane.add(new JLabel("")); 

        //0: Fraction of open taps
        fieldPane.add(new JLabel(Messages.getString("topo.opentaps.name") + ":"));  
        fieldPane.add(textfield[0]);

        //1: Service Quality
        fieldPane.add(new JLabel(Messages.getString("topo.servicequal.name") + 
                ":")); 
        fieldPane.add(textfield[1]);

        //2: Target Flow
        fieldPane.add(new JLabel(Messages.getString("topo.targetflow.name") + 
                ":")); 
        fieldPane.add(textfield[2]);

        //3: Limit on budget
        fieldPane.add(new JLabel(Messages.getString("topo.limitbudget.name") + 
                ":")); 
        fieldPane.add(textfield[3]);
        fieldPane.add(new JLabel(Messages.getString(
                    "ParametersPane.<html><b>-_Physical_Parameters</b>"))); 
        fieldPane.add(new JLabel("")); 

        //4: Water Temperature
        fieldPane.add(new JLabel(Messages.getString("topo.watertemp.name") + ":"));  

        JPanel pann = new JPanel(new BorderLayout());

        //ajout d'un bouton pour entrer en Celsius
        //JButton celsius = new JButton("Celsius temp.");
        //celsius.addActionListener( new ActionListener() {
        //  public void actionPerformed(ActionEvent e) {
        //    String rep = JOptionPane.showInputDialog(null,
        //           "Enter a temperature in Fahren. Neatwork will convert this value in Fahrenheit",
        //           "Celsius conversion",
        //           JOptionPane.PLAIN_MESSAGE);
        //    if (rep!=null)
        //    try {
        //      double val = Double.parseDouble(rep);
        //      val *= 1.8;
        //      val += 32;
        //      textfield[4].setText(Tools.doubleFormat("0.##",val));
        //    } catch (NumberFormatException ex) {}
        //  }
        //});
        pann.add(textfield[4], BorderLayout.CENTER);

        //pann.add(celsius, BorderLayout.EAST);
        fieldPane.add(pann);

        //5: Pipes commercial length
        fieldPane.add(new JLabel(Messages.getString("topo.pipelength.name") + 
                ":")); 
        fieldPane.add(textfield[5]);
        fieldPane.add(new JLabel(Messages.getString(
                    "ParametersPane.<html><b>-_Advanced_Parameters</b>"))); 
        fieldPane.add(new JLabel("")); 

        //6: Orifice coeff
        fieldPane.add(new JLabel(Messages.getString("topo.orifcoef.name") + ":"));  
        fieldPane.add(textfield[6]);

        //7: Pipes coeff
        fieldPane.add(new JLabel(Messages.getString("topo.faucetcoef.name") + 
                ":")); 
        fieldPane.add(textfield[7]);

        //top
        JPanel paneltop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Icon icon0 = new ImageIcon(getClass().getResource("/neatwork/gui/images/Undo.png")); 
        JButton button0 = new JButton(Messages.getString("ParametersPane.Reset"),
                icon0); 
        button0.setActionCommand("reset"); 
        button0.addActionListener(this);
        paneltop.add(button0);

        Icon icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/Check.gif")); 
        JButton button = new JButton(Messages.getString("ParametersPane.Apply"),
                icon); 
        button.setActionCommand("apply"); 
        button.addActionListener(this);
        paneltop.add(button);

        //final
        setLayout(new BorderLayout(5, 5));
        add(new JScrollPane(fieldPane), BorderLayout.CENTER);
        add(paneltop, BorderLayout.NORTH);

        loadValue();
    }

    private void loadValue() {
        //0: Fraction of open taps
        textfield[0].setText(topo.getProperties().getProperty("topo.opentaps.value")); 

        //1: Service Quality
        textfield[1].setText(topo.getProperties().getProperty("topo.servicequal.value")); 

        //2: Target Flow
        textfield[2].setText(topo.getProperties().getProperty("topo.targetflow.value")); 

        //3: Limit on budget
        textfield[3].setText(topo.getProperties().getProperty("topo.limitbudget.value")); 

        //4: Water Temperature
        textfield[4].setText(topo.getProperties().getProperty("topo.watertemp.value")); 

        //5: Pipes commercial length
        textfield[5].setText(topo.getProperties().getProperty("topo.pipelength.value")); 

        //6: Orifice coeff
        textfield[6].setText(topo.getProperties().getProperty("topo.orifcoef.value")); 

        //7: Pipes coeff
        textfield[7].setText(topo.getProperties().getProperty("topo.faucetcoef.value")); 
    }

    private void saveValue() {
        Properties prop = new Properties();

        //0: Fraction of open taps
        try {
            prop.setProperty("topo.opentaps.value", 
                "" + Double.parseDouble(textfield[0].getText())); 
        } catch (NumberFormatException e) {
        }

        //1: Service Quality
        try {
            prop.setProperty("topo.servicequal.value", 
                "" + Double.parseDouble(textfield[1].getText())); 
        } catch (NumberFormatException e) {
        }

        //2: Target Flow
        try {
            prop.setProperty("topo.targetflow.value", 
                "" + Double.parseDouble(textfield[2].getText())); 
        } catch (NumberFormatException e) {
        }

        //3: Limit on budget
        try {
            prop.setProperty("topo.limitbudget.value", 
                "" + Double.parseDouble(textfield[3].getText())); 
        } catch (NumberFormatException e) {
        }

        //4: Water Temperature
        try {
            prop.setProperty("topo.watertemp.value", 
                "" + Double.parseDouble(textfield[4].getText())); 
        } catch (NumberFormatException e) {
        }

        //5: Pipes commercial length
        try {
            prop.setProperty("topo.pipelength.value", 
                "" + Double.parseDouble(textfield[5].getText())); 
        } catch (NumberFormatException e) {
        }

        //6: Orifice coeff
        try {
            prop.setProperty("topo.orifcoef.value", 
                "" + Double.parseDouble(textfield[6].getText())); 
        } catch (NumberFormatException e) {
        }

        //7: Pipes coeff
        try {
            prop.setProperty("topo.faucetcoef.value", 
                "" + Double.parseDouble(textfield[7].getText())); 
        } catch (NumberFormatException e) {
        }

        topo.setProperties(prop);
    }

    private Properties getProperties(String data) {
        saveValue();

        return topo.getProperties();
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(topo)) {
            switch (((Integer) param).intValue()) {
            case Project.MODIF_PROPERTIES:
                loadValue();

                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("apply")) { 
            saveValue();
        }

        if (e.getActionCommand().equals("reset")) { 
            loadValue();
        }
    }
}
