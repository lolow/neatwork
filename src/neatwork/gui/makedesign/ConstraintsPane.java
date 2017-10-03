package neatwork.gui.makedesign;

import neatwork.Messages;

import neatwork.gui.design.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;


/**
 * JPanel des contraintes sur les tuyaux
 * @author L. DROUET
 * @version 1.0
 */
public class ConstraintsPane extends JPanel implements ActionListener {
    private JComboBox comboPipe;
    private JComboBox comboConstraint;
    private JButton buttonAddConstraint;
    private JButton buttonDeleteConstraint;
    private JList listConstraint;
    private JTextField textFieldGreaterThan = new JTextField(10);
    private JTextField textFieldLowerThan = new JTextField(10);
    private ComboDatabase comboDiameter0;
    private ComboDatabase comboDiameter1;
    private ComboDatabase comboDiameter2;
    private JTextField textFieldLength1 = new JTextField(10);
    private CardLayout cardLayout;
    private JPanel ypanel;
    private Vector constraints;

    public ConstraintsPane(Topographie topo, Database database) {
        constraints = new Vector();

        DoubleInputVerifier dblInput = new DoubleInputVerifier();
        textFieldGreaterThan.setInputVerifier(dblInput);
        textFieldLength1.setInputVerifier(dblInput);
        textFieldLowerThan.setInputVerifier(dblInput);

        //panel top
        Vector pipelist = new Vector();
        Iterator iter = topo.getExpandedPipeIterator();

        while (iter.hasNext())
            pipelist.add(iter.next());

        comboPipe = new JComboBox(pipelist);

        Vector typeConstraint = new Vector();
        typeConstraint.add(Messages.getString(
                "ConstraintsPane.pipe(s)_must_be_greater_than")); //0 
        typeConstraint.add(Messages.getString(
                "ConstraintsPane.pipe(s)_must_be_lower_than")); //1 
        typeConstraint.add(Messages.getString(
                "ConstraintsPane.pipe(s)_must_be_equal_to")); //2 
        typeConstraint.add(Messages.getString(
                "ConstraintsPane.the_two_pipes_must_be")); //3 
        comboConstraint = new JComboBox(typeConstraint);

        comboDiameter0 = new ComboDatabase(database);
        comboDiameter1 = new ComboDatabase(database);
        comboDiameter2 = new ComboDatabase(database);

        JPanel paneltop = new JPanel(new BorderLayout(5, 5));
        paneltop.setBorder(BorderFactory.createEtchedBorder());

        JPanel zpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        zpanel.add(new JLabel(Messages.getString(
                    "ConstraintsPane.Constraint_on"))); 
        zpanel.add(comboPipe);
        zpanel.add(new JLabel(",")); 
        zpanel.add(comboConstraint);
        paneltop.add(zpanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        ypanel = new JPanel(cardLayout);

        JPanel p0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p0.add(new JLabel(Messages.getString("ConstraintsPane.diameter"))); 
        p0.add(textFieldGreaterThan);
        ypanel.add(p0, "0"); 

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel(Messages.getString("ConstraintsPane.diameter"))); 
        p1.add(textFieldLowerThan);
        ypanel.add(p1, "1"); 

        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel(Messages.getString("ConstraintsPane.diameter"))); 
        p2.add(comboDiameter0);
        ypanel.add(p2, "2"); 

        JPanel p3 = new JPanel(new GridLayout(2, 1));
        JPanel p31 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p31.add(new JLabel(Messages.getString("ConstraintsPane.diameter_1"))); 
        p31.add(comboDiameter1);
        p31.add(new JLabel(Messages.getString("ConstraintsPane.length_1"))); 
        p31.add(textFieldLength1);
        p3.add(p31);

        JPanel p32 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p32.add(new JLabel(Messages.getString("ConstraintsPane.diameter_2"))); 
        p32.add(comboDiameter2);
        p32.add(new JLabel(Messages.getString("ConstraintsPane.length_2"))); 
        p32.add(new JLabel());
        p3.add(p32);
        ypanel.add(p3, "3"); 
        paneltop.add(ypanel, BorderLayout.CENTER);

        JPanel wpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonAddConstraint = new JButton(Messages.getString(
                    "ConstraintsPane.Add_new_constraint")); 
        buttonAddConstraint.addActionListener(this);
        buttonDeleteConstraint = new JButton(Messages.getString(
                    "ConstraintsPane.Delete")); 
        buttonDeleteConstraint.addActionListener(this);
        wpanel.add(buttonAddConstraint);
        wpanel.add(buttonDeleteConstraint);

        JPanel xpanel = new JPanel(new BorderLayout());
        xpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        xpanel.add(wpanel, BorderLayout.NORTH);
        listConstraint = new JList(new ConstraintsListModel(constraints));
        xpanel.add(new JScrollPane(listConstraint), BorderLayout.CENTER);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(paneltop, BorderLayout.NORTH);
        add(xpanel, BorderLayout.CENTER);
        cardLayout.show(ypanel, "" + comboConstraint.getSelectedIndex()); 
        comboConstraint.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        //combo constraint
        if (e.getSource().equals(comboConstraint)) {
            cardLayout.show(ypanel, "" + comboConstraint.getSelectedIndex()); 
        }

        if (e.getSource().equals(buttonDeleteConstraint)) {
            if (listConstraint.getSelectedIndex() > -1) {
                constraints.remove(listConstraint.getSelectedIndex());
                ((ConstraintsListModel) listConstraint.getModel()).removeConstraints();
            }
        }

        if (e.getSource().equals(buttonAddConstraint)) {
            Vector line = new Vector();
            line.add(new Integer(comboConstraint.getSelectedIndex()));
            line.add(new Pipe(comboPipe.getSelectedItem().toString()));

            switch (comboConstraint.getSelectedIndex()) {
            case 0:
                line.add(textFieldGreaterThan.getText());

                break;

            case 1:
                line.add(textFieldLowerThan.getText());

                break;

            case 2:
                line.add(comboDiameter0.getSelectedDiameter());

                break;

            case 3:
                line.add(comboDiameter1.getSelectedDiameter());
                line.add(textFieldLength1.getText());
                line.add(comboDiameter2.getSelectedDiameter());

                break;
            }

            constraints.add(line);
            ((ConstraintsListModel) listConstraint.getModel()).addConstraints();
        }
    }

    public Vector getContraints() {
        return constraints;
    }
}
