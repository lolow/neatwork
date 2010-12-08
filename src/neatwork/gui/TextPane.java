package neatwork.gui;

import neatwork.Messages;

import neatwork.project.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;


/**
 * Classe qui definit un panel avec le texte comme \u00E9diteur
 * @author L. DROUET
 * @version 1.0
 */
public class TextPane extends JPanel implements ActionListener, Observer {
    private Project project;

    //composants
    private JTextArea textArea = new JTextArea();
    private JButton button;
    private JButton button2;
    private JPanel jPanel = new JPanel();

    public TextPane(Project project) {
        this.project = project;
        project.addObserver(this);
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(jPanel, BorderLayout.SOUTH);
        button = new JButton(Messages.getString("TextPane.Apply"),
                new ImageIcon(getClass().getResource("/neatwork/gui/images/Check.gif"))); //$NON-NLS-1$ //$NON-NLS-2$
        button.setActionCommand(Messages.getString("TextPane.Apply")); //$NON-NLS-1$
        button2 = new JButton(Messages.getString("TextPane.Undo"),
                new ImageIcon(getClass().getResource("/neatwork/gui/images/Undo.png"))); //$NON-NLS-1$ //$NON-NLS-2$
        button2.setActionCommand(Messages.getString("TextPane.Reset")); //$NON-NLS-1$
        jPanel.add(button2);
        jPanel.add(button);
        button2.addActionListener(this);
        button.addActionListener(this);
        updateTextArea();
    }

    private void updateTextArea() {
        textArea.setText(project.getContent());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Messages.getString("TextPane.Apply"))) { //$NON-NLS-1$
            project.freeInfoModif();
            project.setContent(textArea.getText());

            if (!project.getInfoModif().equals("")) { //$NON-NLS-1$
                JOptionPane.showMessageDialog(this,
                    Messages.getString("TextPane.error_in_your_project")); //$NON-NLS-1$
            }

            if (project.getInfoModif().indexOf(Messages.getString(
                            "TextPane.error")) != -1) { //$NON-NLS-1$
                updateTextArea();
            }
        } else if (e.getActionCommand().equals(Messages.getString(
                        "TextPane.Reset"))) { //$NON-NLS-1$
            updateTextArea();
        }
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(project)) {
            switch (((Integer) param).intValue()) {
            case Project.MODIF_CONTENT:
                updateTextArea();

                break;
            }
        }
    }
}
