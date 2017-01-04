package neatwork.gui.fancytable;

import java.awt.*;

import javax.swing.*;


/**
 * Panel qui contient un table et des boutons associes
 * @author L. DROUET
 * @version 1.0
 */
public class FancyTablePanel extends JPanel {
    public FancyTablePanel(String title, Action[] actions, JTable jTable) {
        this(title, actions, jTable, false);
    }

    public FancyTablePanel(String title, Action[] actions, JTable jTable,
        boolean wholeWidth) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //label
        if (!title.equals("")) { 
            add(new JLabel("<html>" + title), BorderLayout.NORTH); 
        }

        //button
        JPanel jPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        int nbActions = (actions == null) ? 0 : actions.length;

        for (int i = 0; i < nbActions; i++) {
            if (actions[i] == null) {
                jPanelTop.add(new JLabel(" ")); 
            } else {
                JButton jbutton = new JButton(actions[i]);
                jPanelTop.add(jbutton);

                if (jbutton.getIcon() != null) {
                    jbutton.setText(""); 
                }

                jbutton.setBorder(BorderFactory.createEtchedBorder());
            }
        }

        //jTable
        ToolTipManager.sharedInstance().unregisterComponent(jTable);
        ToolTipManager.sharedInstance().unregisterComponent(jTable.getTableHeader());

        JPanel jPanel2 = new JPanel(new BorderLayout(2, 2));
        jPanel2.add(jPanelTop, BorderLayout.NORTH);

        JScrollPane jScrollPane = new JScrollPane(jTable);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setMaximumSize(jTable.getPreferredScrollableViewportSize());

        if (wholeWidth) {
            jTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            jPanel2.add(jScrollPane, BorderLayout.CENTER);
        } else {
            jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            Box box = Box.createHorizontalBox();
            box.add(jScrollPane);
            box.add(Box.createHorizontalGlue());
            jPanel2.add(box, BorderLayout.CENTER);
        }

        add(jPanel2, BorderLayout.CENTER);
    }
}
