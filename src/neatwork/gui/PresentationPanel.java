package neatwork.gui;

import neatwork.Messages;

import java.awt.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 * Page de presentation
 * @author L. DROUET
 * @version 1.0
 */
public class PresentationPanel extends JPanel {
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel jLabel1 = new JLabel();
    TitledBorder titledBorder1;
    JLabel jLabel2 = new JLabel();
    Border border1;
    Properties prop;

    public PresentationPanel(Properties properties) {
        this.prop = properties;

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        titledBorder1 = new TitledBorder(BorderFactory.createEmptyBorder(50,
                    50, 50, 50), ""); 
        border1 = BorderFactory.createEmptyBorder(20, 50, 50, 50);
        jLabel1.setHorizontalTextPosition(JLabel.RIGHT);
        jLabel1.setVerticalTextPosition(JLabel.BOTTOM);
        jLabel1.setFont(new java.awt.Font("Dialog", Font.ITALIC, 40)); 
        jLabel1.setForeground(Color.black);
        jLabel1.setBorder(titledBorder1);
        jLabel1.setToolTipText(""); 
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText(prop.getProperty("appli.name", "Neatwork"));  
        this.setLayout(borderLayout1);
        jLabel2.setBorder(border1);
        jLabel2.setBackground(jLabel1.getBackground());
        jLabel2.setForeground(jLabel1.getForeground());
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));  
        jLabel2.setText(Messages.getString("PresentationPanel.text"));  
        this.add(jLabel1, BorderLayout.CENTER);
        this.add(jLabel2, BorderLayout.SOUTH);
    }
}
