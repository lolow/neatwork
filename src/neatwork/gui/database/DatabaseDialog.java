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
    String pathImage = "/neatwork/gui/images/"; 

    public DatabaseDialog(FrameNeatwork frame, Database database0) {
        super(frame, Messages.getString("DatabaseDialog.Materials_database"),
            true); 
        setSize(640, 400);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2,
            (d.height - getSize().height) / 2);
        this.database = database0;
        database.addObserver(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        boolean standalone = frame.properties.getProperty("appli.standalone") 
            .equals("true"); 
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
            Messages.getString("DatabaseDialog.Insert_a_new_diameter")); 

        //DeleteDiam
        actions[1] = jTableDiameter.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Delete_selected_diameter(s)")); 

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
            Messages.getString("DatabaseDialog.Load_data")); 
        actions[6].putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().getResource(pathImage + "DataExtract.png"))); 

        //check
        Icon icon = new ImageIcon(getClass().getResource(pathImage +
                    "DataStore.png")); 
        actions[7] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Apply"), icon,
                Messages.getString("DatabaseDialog.Save_data"), 'A') {  
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
                    "report.png")); 
        actions[9] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Export"), icon3,
                Messages.getString("DatabaseDialog.Report_in_HTML"), 'A') {  
                    public void actionPerformed(ActionEvent e) {
                        //Ecris un rapport html des diameters
                        String s = Messages.getString(
                                "DatabaseDialog.<html><head><title>"); 
                        s += Messages.getString(
                            "DatabaseDialog.Database_-_Diameters"); 
                        s += "</title></head><body>"; 
                        s += ("<h1>" +
                        Messages.getString(
                            "DatabaseDialog.Database_-_Diameters") + "</h1>");   //$NON-NLS-3$
                        s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; 
                        s += Messages.getString(
                            "DatabaseDialog.<tr><th>Nominal</th><th>SDR</th><th>Cost</th><th>Max_Pressure</th><th>Type</th><th>Roughness</th></tr>"); 

                        for (Enumeration en = database.getDiameters();
                                en.hasMoreElements();) {
                            Diameter d = (Diameter) en.nextElement();
                            s += "<tr>"; 
                            s += ("<td>" + d.getNominal() + "</td>");  
                            s += ("<td>" + d.getSdr() + "</td>");  
                            s += ("<td>" + d.getCost() + "</td>");  
                            s += ("<td>" + d.getMaxLength() + "</td>");  
                            s += ("<td>" + d.getType() + "</td>");  
                            s += ("<td>" + d.getRoughness() + "</td>");  
                            s += "</tr>"; 
                        }

                        s += ("</table><p>" + new Date().toString() + "</p>");  
                        Tools.enregFich(s);
                    }
                };
        actions[9].setEnabled(standalone);
        jTabbedPane.addTab(Messages.getString("DatabaseDialog.Diameters"), 
            new FancyTablePanel(Messages.getString(
                    "DatabaseDialog.Diameters_list"), actions, jTableDiameter,
                true)); 

        //orifice
        actions = new Action[10];

        //AddOrifice
        actions[0] = jTableOrifice.insertRowAction;
        actions[0].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Insert_a_new_orifice")); 

        //DeleteOrifice
        actions[1] = jTableOrifice.deleteRowAction;
        actions[1].putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("DatabaseDialog.Delete_selected_orifice(s)")); 

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
            Messages.getString("DatabaseDialog.Load_data")); 
        actions[6].putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().getResource(pathImage + "DataExtract.png"))); 

        //
        icon = new ImageIcon(getClass().getResource(pathImage +
                    "DataStore.png")); 
        actions[7] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Apply"), icon,
                Messages.getString("DatabaseDialog.Save_data"), 'A') {  
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
                    "report.png")); 
        actions[9] = new NeatworkAction(Messages.getString(
                    "DatabaseDialog.Export"), icon3,
                Messages.getString("DatabaseDialog.Report_in_HTML"), 'A') {  
                    public void actionPerformed(ActionEvent e) {
                        //Ecris un rapport html des orifices
                        String s = "<html><head><title>"; 
                        s += Messages.getString(
                            "DatabaseDialog.Database_-_Orifices"); 
                        s += "</title></head><body>"; 
                        s += ("<h1>" +
                        Messages.getString("DatabaseDialog.Database_-_Orifices") +
                        "</h1>");   //$NON-NLS-3$
                        s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; 
                        s += Messages.getString(
                            "DatabaseDialog.<tr><th>Diameter</th></tr>"); 

                        for (Iterator en = database.getOrifices().iterator();
                                en.hasNext();) {
                            Orifice d = (Orifice) en.next();
                            s += "<tr>"; 
                            s += ("<td>" + d.getDiameter() + "</td>");  
                            s += "</tr>"; 
                        }

                        s += ("</table><p>" + new Date().toString() + "</p>");  
                        Tools.enregFich(s);
                    }
                };
        actions[9].setEnabled(standalone);
        jTabbedPane.addTab(Messages.getString("DatabaseDialog.Orifices"), 
            Tools.getPanelTable(Messages.getString(
                    "DatabaseDialog.<html>Orifices_list"), actions,
                jTableOrifice)); 

        panel.add(jTabbedPane, BorderLayout.CENTER);

        JPanel jPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jPanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton buttonClose = new JButton(Messages.getString(
                    "DatabaseDialog.Close")); 
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
