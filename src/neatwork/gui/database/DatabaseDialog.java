package neatwork.gui.database;

import neatwork.Messages;

import neatwork.gui.*;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * boite de dialogue pour la database
 * @author L. DROUET
 * @version 1.0
 */
public class DatabaseDialog extends JDialog implements Observer {
    private Database database;
    private DBDiameterTableModel diameterTableModel;
    private DBOrificeTableModel orificeTableModel;
    private FancyTable jTableOrifice;
    private FancyTable jTableDiameter;
    private JTabbedPane jTabbedPane;
    String pathImage = "/neatwork/gui/images/"; //$NON-NLS-1$

    public DatabaseDialog(FrameNeatwork frame, Database database0) {
        super(frame, Messages.getString("DatabaseDialog.Materials_database"),
            true); //$NON-NLS-1$
        setSize(640, 400);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        this.database = database0;
        database.addObserver(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        boolean standalone = frame.properties.getProperty("appli.standalone") //$NON-NLS-1$
            .equals("true"); //$NON-NLS-1$
        diameterTableModel = new DBDiameterTableModel(database);

        orificeTableModel = new DBOrificeTableModel(database);

        jTableDiameter = new FancyTable(diameterTableModel, true);
        jTableOrifice = new FancyTable(orificeTableModel, true);

        jTabbedPane = new JTabbedPane();

        //diameters
        Action[] actions = new Action[10];

        //AddDiam
        actions[0] = jTableDiameter.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Insert_a_new_diameter")); //$NON-NLS-1$

        //DeleteDiam
        actions[1] = jTableDiameter.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Delete_selected_diameter(s)")); //$NON-NLS-1$

        //espace
        actions[2] = null;

        //undo
        actions[3] = jTableDiameter.copyAction;

        //paste
        actions[4] = jTableDiameter.pasteAction;

        //espace
        actions[5] = null;

        //undo
        actions[6] = jTableDiameter.updateAction;
        actions[6].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Load_data")); //$NON-NLS-1$
        actions[6].putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().getResource(pathImage + "DataExtract.png"))); //$NON-NLS-1$

        //check
        Icon icon = new ImageIcon(getClass().getResource(pathImage +
                    "DataStore.png")); //$NON-NLS-1$
        actions[7] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Apply"), icon,
                Messages.getString("DatabaseDialog.Save_data"), 'A') { //$NON-NLS-1$ //$NON-NLS-2$
                    public void actionPerformed(ActionEvent e) {
                        database.setDiametersContent(diameterTableModel.getContent());
                        jTableDiameter.updateData();
                        database.saveDiameters(diameterTableModel.getContent());
                    }
                };

        //espace
        actions[8] = null;

        //save tableau
        Icon icon3 = new ImageIcon(getClass().getResource(pathImage +
                    "report.png")); //$NON-NLS-1$
        actions[9] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Export"), icon3,
                Messages.getString("DatabaseDialog.Report_in_HTML"), 'A') { //$NON-NLS-1$ //$NON-NLS-2$
                    public void actionPerformed(ActionEvent e) {
                        //Ecris un rapport html des diameters
                        String s = Messages.getString(
                                "DatabaseDialog.<html><head><title>"); //$NON-NLS-1$
                        s += Messages.getString(
                            "DatabaseDialog.Database_-_Diameters"); //$NON-NLS-1$
                        s += "</title></head><body>"; //$NON-NLS-1$
                        s += ("<h1>" +
                        Messages.getString(
                            "DatabaseDialog.Database_-_Diameters") + "</h1>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
                        s += Messages.getString(
                            "DatabaseDialog.<tr><th>Nominal</th><th>SDR</th><th>Cost</th><th>Max_Pressure</th><th>Type</th><th>Roughness</th></tr>"); //$NON-NLS-1$

                        for (Enumeration en = database.getDiameters();
                                en.hasMoreElements();) {
                            Diameter d = (Diameter) en.nextElement();
                            s += "<tr>"; //$NON-NLS-1$
                            s += ("<td>" + d.getNominal() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += ("<td>" + d.getSdr() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += ("<td>" + d.getCost() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += ("<td>" + d.getMaxLength() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += ("<td>" + d.getType() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += ("<td>" + d.getRoughness() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += "</tr>"; //$NON-NLS-1$
                        }

                        s += ("</table><p>" + new Date().toString() + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
                        Tools.enregFich(s);
                    }
                };
        actions[9].setEnabled(standalone);
        jTabbedPane.addTab(Messages.getString("DatabaseDialog.Diameters"), //$NON-NLS-1$
            new FancyTablePanel(Messages.getString(
                    "DatabaseDialog.Diameters_list"), actions, jTableDiameter,
                true)); //$NON-NLS-1$

        //orifice
        actions = new Action[10];

        //AddOrifice
        actions[0] = jTableOrifice.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Insert_a_new_orifice")); //$NON-NLS-1$

        //DeleteOrifice
        actions[1] = jTableOrifice.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Delete_selected_orifice(s)")); //$NON-NLS-1$

        //espace
        actions[2] = null;

        //undo
        actions[3] = jTableOrifice.copyAction;

        //paste
        actions[4] = jTableOrifice.pasteAction;

        //espace
        actions[5] = null;

        //undo
        actions[6] = jTableOrifice.updateAction;
        actions[6].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Load_data")); //$NON-NLS-1$
        actions[6].putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().getResource(pathImage + "DataExtract.png"))); //$NON-NLS-1$

        //
        icon = new ImageIcon(getClass().getResource(pathImage +
                    "DataStore.png")); //$NON-NLS-1$
        actions[7] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Apply"), icon,
                Messages.getString("DatabaseDialog.Save_data"), 'A') { //$NON-NLS-1$ //$NON-NLS-2$
                    public void actionPerformed(ActionEvent e) {
                        database.setOrificesContent(orificeTableModel.getContent());
                        jTableOrifice.updateData();
                        database.saveOrifices(orificeTableModel.getContent());
                    }
                };

        //espace
        actions[8] = null;

        //save tableau
        Icon icon4 = new ImageIcon(getClass().getResource(pathImage +
                    "report.png")); //$NON-NLS-1$
        actions[9] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Export"), icon3,
                Messages.getString("DatabaseDialog.Report_in_HTML"), 'A') { //$NON-NLS-1$ //$NON-NLS-2$
                    public void actionPerformed(ActionEvent e) {
                        //Ecris un rapport html des orifices
                        String s = "<html><head><title>"; //$NON-NLS-1$
                        s += Messages.getString(
                            "DatabaseDialog.Database_-_Orifices"); //$NON-NLS-1$
                        s += "</title></head><body>"; //$NON-NLS-1$
                        s += ("<h1>" +
                        Messages.getString("DatabaseDialog.Database_-_Orifices") +
                        "</h1>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
                        s += Messages.getString(
                            "DatabaseDialog.<tr><th>Diameter</th></tr>"); //$NON-NLS-1$

                        for (Iterator en = database.getOrifices().iterator();
                                en.hasNext();) {
                            Orifice d = (Orifice) en.next();
                            s += "<tr>"; //$NON-NLS-1$
                            s += ("<td>" + d.getDiameter() + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            s += "</tr>"; //$NON-NLS-1$
                        }

                        s += ("</table><p>" + new Date().toString() + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
                        Tools.enregFich(s);
                    }
                };
        actions[9].setEnabled(standalone);
        jTabbedPane.addTab(Messages.getString("DatabaseDialog.Orifices"), //$NON-NLS-1$
            Tools.getPanelTable(Messages.getString(
                    "DatabaseDialog.<html>Orifices_list"), actions,
                jTableOrifice)); //$NON-NLS-1$

        panel.add(jTabbedPane, BorderLayout.CENTER);

        JPanel jPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jPanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton buttonClose = new JButton(Messages.getString(
                    "DatabaseDialog.Close")); //$NON-NLS-1$
        jPanel2.add(buttonClose);
        buttonClose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        panel.add(jPanel2, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void updateDiametersTable() {
        diameterTableModel.updateData();
    }

    private void updateOrificesTable() {
        orificeTableModel.updateData();
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(database)) {
            switch (((Integer) param).intValue()) {
            case Database.MODIF_ORIFICE:
                updateOrificesTable();

                break;

            case Database.MODIF_DIAMETER:
                updateDiametersTable();

                break;
            }
        }
    }
}
