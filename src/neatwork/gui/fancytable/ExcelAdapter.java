package neatwork.gui.fancytable;

import neatwork.Messages;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;


/**
* ExcelAdapter enables Copy-Paste Clipboard functionality on JTables.
* The clipboard data format used by the adapter is compatible with
* the clipboard format used by Excel. This provides for clipboard
* interoperability between enabled JTables and Excel.
*/
public class ExcelAdapter implements ActionListener {
    private String rowstring;
    private String value;
    private Clipboard system;
    private StringSelection stsel;
    private JTable jTable1;

    /**
     * The Excel Adapter is constructed with a
     * JTable on which it enables Copy-Paste and acts
     * as a Clipboard listener.
     */
    public ExcelAdapter(JTable myJTable) {
        jTable1 = myJTable;

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.CTRL_MASK, false);

        // Identifying the copy KeyStroke user can modify this
        // to copy on some other Key combination.
        //KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
        // Identifying the Paste KeyStroke user can modify this
        //to copy on some other Key combination.
        jTable1.registerKeyboardAction(this,
            Messages.getString("ExcelAdapter.Copy"), copy, //$NON-NLS-1$
            JComponent.WHEN_FOCUSED);

        //jTable1.registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Public Accessor methods for the Table on which this adapter acts.
     */
    public JTable getJTable() {
        return jTable1;
    }

    public void setJTable(JTable jTable1) {
        this.jTable1 = jTable1;
    }

    /**
     * This method is activated on the Keystrokes we are listening to
     * in this implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed.
     * Paste is done by aligning the upper left corner of the selection with the
     * 1st element in the current selection of the JTable.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo(Messages.getString(
                        "ExcelAdapter.Copy")) == 0) { //$NON-NLS-1$

            StringBuffer sbf = new StringBuffer();

            // Check to ensure we have selected only a contiguous block of
            // cells
            int numcols = jTable1.getColumnCount();
            int numrows = jTable1.getRowCount();

            //int[] rowsselected=jTable1.getSelectedRows();
            //int[] colsselected=jTable1.getSelectedColumns();
            //if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
            //       numrows==rowsselected.length) &&
            //  (numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
            //       numcols==colsselected.length)))
            //{
            //   JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
            //    "Invalid Copy Selection",
            //    JOptionPane.ERROR_MESSAGE);
            //   return;
            //}
            for (int i = 0; i < numrows; i++) {
                for (int j = 0; j < numcols; j++) {
                    sbf.append(jTable1.getValueAt(i, j));

                    if (j < (numcols - 1)) {
                        sbf.append("\t"); //$NON-NLS-1$
                    }
                }

                sbf.append("\n"); //$NON-NLS-1$
            }

            stsel = new StringSelection(sbf.toString());
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
            system.setContents(stsel, stsel);
        }
    }
}
