package neatwork.gui.fancytable;

import neatwork.Messages;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * JTable am�lior� :
 * @author L. DROUET
 * @version 1.0
 */
public class FancyTable extends JTable {
    private FancyTableModel fancyTableModel;
    public Action insertRowAction;
    public Action deleteRowAction;
    public Action updateAction;
    public Action copyAction;
    public Action pasteAction;
    private Color colorChanged;
    private Color colorUndo;
    private boolean updateColor;
    String pathImage = "/neatwork/gui/images/"; 

    public FancyTable(FancyTableModel fancyTableModel) {
        this(fancyTableModel, false);
    }

    public FancyTable(FancyTableModel fancyTableModel, boolean updateColor) {
        colorUndo = getBackground();
        colorChanged = new Color(200, 200, 200);
        this.updateColor = updateColor;

        //setCellSelectionEnabled(true);
        //avec tri
        TableSorter sorter = new TableSorter(fancyTableModel);
        setModel(sorter);

        //sorter.addMouseListenerToHeaderInTable(this);
        //sans tri
        //this.setModel(fancyTableModel);
        this.fancyTableModel = fancyTableModel;

        //define standard actions
        Icon icon = new ImageIcon(getClass().getResource(pathImage +
                    "NewRow.png")); 
        insertRowAction = new AbstractAction("+", icon) { 
                    public void actionPerformed(ActionEvent e) {
                        addNewRow();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage +
                    "DeleteRow.png")); 
        deleteRowAction = new AbstractAction("-", icon) { 
                    public void actionPerformed(ActionEvent e) {
                        deleteRows();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage + "Undo.png")); 
        updateAction = new AbstractAction("u", icon) { 
                    public void actionPerformed(ActionEvent e) {
                        updateData();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage + "Copy.png")); 
        copyAction = new AbstractAction("c", icon) { 
                    public void actionPerformed(ActionEvent e) {
                        StringSelection stsel = new StringSelection(FancyTable.this.fancyTableModel.getCopy());
                        Clipboard system = Toolkit.getDefaultToolkit()
                                                  .getSystemClipboard();
                        system.setContents(stsel, stsel);
                    }
                };
        copyAction.putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("FancyTable.Copy")); 
        icon = new ImageIcon(getClass().getResource(pathImage + "Paste.png")); 
        pasteAction = new AbstractAction("v", icon) { 
                    public void actionPerformed(ActionEvent e) {
                        Clipboard system = Toolkit.getDefaultToolkit()
                                                  .getSystemClipboard();

                        try {
                            String paste = system.getContents(this)
                                                 .getTransferData(DataFlavor.stringFlavor)
                                                 .toString();

                            if (!FancyTable.this.fancyTableModel.setPaste(paste)) {
                                JOptionPane.showMessageDialog(null,
                                    Messages.getString(
                                        "FancyTable.Clipboard_is_not_well_formated"), 
                                    Messages.getString(
                                        "FancyTable.Format_error"),
                                    JOptionPane.ERROR_MESSAGE); 
                            } else {
                                if (FancyTable.this.updateColor) {
                                    setBackground(colorChanged);
                                }
                            }
                        } catch (IOException ex) {
                        } catch (UnsupportedFlavorException ex) {
                        }
                    }
                };
        pasteAction.putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("FancyTable.Paste")); 
        autoFit();

        //ExcelAdapter ea = new ExcelAdapter(this);
        setDefaultRenderer(Double.class, new FancyTableCellRenderer());
        setDefaultRenderer(String.class, new FancyTableCellRenderer());
    }

    public void autoFit() {
        //tailles des colonnes
        if (fancyTableModel.getWidthHeader().length > 0) {
            int width = 0;

            for (int i = 0; i < fancyTableModel.getWidthHeader().length; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(fancyTableModel.getWidthHeader()[i]);
                width += fancyTableModel.getWidthHeader()[i];
            }

            setPreferredScrollableViewportSize(new Dimension(width + 15, 1000));
        }
    }

    public void addNewRow() {
        fancyTableModel.addNewLine(getSelectedRow() + 1);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scrollRectToVisible(getCellRect(fancyTableModel.getRow(),
                            1, true));
                }
            });
        copyAction.setEnabled(false);

        if (updateColor) {
            setBackground(colorChanged);
        }
    }

    public void deleteRows() {
        fancyTableModel.deleteLines(getSelectedRows());
        copyAction.setEnabled(false);

        if (updateColor) {
            setBackground(colorChanged);
        }
    }

    public void updateData() {
        if (updateColor) {
            setBackground(colorUndo);
        }

        fancyTableModel.updateData();
        copyAction.setEnabled(true);
    }

    public void editingStopped(ChangeEvent e) {
        if (updateColor) {
            setBackground(colorChanged);
        }

        copyAction.setEnabled(false);
        super.editingStopped(e);
    }
}
