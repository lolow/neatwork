package neatwork.gui;

import neatwork.Messages;

import neatwork.file.*;

import neatwork.gui.tabbedpane.*;

import neatwork.project.*;

import java.awt.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * Jpanel contenant les projets
 * @author L. DROUET
 * @version 1.0
 */
public class ProjectPane extends JPanel implements Observer, ChangeListener {
    private ProjectManager projectManager;
    private AbstractFileManager fileManager;
    private Properties properties;
    private Database database;
    private SingleRowTabbedPane tabbedPane;
    private Hashtable icons = new Hashtable();

    public ProjectPane(ProjectManager projectManager,
        AbstractFileManager fileManager, Properties properties,
        Database database) {
        this.projectManager = projectManager;
        this.fileManager = fileManager;
        this.properties = properties;
        this.database = database;
        icons.put(new Integer(Project.TYPE_TOPO),
            new ImageIcon(getClass().getResource("/neatwork/gui/images/Topo.gif"))); 
        icons.put(new Integer(Project.TYPE_DESIGN),
            new ImageIcon(getClass().getResource("/neatwork/gui/images/Design.gif"))); 

        projectManager.addObserver(this);

        //composants
        setLayout(new BorderLayout());
        tabbedPane = new SingleRowTabbedPane(SingleRowTabbedPane.FOUR_BUTTONS,
                SwingConstants.RIGHT);
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.addChangeListener(this);
        createPanes();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createPanes() {
        int size = tabbedPane.getTabCount();

        for (int i = 0; i < size; i++) {
            tabbedPane.removeTabAt(0);
        }

        Iterator iter = projectManager.getProjectIterator();

        while (iter.hasNext()) {
            Project project = (Project) iter.next();
            tabbedPane.addTab(project.getShortName(),
                (Icon) icons.get(new Integer(project.getType())),
                createPanel(project), project.getName());
        }

        if (tabbedPane.getTabCount() == 0) {
            PresentationPanel panel = new PresentationPanel(properties);
            tabbedPane.addTab(Messages.getString("ProjectPane.NeatWork"), panel); 
        }

        projectManager.setIndex(tabbedPane.getSelectedIndex());
    }

    private JPanel createPanel(Project project) {
        switch (project.getType()) {
        case Project.TYPE_TOPO:
            return new TopographiePane((Topographie) project);

        case Project.TYPE_DESIGN:
            return new DesignPane((Design) project, database, fileManager,
                properties);
        }

        return new JPanel();
    }

    public void update(Observable observable, Object object) {
        if (observable.getClass().isInstance(projectManager)) {
            int choix = ((Integer) object).intValue();

            switch (choix) {
            case ProjectManager.MODIF_SETPROJECT:

                int cur = tabbedPane.getSelectedIndex();
                createPanes();
                tabbedPane.setSelectedIndex(cur);

                break;

            case ProjectManager.MODIF_ADDPROJECT:
                createPanes();
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

                break;

            case ProjectManager.MODIF_REMOVEPROJECT:

                int sel = tabbedPane.getSelectedIndex();

                if (sel == (tabbedPane.getTabCount() - 1)) {
                    sel--;
                }

                createPanes();

                if (sel > -1) {
                    tabbedPane.setSelectedIndex(sel);
                }

                break;

            default:}
        }
    }

    public void stateChanged(ChangeEvent e) {
        projectManager.setIndex(tabbedPane.getSelectedIndex());
    }
}
