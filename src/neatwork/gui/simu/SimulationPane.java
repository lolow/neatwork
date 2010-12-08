package neatwork.gui.simu;

import neatwork.file.*;

import neatwork.gui.*;

import neatwork.gui.fancytable.*;

import neatwork.project.*;

import neatwork.solver.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import neatwork.Messages;


/**
 * Panel d'affichage des simulation
 * @author L. DROUET
 * @version 1.0
 */
public class SimulationPane extends JPanel implements ActionListener,
	ListSelectionListener {
	private Vector listSimu;
	private String[] listTable = {
		"", Messages.getString("SimulationPane.Flows_at_faucets"), Messages.getString("SimulationPane.Percentiles_at_faucets"), Messages.getString("SimulationPane.Speed_in_pipes"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Messages.getString("SimulationPane.Nodes_pressure") //$NON-NLS-1$
	};
	private Design design;
	private AbstractFileManager fileManager;
	private Properties properties;
	private Action deleteSimuAction;
	private Action reportAction;
	private SimuTableModel simuTableModel;
	private Simulation simu;
	private JButton buttonNew;
	private JTextArea textAreaParam;
	private JComboBox comboSimu;
	private JList listTableau;
	private FancyTable table;

	public SimulationPane(Design design, AbstractFileManager fileManager,
		Properties defProp) {
		//init
		this.design = design;
		this.fileManager = fileManager;
		this.properties = defProp;
		setListSimu(fileManager.getListFile(Project.TYPE_SIMU));

		Icon icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/Delete.png")); //$NON-NLS-1$
		deleteSimuAction = new AbstractAction("", icon) { //$NON-NLS-1$
					public void actionPerformed(ActionEvent e) {
						deleteSimu();
					}
				};
		deleteSimuAction.putValue(Action.SHORT_DESCRIPTION,
			Messages.getString("SimulationPane.Delete_this_simulation")); //$NON-NLS-1$

		icon = new ImageIcon(getClass().getResource("/neatwork/gui/images/report.png")); //$NON-NLS-1$
		reportAction = new AbstractAction("", icon) { //$NON-NLS-1$
					public void actionPerformed(ActionEvent e) {
						String s = "<html><head><title>"; //$NON-NLS-1$
						s += Messages.getString("SimulationPane.Simulation_report"); //$NON-NLS-1$
						s += "</title></head><body>"; //$NON-NLS-1$
						s += ("<h1>" + Messages.getString("SimulationPane.Simulation_report0") + simu.getName() + //$NON-NLS-1$ //$NON-NLS-2$
						"</h1>"); //$NON-NLS-1$
						s += ("<h2>" + Messages.getString("SimulationPane.Parameters") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						s += (Messages.getString("SimulationPane.Simulated_design") + //$NON-NLS-1$
						SimulationPane.this.design.getName() + "<br>"); //$NON-NLS-1$
						s += (Messages.getString("SimulationPane.Type_of_simulation0") + //$NON-NLS-1$
						simu.getProperties().getProperty("simu.typesimu.value")); //$NON-NLS-1$

						if (simu.getProperties()
									.getProperty("simu.typesimu.value").equals("random")) { //$NON-NLS-1$ //$NON-NLS-2$
							s += (Messages.getString("SimulationPane.<br>Number_of_simulations") + //$NON-NLS-1$
							simu.getProperties().getProperty("simu.nbsim.value")); //$NON-NLS-1$
							s += (Messages.getString("SimulationPane.<br>Fraction_of_open_faucets") + //$NON-NLS-1$
							simu.getProperties().getProperty("simu.simopentaps.value")); //$NON-NLS-1$
						}

						if (simu.getProperties()
									.getProperty("simu.typesimu.value").equals("tapbytap")) { //$NON-NLS-1$ //$NON-NLS-2$
							s += (Messages.getString("SimulationPane.<br>Number_of_simulations") + //$NON-NLS-1$
							simu.getProperties().getProperty("simu.nbsim.value")); //$NON-NLS-1$
						}

						s += (Messages.getString("SimulationPane.<br>Used_type_of_orifice") + //$NON-NLS-1$
						simu.getProperties().getProperty("simu.typeorifice.value")); //$NON-NLS-1$
						s += Messages.getString("SimulationPane.<h2>Flows_at_faucet</h2>"); //$NON-NLS-1$
						s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
						s += (Messages.getString("SimulationPane.<tr><th>Faucet_ID</th><th>Nb_of_occurences</th><th>Min</th>") + //$NON-NLS-1$
						Messages.getString("SimulationPane.<th>Average</th><th>Max</th><th>Variability</th><th>&lt;") + //$NON-NLS-1$
						simu.getProperties().getProperty("simu.mincriticalflow.value", //$NON-NLS-1$
							"") + "</th><th>&gt; " + //$NON-NLS-1$ //$NON-NLS-2$
						simu.getProperties().getProperty("simu.maxcriticalflow.value", //$NON-NLS-1$
							"") + Messages.getString("SimulationPane.</th><th>nb_of_failures</th></tr>")); //$NON-NLS-1$ //$NON-NLS-2$

						for (Enumeration en = simu.getFlowTaps().elements();
								en.hasMoreElements();) {
							Vector v = (Vector) en.nextElement();
							s += "<tr>"; //$NON-NLS-1$

							for (int i = 0; i < v.size(); i++) {
								s += ("<td>" + v.get(i) + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
							}

							s += "</tr>"; //$NON-NLS-1$
						}

						s += "</table>"; //$NON-NLS-1$
						s += Messages.getString("SimulationPane.<h2>Percentiles_at_faucet</h2>"); //$NON-NLS-1$
						s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
						s += (Messages.getString("SimulationPane.<tr><th>Faucet_ID</th><th>Nb_of_occurences</th><th>Min</th>") + //$NON-NLS-1$
						Messages.getString("SimulationPane.<th>&lt;_10</th><th>&lt;_25</th><th>&lt;_50</th><th>&lt;_75</th>") + //$NON-NLS-1$
						Messages.getString("SimulationPane.<th>&lt;_90</th><th>Max</th></tr>")); //$NON-NLS-1$

						for (Enumeration en = simu.getQuartileTaps().elements();
								en.hasMoreElements();) {
							Vector v = (Vector) en.nextElement();
							s += "<tr>"; //$NON-NLS-1$

							for (int i = 0; i < v.size(); i++) {
								s += ("<td>" + v.get(i) + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
							}

							s += "</tr>"; //$NON-NLS-1$
						}

						s += "</table>"; //$NON-NLS-1$
						s += Messages.getString("SimulationPane.<h2>Speed_in_pipes</h2>"); //$NON-NLS-1$
						s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
						s += (Messages.getString("SimulationPane.<tr><th>Pipe_ID</th><th>Nb_of_simulation</th><th>Average</th>") + //$NON-NLS-1$
						Messages.getString("SimulationPane.<th>Maximum</th></tr>")); //$NON-NLS-1$

						for (Enumeration en = simu.getQuartileTaps().elements();
								en.hasMoreElements();) {
							Vector v = (Vector) en.nextElement();
							s += "<tr>"; //$NON-NLS-1$

							for (int i = 0; i < 4; i++) {
								s += ("<td>" + v.get(i) + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
							}

							s += "</tr>"; //$NON-NLS-1$
						}

						s += "</table>"; //$NON-NLS-1$
						s += Messages.getString("SimulationPane.<h2>Nodes_Pressure</h2>"); //$NON-NLS-1$
						s += "<table BORDER CELLPADDING=0 CELLSPACING=0>"; //$NON-NLS-1$
						s += (Messages.getString("SimulationPane.<tr><th>Node_ID</th><th>Minimum</th><th>Average</th>") + //$NON-NLS-1$
						Messages.getString("SimulationPane.<th>Maximum</th></tr>")); //$NON-NLS-1$

						for (Enumeration en = simu.getNodesPressure().elements();
								en.hasMoreElements();) {
							Vector v = (Vector) en.nextElement();
							s += "<tr>"; //$NON-NLS-1$

							for (int i = 0; i < v.size(); i++) {
								s += ("<td>" + v.get(i) + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
							}

							s += "</tr>"; //$NON-NLS-1$
						}

						s += "</table>"; //$NON-NLS-1$
						Tools.enregFich(s);
					}
				};
		reportAction.putValue(Action.SHORT_DESCRIPTION, Messages.getString("SimulationPane.report_in_HTML")); //$NON-NLS-1$

		simuTableModel = new SimuTableModel(design, simu, table);

		//dessin
		dessinComponent();

		if (listSimu.size() > 0) {
			comboSimu.setSelectedIndex(0);
		}

		updateComponent();
	}

	private void runSimulation(NewSimuDialog dialog) {
		//solver
		AbstractSolver solver = null;

		if (properties.getProperty("solver.distant").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
			solver = new SolverClient(properties);
		} else {
			solver = new SolverDisk();
		}

		//init
		solver.init();

		ThreadRunSimulation thread = new ThreadRunSimulation(dialog, solver);

		//affiche la barre de progression
		SolverProgressDialog solverDialog = new SolverProgressDialog(solver,
				thread);
		solverDialog.setVisible(true);

		//enregistre la simulation
		String firstName = getFirstPossibleSimuName();
		String fileName = design.getName() + "." + firstName; //$NON-NLS-1$
		fileManager.writeFile(fileName, solver.getSimulationContent(),
			Project.TYPE_SIMU);

		//ajout de la simulation a la liste
		comboSimu.addItem("simu " + firstName); //$NON-NLS-1$
		comboSimu.setSelectedIndex(comboSimu.getModel().getSize() - 1);
	}

	private void deleteSimu() {
		if (comboSimu.getSelectedIndex() > -1) {
			String simuName = comboSimu.getSelectedItem().toString().substring(5);
			fileManager.deleteFile(design.getName() + "." + simuName, //$NON-NLS-1$
				Project.TYPE_SIMU);
			comboSimu.removeItemAt(comboSimu.getSelectedIndex());
		}
	}

	private void setListSimu(String[] names) {
		listSimu = new Vector();

		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(design.getName() + ".")) { //$NON-NLS-1$
				listSimu.add("simu " + //$NON-NLS-1$
					names[i].substring(design.getName().length() + 1));
			}
		}
	}

	private String getFirstPossibleSimuName() {
		int num = 0;
		Enumeration e = listSimu.elements();

		while (e.hasMoreElements()) {
			String item = e.nextElement().toString();
			int itemNum = Integer.parseInt(item.substring("simu ".length())); //enl\u00E8ve "simu " //$NON-NLS-1$
			num = Math.max(num, itemNum + 1);
		}

		return "" + num; //$NON-NLS-1$
	}

	private void dessinComponent() {
		//panel en haut a gauche
		JPanel panel0 = new JPanel(new BorderLayout(3, 3));
		comboSimu = new JComboBox(listSimu);
		comboSimu.setActionCommand("SelectSimu"); //$NON-NLS-1$
		comboSimu.addActionListener(this);
		buttonNew = new JButton(Messages.getString("SimulationPane.New_Simulation...")); //$NON-NLS-1$
		buttonNew.setActionCommand("NewSim"); //$NON-NLS-1$
		buttonNew.addActionListener(this);

		JPanel panel02 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel02.add(buttonNew);

		JButton b0 = new JButton(deleteSimuAction);
		b0.setBorder(BorderFactory.createEtchedBorder());
		panel02.add(b0);

		JButton b1 = new JButton(reportAction);
		b1.setBorder(BorderFactory.createEtchedBorder());
		panel02.add(b1);

		JPanel jpanelT = new JPanel(new BorderLayout());
		jpanelT.add(panel02, BorderLayout.NORTH);
		jpanelT.add(comboSimu, BorderLayout.SOUTH);
		textAreaParam = new JTextArea(""); //$NON-NLS-1$
		textAreaParam.setEditable(false);
		panel0.add(jpanelT, BorderLayout.NORTH);
		panel0.add(new JScrollPane(textAreaParam), BorderLayout.CENTER);

		//liste en haut a droite
		listTableau = new JList(listTable);
		listTableau.addListSelectionListener(this);

		//tableau en bas
		table = new FancyTable(simuTableModel);

		//mise en place des composants
		JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel0,
				new JScrollPane(listTableau));
		split1.setOneTouchExpandable(true);
		split1.setDividerLocation(300);

		JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split1,
				new JScrollPane(table));
		split2.setOneTouchExpandable(true);
		split2.setDividerLocation(200);

		this.setLayout(new BorderLayout());
		this.add(split2, BorderLayout.CENTER);
	}

	public void updateComponent() {
		boolean itsOK = (simu != null);
		textAreaParam.setEnabled(itsOK);
		table.setEnabled(itsOK);
		listTableau.setEnabled(itsOK);

		String content = ""; //$NON-NLS-1$
		
		String[] listTypeSimu = { 
			Messages.getString("NewSimuDialog.monte-carlo_sampling"), 
			Messages.getString("NewSimuDialog.individual_faucets"),
			 Messages.getString("NewSimuDialog.user-defined") }; 
		String[] listTypeOrifice = { Messages.getString("NewSimuDialog.ideal"), Messages.getString("NewSimuDialog.commercial") };  //$NON-NLS-1$ //$NON-NLS-2$

			 
		if (simu != null) {
			//textarea
			properties.putAll(simu.getProperties());
			content = Messages.getString("simu.nbsim.name") + "\t: " + //$NON-NLS-1$ //$NON-NLS-2$
				properties.getProperty("simu.nbsim.value") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			content += (Messages.getString("simu.simopentaps.name") + //$NON-NLS-1$
			"\t: " + properties.getProperty("simu.simopentaps.value") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			content += Messages.getString("simu.typeorifice.name") + //$NON-NLS-1$
			"\t\t: ";
			
			//type orifice
			if (properties.getProperty("simu.typeorifice.value").equals("commercial")) { //$NON-NLS-1$ //$NON-NLS-2$
				content += listTypeOrifice[1] + "\n";
			} else {
				content += listTypeOrifice[0] + "\n"; 
			}
			
			content += Messages.getString("simu.typesimu.name") + "\t: ";
			
			//type simu
			if (properties.getProperty("simu.typesimu.value").equals("random")) { //$NON-NLS-1$ //$NON-NLS-2$
				content += listTypeSimu[0];
			} else if (properties.getProperty("simu.typesimu.value").equals("tapbytap")) { //$NON-NLS-1$ //$NON-NLS-2$
				content += listTypeSimu[1];
			} else {
				content += listTypeSimu[2];
			}

		}

		textAreaParam.setText(content);
		setResultsTable();
	}

	private void setResultsTable() {
		table.setVisible(true);

		if (simu != null) {
			switch (listTableau.getSelectedIndex()) {
			case 0:
				simuTableModel.setModel(SimuTableModel.TYPE_DESIGNDIFF);

				break;

			case 1:
				simuTableModel.setModel(SimuTableModel.TYPE_FLOWTAPS);

				break;

			case 2:
				simuTableModel.setModel(SimuTableModel.TYPE_QUARTILETAPS);

				break;

			case 3:
				simuTableModel.setModel(SimuTableModel.TYPE_SPEEDPIPE);

				break;

			case 4:
				simuTableModel.setModel(SimuTableModel.TYPE_NODEPRESSURE);

				break;

			default:
				simuTableModel.setModel(SimuTableModel.TYPE_NULL);

				break;
			}
		} else {
			table.setVisible(false);

			//table.getColumnModel().
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("NewSim")) { //$NON-NLS-1$
			//si le design n'est pas enregistrer, on averti l'utilisateur
			boolean isOk = false;

			if (!isDesignSaved()) {
				JOptionPane.showMessageDialog(null,
					Messages.getString("SimulationPane.You_have_to_save_this_design_in_order_to_run_a_simulation."), //$NON-NLS-1$
					Messages.getString("SimulationPane.Design_not_saved"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			} else {
				isOk = true;
			}

			if (isOk) {
				NewSimuDialog dialog = new NewSimuDialog(design, fileManager,
				design.getProperties());
				dialog.setVisible(true);

				if (dialog.runSimu) {
					runSimulation(dialog);
				}
				eatProperties("simu.simopentaps.value",dialog.getParameters());
				eatProperties("simu.typeorifice.value",dialog.getParameters());
				eatProperties("simu.typesimu.value",dialog.getParameters());
				eatProperties("simu.nbsim.value",dialog.getParameters());
				eatProperties("simu.targetflow.value",dialog.getParameters());
				eatProperties("simu.maxcriticalflow.value",dialog.getParameters());
				eatProperties("simu.mincriticalflow.value",dialog.getParameters());
				
			}
		}

		if (e.getActionCommand().equals("SelectSimu")) { //$NON-NLS-1$
			if (comboSimu.getSelectedIndex() > -1) {
				String name = comboSimu.getSelectedItem().toString().substring("simu ".length()); //$NON-NLS-1$
				simu = new Simulation(design.getName() + "." + name); //$NON-NLS-1$
				simu.setContent(fileManager.readFile(simu.getName(),
						Project.TYPE_SIMU));
				simuTableModel = new SimuTableModel(design, simu, table);
				table.setModel(simuTableModel);
				simuTableModel.fireTableDataChanged();
			} else {
				simu = null;
			}

			updateComponent();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		setResultsTable();
	}

	public boolean isDesignSaved() {
		String[] fdsg = fileManager.getListFile(Project.TYPE_DESIGN);
		boolean find = false;
		int i = 0;

		while ((i < fdsg.length) && (!find)) {
			find = fdsg[i].equals(design.getName());
			i++;
		}

		return find;
	}
    
	private void eatProperties(String n, Properties p){
		design.getProperties().setProperty(n,p.getProperty(n));    	
	}
}
