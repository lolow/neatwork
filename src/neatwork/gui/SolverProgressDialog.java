package neatwork.gui;

import neatwork.Messages;

import neatwork.solver.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * Boite de dialogue qui montre la progression de r\u00E9solution
 * @author L. DROUET
 * @version 1.0
 */
public class SolverProgressDialog extends JDialog implements Observer,
    ActionListener {
    private AbstractSolver solver;
    private JLabel labelStatus = new JLabel(""); 
    private JProgressBar progressBar = new JProgressBar(0, 100);
    private JButton buttonClose;

    public SolverProgressDialog(AbstractSolver solver, Thread thread) {
        setTitle(Messages.getString("SolverProgressDialog.In_Progress")); 
        setModal(true);
        setSize(400, 150);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.solver = solver;
        solver.addObserver(this);

        Container container = getContentPane();
        container.setLayout(new BorderLayout(5, 5));

        JPanel panelT = new JPanel(new BorderLayout(5, 5));
        panelT.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelT.add(progressBar, BorderLayout.SOUTH);
        progressBar.setStringPainted(true);

        panelT.add(labelStatus, BorderLayout.NORTH);

        JPanel panelB = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelB.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5));
        buttonClose = new JButton(Messages.getString(
                    "SolverProgressDialog.Close")); 
        buttonClose.setMnemonic('C');
        buttonClose.setEnabled(false);
        panelB.add(buttonClose);

        container.add(panelT, BorderLayout.NORTH);
        container.add(panelB, BorderLayout.SOUTH);

        buttonClose.addActionListener(this);

        thread.start();
    }

    private void setProgress() {
        progressBar.setValue(solver.getProgress());
        buttonClose.setEnabled(solver.getProgress() == 100);
    }

    private void setStatus() {
        labelStatus.setText(solver.getStatut());
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(solver)) {
            switch (((Integer) param).intValue()) {
            case AbstractSolver.MODIF_PROGRESS:

                //do change
                setProgress();

                break;

            case AbstractSolver.MODIF_STATUS:

                //do change
                setStatus();

                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
