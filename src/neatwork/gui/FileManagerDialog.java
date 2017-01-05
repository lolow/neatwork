package neatwork.gui;

import neatwork.Messages;

import neatwork.file.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Classe qui definit la boite de dialogue du gestionnaire de fichier
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class FileManagerDialog extends JDialog
		implements Observer, ListSelectionListener, DocumentListener, ActionListener {

	private static final long serialVersionUID = 8103853824269816828L;
	public final static int TYPEDIALOG_OPEN = 0;
	public final static int TYPEDIALOG_SAVE = 1;
	public final static int TYPEDIALOG_DELETE = 2;
	public final static int TYPEDIALOG_NEW = 3;
	private AbstractFileManager fileManager;
	private int typeDialog;
	private int typeFile;
	private String content;
	private Action newFile;
	private Action openFile;
	private Action saveFile;
	private Action deleteFile;
	private Action cancel;
	private boolean cancelled = true;

	// composants
	private Box box;
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JButton jButton1 = new JButton();
	private JButton jButton2 = new JButton();
	private JButton jButtonPath = new JButton(Messages.getString("FileManagerDialog.Select"));
	private JButton jButtonUser = new JButton(Messages.getString("FileManagerDialog.User0"));
	private JLabel jLabel1 = new JLabel(Messages.getString("FileManagerDialog.Filename"));
	private JLabel jLabel = new JLabel();
	private JLabel jLabelPath = new JLabel();
	private JLabel jLabelStatus = new JLabel(" ");
	private JList<String> jList = new JList<String>();
	private JTextField jTextField = new JTextField();

	public FileManagerDialog(JFrame frame, AbstractFileManager fileManager, Properties properties) {
		super(frame, true);
		this.fileManager = fileManager;
		fileManager.addObserver(this);

		// Mise en place des composants
		Container content = getContentPane();
		content.setLayout(new BorderLayout(5, 5));

		// haut
		JPanel paneltop = new JPanel(new BorderLayout());
		paneltop.add(jLabel, BorderLayout.SOUTH);

		if (properties.getProperty("file.distant").equals("false")) {
			paneltop.add(new JLabel(Messages.getString("FileManagerDialog.Project_Path")), BorderLayout.WEST);
			jLabelPath.setText(((FileManagerDisk) fileManager).getProjectPath());
			jLabelPath.setAutoscrolls(true);
			jLabelPath.setForeground(Color.black);
			paneltop.add(jLabelPath, BorderLayout.CENTER);
			paneltop.add(jButtonPath, BorderLayout.EAST);
			jButtonPath.addActionListener(this);
		} else {
			paneltop.add(new JLabel(Messages.getString("FileManagerDialog.User")), BorderLayout.WEST);

			JLabel lbl = new JLabel(properties.getProperty("appli.user"));
			lbl.setForeground(Color.black);
			paneltop.add(lbl, BorderLayout.CENTER);
			paneltop.add(jButtonUser, BorderLayout.EAST);
			jButtonUser.addActionListener(this);
		}

		// droite
		jPanel1.setLayout(new BorderLayout());
		box = Box.createHorizontalBox();
		box.add(jButton1);
		box.add(Box.createHorizontalStrut(10));
		box.add(jButton2);
		jPanel1.add(box, BorderLayout.EAST);
		jPanel1.add(jLabelStatus, BorderLayout.SOUTH);

		// bas
		jPanel2.setLayout(new BorderLayout());
		jLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jPanel3.setLayout(new BorderLayout(5, 5));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jPanel3.add(jLabel1, BorderLayout.WEST);
		jPanel3.add(jTextField, BorderLayout.CENTER);
		jPanel3.add(jPanel1, BorderLayout.SOUTH);
		jPanel2.add(jPanel3, BorderLayout.SOUTH);

		// gauche
		jPanel4.setLayout(new BorderLayout());
		jPanel4.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		jPanel4.add(new JScrollPane(jList), BorderLayout.CENTER);
		jPanel4.add(paneltop, BorderLayout.NORTH);
		jPanel2.add(jPanel4, BorderLayout.CENTER);
		content.add(jPanel2, BorderLayout.CENTER);
		setSize(400, 400);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);

		// definition des composants
		jButton1.setDefaultCapable(true);

		ListSelectionModel listSelectionModel = new DefaultListSelectionModel();
		listSelectionModel.addListSelectionListener(this);
		jList.setSelectionModel(listSelectionModel);
		jTextField.getDocument().addDocumentListener(this);
		jTextField.setActionCommand("ENTER");
		jTextField.addActionListener(this);

		// definition des actions
		openFile = new NeatworkAction(Messages.getString("FileManagerDialog.Open"), null,
				Messages.getString("FileManagerDialog.Open_the_selected_file"), 'O') {

			private static final long serialVersionUID = -5705192141429230430L;

			public void actionPerformed(ActionEvent e) {
				if (!jTextField.getText().equals("")) {
					setContent(getFileManager().readFile(getFileName(), typeFile));
					setCancelled(false);
					setVisible(false);
				}
			}
		};

		newFile = new NeatworkAction(Messages.getString("FileManagerDialog.New"), null,
				Messages.getString("FileManagerDialog.Create_a_new_topography"), 'N') {

			private static final long serialVersionUID = -7267100328646520522L;

			public void actionPerformed(ActionEvent e) {
				if (!jTextField.getText().equals("")) {

					boolean ok = true;

					if (isInList(jTextField.getText())) {
						Object[] options = { Messages.getString("FileManagerDialog.Yes"),
								Messages.getString("FileManagerDialog.No") };

						if (JOptionPane.showOptionDialog(null,
								Messages.getString(
										"FileManagerDialog.This_file_already_exists._It_will_be_overwritten_if_you_click_on_Yes.")
										+ Messages.getString("FileManagerDialog.Click_no_to_abort."),
								Messages.getString("FileManagerDialog.Existing_File..."), JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]) == JOptionPane.NO_OPTION) {
							ok = false;
						}
					}

					if (ok) {
						setContent("");
						setCancelled(false);
						setVisible(false);
					}
				}
			}
		};

		saveFile = new NeatworkAction(Messages.getString("FileManagerDialog.Save"), null,
				Messages.getString("FileManagerDialog.Save_your_file"), 'O') {

			private static final long serialVersionUID = -9028842474858373304L;

			public void actionPerformed(ActionEvent e) {
				if (!jTextField.getText().equals("")) {

					Object[] options = { Messages.getString("FileManagerDialog.Yes"),
							Messages.getString("FileManagerDialog.No") };

					if ((!isInList(jTextField.getText())) || (JOptionPane.showOptionDialog(null,
							Messages.getString("FileManagerDialog.The_file") + jTextField.getText()
									+ Messages.getString("FileManagerDialog._exists._Do_you_wish_to_overwrite_it"),
							Messages.getString("FileManagerDialog.Overwrite_File"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]) == JOptionPane.YES_OPTION)) {
						getFileManager().deleteFile(jTextField.getText(), typeFile);

						if (getFileManager().writeFile(jTextField.getText(), getContent(), typeFile)) {
							setCancelled(false);
							setVisible(false);
						}
					}
				}
			}
		};

		deleteFile = new NeatworkAction(Messages.getString("FileManagerDialog.Delete"), null,
				Messages.getString("FileManagerDialog.Delete_the_selected_file"), 'O') {

			private static final long serialVersionUID = -7622276848158543068L;

			public void actionPerformed(ActionEvent e) {
				if (!jTextField.getText().equals("")) {
					getFileManager().deleteFile(jList.getSelectedValue().toString(), typeFile);
					setList();
				}
			}
		};

		cancel = new NeatworkAction(Messages.getString("FileManagerDialog.Cancel"), null, null, 'C') {

			private static final long serialVersionUID = -4109503495111028088L;

			public void actionPerformed(ActionEvent e) {
				setCancelled(true);
				setVisible(false);
			}
		};

		// ActionListener
		jList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					jButton1.doClick();
				}
			}
		});
	}

	public void show(int typeDialog, int typeFile) {
		this.typeDialog = typeDialog;
		this.typeFile = typeFile;
		jLabelStatus.setText(" ");
		setCancelled(true);
		setTitle();
		setComponents();
		setList();
		textChanged(null);
		this.setModal(true);
		jTextField.setEditable(typeDialog != TYPEDIALOG_OPEN);
		setVisible(true);
	}

	private void setTitle() {
		switch (typeDialog) {
		case TYPEDIALOG_OPEN:
			setTitle(Messages.getString("FileManagerDialog.Open_a"));

			break;

		case TYPEDIALOG_NEW:
			setTitle(Messages.getString("FileManagerDialog.New0"));

			break;

		case TYPEDIALOG_SAVE:
			setTitle(Messages.getString("FileManagerDialog.Save_a"));

			break;

		case TYPEDIALOG_DELETE:
			setTitle(Messages.getString("FileManagerDialog.Delete_a"));

			break;
		}

		switch (typeFile) {
		case Project.TYPE_DESIGN:
			setTitle(getTitle() + Messages.getString("FileManagerDialog._design"));

			break;

		case Project.TYPE_TOPO:
			setTitle(getTitle() + Messages.getString("FileManagerDialog._topography"));

			break;
		}
	}

	private void setComponents() {
		switch (typeDialog) {
		case TYPEDIALOG_OPEN:
			jButton1.setAction(openFile);
			jButton2.setAction(cancel);

			break;

		case TYPEDIALOG_NEW:
			jButton1.setAction(newFile);
			jButton2.setAction(cancel);

			break;

		case TYPEDIALOG_SAVE:
			jButton1.setAction(saveFile);
			jButton2.setAction(cancel);

			break;

		case TYPEDIALOG_DELETE:
			jButton1.setAction(deleteFile);
			jButton2.setAction(cancel);
			cancel.putValue(Action.NAME, Messages.getString("FileManagerDialog.Close"));

			break;
		}

		switch (typeFile) {
		case Project.TYPE_DESIGN:
			jLabel.setText(Messages.getString("FileManagerDialog.List_of_your_designs"));

			break;

		case Project.TYPE_TOPO:
			jLabel.setText(Messages.getString("FileManagerDialog.List_of_your_topographies"));

			break;
		}
	}

	private void setList() {
		jList.removeAll();

		String[] fileList = fileManager.getListFile(typeFile);

		if (fileList != null) {
			Arrays.sort(fileList);
			jList.setListData(fileList);
		}
	}

	public boolean getCancelled() {
		return cancelled;
	}

	private void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public String getFileName() {
		return jTextField.getText();
	}

	public void setFileName(String fileName) {
		jTextField.setText(fileName);
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void update(Observable observable, Object object) {
		if (fileManager.getFileManagerStatus().equals("")) {
			jLabelStatus.setText(" ");
		} else {
			jLabelStatus.setText(fileManager.getFileManagerStatus());
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (jList.getSelectedIndex() > -1) {
			jTextField.setText(jList.getSelectedValue().toString());
		}
	}

	private void textChanged(DocumentEvent e) {
		jButton1.setEnabled(!(jTextField.getText().length() == 0));
	}

	private boolean isInList(String value) {
		boolean contains = false;

		for (int i = 0; i < jList.getModel().getSize(); i++) {
			contains |= jList.getModel().getElementAt(i).toString().equals(value);
		}

		return contains;
	}

	private AbstractFileManager getFileManager() {
		return fileManager;
	}

	public void insertUpdate(DocumentEvent e) {
		textChanged(e);
	}

	public void removeUpdate(DocumentEvent e) {
		textChanged(e);
	}

	public void changedUpdate(DocumentEvent e) {
		textChanged(e);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ENTER")) {
			jButton1.doClick();
		}

		if (e.getSource().equals(jButtonPath)) {
			// browse
			JFileChooser fc = new JFileChooser(jLabelPath.getText());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.showDialog(this, Messages.getString("FileManagerDialog.Select"));

			File fich = fc.getSelectedFile();

			if ((fich != null) && (fich.exists())) {
				((FileManagerDisk) fileManager).setProjectPath(fich.getAbsolutePath());
				jLabelStatus.setText("");
				setList();
				setComponents();
			}
		}

		if (e.getSource().equals(jButtonUser)) {
			// user
		}
	}
}
