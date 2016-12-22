package neatwork.gui;

import neatwork.Messages;

import neatwork.file.*;

import neatwork.gui.design.*;

import neatwork.gui.simu.*;

import neatwork.gui.tree.*;

import neatwork.project.*;

import java.awt.*;

import java.util.*;

import javax.swing.*;


/**
 * Panel qui affiche un design
 * @author L. DROUET
 * @version 1.0
 */
public class DesignPane extends JPanel {
    Design design;
    JTabbedPane jTabbedPane = new JTabbedPane();

    public DesignPane(Design design, Database database,
        AbstractFileManager fileManager, Properties properties) {
        this.design = design;
        setLayout(new BorderLayout());
        jTabbedPane.setTabPlacement(JTabbedPane.TOP);

        //text pane
        jTabbedPane.addTab(Messages.getString("DesignPane.Tables"),
            new DesignTablePane(design, database)); 
        jTabbedPane.addTab(Messages.getString("DesignPane.Simulation"), 
            new SimulationPane(design, fileManager, properties));
        jTabbedPane.addTab(Messages.getString("DesignPane.TreeView"),
            new TreePane(design)); 
        jTabbedPane.addTab(Messages.getString("DesignPane.Text"),
            new TextPane(design)); 

        add(jTabbedPane, BorderLayout.CENTER);
    }
}
