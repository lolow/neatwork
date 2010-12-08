package neatwork.gui.fancytable;

import neatwork.Messages;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * JTable amélioré :
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
    String pathImage = "/neatwork/gui/images/"; //$NON-NLS-1$

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
                    "NewRow.png")); //$NON-NLS-1$
        insertRowAction = new AbstractAction("+", icon) { //$NON-NLS-1$
                    public void actionPerformed(ActionEvent e) {
                        addNewRow();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage +
                    "DeleteRow.png")); //$NON-NLS-1$
        deleteRowAction = new AbstractAction("-", icon) { //$NON-NLS-1$
                    public void actionPerformed(ActionEvent e) {
                        deleteRows();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage + "Undo.png")); //$NON-NLS-1$
        updateAction = new AbstractAction("u", icon) { //$NON-NLS-1$
                    public void actionPerformed(ActionEvent e) {
                        updateData();
                    }
                };
        icon = new ImageIcon(getClass().getResource(pathImage + "Copy.png")); //$NON-NLS-1$
        copyAction = new AbstractAction("c", icon) { //$NON-NLS-1$
                    public void actionPerformed(ActionEvent e) {
                        StringSelection stsel = new StringSelection(FancyTable.this.fancyTableModel.getCopy());
                        Clipboard system = Toolkit.getDefaultToolkit()
                                                  .getSystemClipboard();
                        system.setContents(stsel, stsel);
                    }
                };
        copyAction.putValue(Action.SHORT_DESCRIPTION,
            Messages.getString("FancyTable.Copy")); //$NON-NLS-1$
        icon = new ImageIcon(getClass().getResource(pathImage + "Paste.png")); //$NON-NLS-1$
        pasteAction = new AbstractAction("v", icon) { //$NON-NLS-1$
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
                                        "FancyTable.Clipboard_is_not_well_formated"), //$NON-NLS-1$
                                    Messages.getString(
                                        "FancyTable.Format_error"),
                                    JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
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
            Messages.getString("FancyTable.Paste")); //$NON-NLS-1$
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
