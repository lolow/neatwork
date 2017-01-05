package neatwork.gui;

import neatwork.Messages;

import neatwork.file.*;

import neatwork.gui.database.*;

import neatwork.gui.design.*;

import neatwork.gui.makedesign.*;

import neatwork.gui.topographie.*;

import neatwork.project.*;

import neatwork.solver.*;

import neatwork.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

/**
 * Classe qui donne une simple definition de la fenetre de neatwork
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class FrameNeatwork extends JFrame implements Observer, Runnable, ItemListener {

	private static final long serialVersionUID = -6973083343745891422L;
	public Properties properties = new Properties();
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenu databaseMenu;
	private JMenu topographyMenu;
	private JMenu designMenu;
	private JMenu langMenu;
	private JRadioButtonMenuItem menuItemSolverLocal;
	private AbstractFileManager fileManager;
	private Database database;

	// composants
	private FileManagerDialog dialog;
	private AboutDialog aboutDialog;
	private UnitDialog unitDialog;
	private ProjectManager projectManager;
	private ProjectPane projectPane;
	private DatabaseDialog databaseDialog;
	private Action exitAction;
	private Action newTopoAction;
	private Action openTopoAction;
	private Action openDesignAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action deleteTopoAction;
	private Action deleteDesignAction;
	private Action closeAction;
	private Action closeAllAction;
	private Action aboutAction;
	private Action unitAction;
	private Action databaseAction;
	private Action makeDesignAction;
	private Action quickCheckAction;
	private Action topoStatAction;
	private Action designParameterAction;
	private Action designExtractTopoAction;
	private Action designLoadFactorAction;
	private Action reportTopoAction;
	private Action reportDesignAction;

	// Loclization
	private String[] langList = { Messages.getString("FrameNeatwork.English"),
			Messages.getString("FrameNeatwork.Spanish"), Messages.getString("FrameNeatwork.French") }; //$NON-NLS-2$
	private String[] langcode = { Locale.ENGLISH.getLanguage(), "sp", Locale.FRENCH.getLanguage() }; // $NON-NLS-3$

	public FrameNeatwork(Properties properties) {
		super(properties.getProperty("appli.name", "NeatWork"));
		this.properties = properties;
		setSize(750, 550);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		defineAction();
		defineMenu();

		// charge les managers en memoire
		if (properties.getProperty("file.distant").equals("true")) {
			fileManager = new FileManagerClient(properties);
		} else {
			fileManager = new FileManagerDisk(properties);
		}

		dialog = new FileManagerDialog(this, fileManager, properties);
		projectManager = new ProjectManager();
		aboutDialog = new AboutDialog(this, properties);
		unitDialog = new UnitDialog(this, properties);
		projectManager.addObserver(this);
		database = new Database(fileManager, properties);
		database.addObserver(this);
		databaseDialog = new DatabaseDialog(this, database);

		// dessine les composants
		Container content = getContentPane();
		projectPane = new ProjectPane(projectManager, fileManager, properties, database);
		content.add(projectPane);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitApplication();
			}
		});

		// Affiche la fenetre
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
		setVisible(true);
	}

	/** definit les actions */
	private void defineAction() {
		String pathImg = "/neatwork/gui/images/";

		// create Action Exit
		Icon exitIcon = null;
		exitAction = new NeatworkAction(Messages.getString("FrameNeatwork.Exit"), exitIcon,
				Messages.getString("FrameNeatwork.Exit_Application"), 'X') {

			private static final long serialVersionUID = 526605385280572705L;

			public void actionPerformed(ActionEvent e) {
				exitApplication();
			}
		};

		// create Action New Topo
		Icon newTopoIcon = null;
		newTopoAction = new NeatworkAction(Messages.getString("FrameNeatwork.New_Topography..."), newTopoIcon,
				Messages.getString("FrameNeatwork.New_Topography"), 'N') {

			private static final long serialVersionUID = -395938043005175231L;

			public void actionPerformed(ActionEvent e) {
				dialog.show(FileManagerDialog.TYPEDIALOG_NEW, Project.TYPE_TOPO);

				if (!dialog.getCancelled()) {
					Project project = new Topographie(dialog.getFileName(), dialog.getContent(), properties);
					projectManager.addProject(project);
				}
			}
		};

		// create Action Open Topo
		Icon openTopoIcon = null;
		openTopoAction = new NeatworkAction(Messages.getString("FrameNeatwork.Open_Topography..."), openTopoIcon,
				Messages.getString("FrameNeatwork.Open_Topography"), 'T') {

			private static final long serialVersionUID = 9031161505527837056L;

			public void actionPerformed(ActionEvent e) {
				dialog.show(FileManagerDialog.TYPEDIALOG_OPEN, Project.TYPE_TOPO);

				if (!dialog.getCancelled()) {
					Project project = new Topographie(dialog.getFileName(), dialog.getContent(), properties);
					project.setName(projectManager.isAlreadyLoaded(project));
					projectManager.addProject(project);
				}
			}
		};

		// create Action Open Design
		Icon openDesignIcon = null;
		openDesignAction = new NeatworkAction(Messages.getString("FrameNeatwork.Open_Design..."), openDesignIcon,
				Messages.getString("FrameNeatwork.Open_Design"), 'T') {

			private static final long serialVersionUID = -6110165524249615340L;

			public void actionPerformed(ActionEvent e) {
				dialog.show(FileManagerDialog.TYPEDIALOG_OPEN, Project.TYPE_DESIGN);

				if (!dialog.getCancelled()) {
					Project project = new Design(dialog.getFileName(), dialog.getContent(), properties);
					project.setName(projectManager.isAlreadyLoaded(project));
					projectManager.addProject(project);
					((Design) project).refreshDesign(FrameNeatwork.this.database);
				}
			}
		};

		// create Action Save
		Icon saveIcon = null;
		saveAction = new NeatworkAction(Messages.getString("FrameNeatwork.Save"), saveIcon,
				Messages.getString("FrameNeatwork.Save"), 'S') {

			private static final long serialVersionUID = 5857926935768103116L;

			public void actionPerformed(ActionEvent e) {
				Project project = projectManager.getCurrentProject();
				String newname = fileManager.getFirstNameCompatible(project);

				if (newname.equals(project.getName())) {
					saveAsAction.actionPerformed(null);
				} else {
					fileManager.writeFile(projectManager.getCurrentProject());
				}
			}
		};

		// create Action Save As
		Icon saveAsIcon = null;
		saveAsAction = new NeatworkAction(Messages.getString("FrameNeatwork.Save_as..."), saveAsIcon,
				Messages.getString("FrameNeatwork.Save_as"), 'A') {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1436146608278045870L;

			public void actionPerformed(ActionEvent e) {
				Project project = projectManager.getCurrentProject();
				dialog.setFileName(project.getName());
				dialog.setContent(project.getContent());
				dialog.show(FileManagerDialog.TYPEDIALOG_SAVE, projectManager.getCurrentProject().getType());

				if (!dialog.getCancelled()) {
					// fileManager.deleteFile(project.getName(),
					// project.getType());
					project.setName(dialog.getFileName());
					projectManager.setCurrentProject(project);
				}
			}
		};

		// create Action Close
		Icon closeIcon = null;
		closeAction = new NeatworkAction(Messages.getString("FrameNeatwork.Close"), closeIcon,
				Messages.getString("FrameNeatwork.Close_this_file"), 'C') {

			private static final long serialVersionUID = -5826745459685218514L;

			public void actionPerformed(ActionEvent e) {
				Object[] options = { Messages.getString("FrameNeatwork.Yes"), Messages.getString("FrameNeatwork.No") };

				if (JOptionPane.showOptionDialog(null,
						Messages.getString("FrameNeatwork.Do_you_want_to_close_the_file")
								+ projectManager.getCurrentProject().getName() + "?",
						Messages.getString("FrameNeatwork.Close_File"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[0]) == JOptionPane.YES_OPTION) {
					projectManager.removeCurrentProject();
				}
			}
		};

		// create Action Close All
		Icon closeAllIcon = null;
		closeAllAction = new NeatworkAction(Messages.getString("FrameNeatwork.Close_All"), closeAllIcon,
				Messages.getString("FrameNeatwork.Close_all_files"), 'A') {

			private static final long serialVersionUID = -4352582885086698773L;

			public void actionPerformed(ActionEvent e) {
				Object[] options = { Messages.getString("FrameNeatwork.Yes"), Messages.getString("FrameNeatwork.No") };

				if (JOptionPane.showOptionDialog(null,
						Messages.getString("FrameNeatwork.Do_you_want_to_close_all_the_files"),
						Messages.getString("FrameNeatwork.Close_All_The_Files"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[0]) == JOptionPane.YES_OPTION) {
					int n = projectManager.getNbProject();

					for (int i = 0; i < n; i++) {
						projectManager.removeCurrentProject();
					}
				}
			}
		};

		// create Action Delete Topo
		Icon deleteTopoIcon = null;
		deleteTopoAction = new NeatworkAction(Messages.getString("FrameNeatwork.Delete_a_Topography..."),
				deleteTopoIcon, Messages.getString("FrameNeatwork.Delete_a_Topography"), 'T') {

			private static final long serialVersionUID = -2291449089176157754L;

			public void actionPerformed(ActionEvent e) {
				dialog.setFileName("");
				dialog.show(FileManagerDialog.TYPEDIALOG_DELETE, Project.TYPE_TOPO);
			}
		};

		// create Action Delete Design
		Icon deleteDesignIcon = null;
		deleteDesignAction = new NeatworkAction(Messages.getString("FrameNeatwork.Delete_a_Design..."),
				deleteDesignIcon, Messages.getString("FrameNeatwork.Delete_a_Design"), 'D') {

			private static final long serialVersionUID = -242765195609305778L;

			public void actionPerformed(ActionEvent e) {
				dialog.setFileName("");
				dialog.show(FileManagerDialog.TYPEDIALOG_DELETE, Project.TYPE_DESIGN);
			}
		};

		// create Action About
		Icon aboutIcon = new ImageIcon(getClass().getResource(pathImg + "Inform.png"));
		;
		aboutAction = new NeatworkAction(Messages.getString("FrameNeatwork.About"), aboutIcon,
				Messages.getString("FrameNeatwork.About_Neatwork"), 'A') {

			private static final long serialVersionUID = 93141884949499253L;

			public void actionPerformed(ActionEvent e) {
				aboutDialog.setVisible(true);
			}
		};

		// create Action Unit
		Icon unitIcon = null;
		unitAction = new NeatworkAction(Messages.getString("FrameNeatwork.Units"), unitIcon,
				Messages.getString("FrameNeatwork.Units"), 'A') {

			private static final long serialVersionUID = 5106700239272163118L;

			public void actionPerformed(ActionEvent e) {
				unitDialog.setVisible(true);
			}
		};

		// create Action DataBase
		Icon databaseIcon = new ImageIcon(getClass().getResource(pathImg + "Cylinder.png"));
		;
		databaseAction = new NeatworkAction(Messages.getString("FrameNeatwork.Edit_database..."), databaseIcon,
				Messages.getString("FrameNeatwork.Edit_database..."), 'B') {

			private static final long serialVersionUID = 4212789200941010674L;

			public void actionPerformed(ActionEvent e) {
				databaseDialog.setVisible(true);
			}
		};

		// create MakeDesign Action
		Icon makeDesignIcon = null;
		makeDesignAction = new NeatworkAction(Messages.getString("FrameNeatwork.Make_Design..."), makeDesignIcon,
				Messages.getString("FrameNeatwork.Make_a_design_from_your_current_topo"), 'K') {

			private static final long serialVersionUID = -994579565342754977L;

			public void actionPerformed(ActionEvent e) {
				Topographie topo = (Topographie) projectManager.getCurrentProject();
				topo.setContent(topo.getContent());

				if (!topo.isATree()) {
					JOptionPane.showMessageDialog(FrameNeatwork.this,
							Messages.getString("FrameNeatwork.The_topography_must_be_a_tree__!"),
							Messages.getString("FrameNeatwork.Not_a_tree"), JOptionPane.INFORMATION_MESSAGE);
				} else if (!(topo.getMinLength() > 0)) {
					JOptionPane.showMessageDialog(FrameNeatwork.this,
							Messages.getString("FrameNeatwork.All_pipe_lengths_must_be_greater_than_0__!"),
							Messages.getString("FrameNeatwork.Bad_length"), JOptionPane.INFORMATION_MESSAGE);
				} else {
					MakeDesignDialog dialog = new MakeDesignDialog(FrameNeatwork.this, database, topo);
					topo.setHauteurSource(dialog.getHSource());
					dialog.setVisible(true);

					if (!dialog.getCanceled()) {
						runMakeDesign(dialog);
					}
				}
			}
		};

		// create InformationTopo Action
		Icon topoStatIcon = null;
		topoStatAction = new NeatworkAction(Messages.getString("FrameNeatwork.Network_summary..."), topoStatIcon,
				Messages.getString("FrameNeatwork.Network_summary"), 'U') {

			private static final long serialVersionUID = 6896673256851773978L;

			public void actionPerformed(ActionEvent e) {
				Topographie topo = (Topographie) projectManager.getCurrentProject();
				TopoStatDialog dialog = new TopoStatDialog(topo, FrameNeatwork.this);
				dialog.setVisible(true);
			}
		};

		// create ReportTopo Action
		Icon reportopoIcon = null;
		reportTopoAction = new NeatworkAction(Messages.getString("FrameNeatwork.Report_in_HTML..."), reportopoIcon, "",
				'H') {

			private static final long serialVersionUID = -3874561978926006687L;

			public void actionPerformed(ActionEvent e) {
				Topographie topo = (Topographie) projectManager.getCurrentProject();
				String s = "<html><head><title>";
				s += Messages.getString("FrameNeatwork.Topography_report");
				s += "</title></head><body>";
				s += ("<h1>" + Messages.getString("FrameNeatwork.Topography_report0") + topo.getName() + "</h1>");
				s += ("<h2>" + Messages.getString("FrameNeatwork.Node_List") + "</h2>"); // $NON-NLS-3$
				s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
				s += Messages.getString(
						"FrameNeatwork.<tr><th>ID</th><th>Height</th><th>X</th><th>Y</th><th>Faucets</th><th>Nature</th></tr>");

				for (Iterator en = topo.getNodeIterator(); en.hasNext();) {
					Node n = (Node) en.next();
					s += "<tr>";
					s += ("<td>" + n.getName() + "</td>");
					s += ("<td>" + n.getHeight() + "</td>");
					s += ("<td>" + n.getCoordX() + "</td>");
					s += ("<td>" + n.getCoordY() + "</td>");
					s += ("<td>" + n.getNbTaps() + "</td>");
					s += ("<td>" + Node.getNameType(n.getType()) + "</td>");
					s += "</tr>";
				}

				s += "</table>";
				s += ("<h2>" + Messages.getString("FrameNeatwork.Arc_List") + "</h2>"); // $NON-NLS-3$
				s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
				s += Messages.getString("FrameNeatwork.<tr><th>Begin</th><th>End</th><th>Length</th></tr>");

				for (Iterator en = topo.getPipeIterator(); en.hasNext();) {
					Pipe n = (Pipe) en.next();
					s += "<tr>";
					s += ("<td>" + n.getBegin() + "</td>");
					s += ("<td>" + n.getEnd() + "</td>");
					s += ("<td>" + n.getLength() + "</td>");
					s += "</tr>";
				}

				s += "</table>";
				s += ("<h2>" + Messages.getString("FrameNeatwork.Summary") + Messages.getString("FrameNeatwork.</h2>")); // $NON-NLS-3$
				s += (Messages.getString("FrameNeatwork.<B>Number_of_Nodes__</B>") + topo.getNbNodes() + "<UL>"
						+ Messages.getString("FrameNeatwork.<LI>_<i>Branching_nodes__</i>")
						+ topo.getNbNodes(Node.TYPE_DISPATCH)
						+ Messages.getString("FrameNeatwork.<LI>_<i>Faucet_nodes__</i>")
						+ topo.getNbNodes(Node.TYPE_FAUCET) + Messages.getString("FrameNeatwork._(with__")
						+ topo.getNbTotalTaps() + Messages.getString("FrameNeatwork._individual_faucets)_") + "</UL>"
						+ Messages.getString("FrameNeatwork.<B>Total_height_change__</B>")
						+ Tools.doubleFormat("#", topo.getTotalHeightChange()) + " m<BR><BR>"
						+ Messages.getString("FrameNeatwork.<B>Number_of_Pipes__</B>") + topo.getNbPipes() + " <BR>"
						+ Messages.getString("FrameNeatwork.<B>Total_length__</B>")
						+ Tools.doubleFormat("#", topo.getTotalLength()) + " m ");
				s += ("<p>" + new Date().toString() + "</p>");
				Tools.enregFich(s);
			}
		};

		// create QuickCheck Action
		Icon quickCheckIcon = null;
		quickCheckAction = new NeatworkAction(Messages.getString("FrameNeatwork.Quick_Check..."), quickCheckIcon,
				Messages.getString("FrameNeatwork.Quick_faucets_check"), 'U') {
			public void actionPerformed(ActionEvent e) {
				Topographie topo = (Topographie) projectManager.getCurrentProject();
				QuickCheckDialog dialog = new QuickCheckDialog(FrameNeatwork.this, topo);
				dialog.setVisible(true);
			}
		};

		// create designParameter Action
		Icon designParameterIcon = null;
		designParameterAction = new NeatworkAction(Messages.getString("FrameNeatwork.Design_Parameters..."),
				designParameterIcon, Messages.getString("FrameNeatwork.Design_Parameters"), 'U') {
			public void actionPerformed(ActionEvent e) {
				Design design = (Design) projectManager.getCurrentProject();
				DesignParaDialog dialog = new DesignParaDialog(FrameNeatwork.this, design);
				dialog.setVisible(true);
			}
		};

		// create designExtractTopo Action
		designExtractTopoAction = new NeatworkAction(Messages.getString("FrameNeatwork.Extract_topography"),
				designParameterIcon, Messages.getString("FrameNeatwork.Extract_topography"), 'U') {
			public void actionPerformed(ActionEvent e) {
				Design design = (Design) projectManager.getCurrentProject();

				if (design.isATree()) {
					Project project = new Topographie(design.getName(), design.extractTopoContent(), properties);
					project.setName(projectManager.isAlreadyLoaded(project));
					projectManager.addProject(project);
				} else {
					JOptionPane.showConfirmDialog(null,
							Messages.getString("FrameNeatwork.This_feature_is_not_valid_for_non-tree_design"));
				}
			}
		};

		// create ReportDesign Action
		Icon repordesignIcon = null;
		reportDesignAction = new NeatworkAction(Messages.getString("FrameNeatwork.Report_in_HTML..."), repordesignIcon,
				"", 'H') {
			public void actionPerformed(ActionEvent e) {
				Design dsg = (Design) projectManager.getCurrentProject();
				String s = "<html><head><title>";
				s += Messages.getString("FrameNeatwork.Design_report");
				s += "</title></head><body>";
				s += ("<h1>" + Messages.getString("FrameNeatwork.Design_report0") + dsg.getName() + "</h1>");
				s += ("<h2>" + Messages.getString("FrameNeatwork.Node_List") + "</h2>"); // $NON-NLS-3$
				s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
				s += Messages.getString(
						"FrameNeatwork.<tr><th>ID</th><th>Height</th><th>X</th><th>Y</th><th>Ideal_Orifice</th><th>Commercial_Orifice</th><th>Nature</th></tr>");

				for (Iterator en = dsg.getNodeIterator(); en.hasNext();) {
					Node n = (Node) en.next();
					s += "<tr>";
					s += ("<td>" + n.getName() + "</td>");
					s += ("<td>" + n.getHeight() + "</td>");
					s += ("<td>" + n.getCoordX() + "</td>");
					s += ("<td>" + n.getCoordY() + "</td>");
					s += ("<td>" + n.getOrifice() + "</td>");
					s += ("<td>" + n.getComercialOrifice() + "</td>");
					s += ("<td>" + Node.getNameType(n.getType()) + "</td>");
					s += "</tr>";
				}

				s += "</table>";
				s += ("<h2>" + Messages.getString("FrameNeatwork.Arc_List") + "</h2>"); // $NON-NLS-3$
				s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
				s += Messages.getString(
						"FrameNeatwork.<tr><th>Begin</th><th>End</th><th>Length</th><th>Length1</th><th>Diam1</th><th>Length2</th><th>Diam2</th></tr>");

				for (Iterator en = dsg.getPipeIterator(); en.hasNext();) {
					Pipe n = (Pipe) en.next();
					s += "<tr>";
					s += ("<td>" + n.getBegin() + "</td>");
					s += ("<td>" + n.getEnd() + "</td>");
					s += ("<td>" + n.getLength() + "</td>");
					s += ("<td>" + n.getLength1() + "</td>");
					s += ("<td>" + n.getRefDiam1() + "</td>");
					s += ("<td>" + n.getLength2() + "</td>");
					s += ("<td>" + n.getRefDiam2() + "</td>");
					s += "</tr>";
				}

				s += "</table>";

				Hashtable usedlength = dsg.getSummary();
				s += ("<h2>" + Messages.getString("FrameNeatwork.Diameter_references") + "</h2>"); // $NON-NLS-3$
				s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
				s += (Messages.getString(
						"FrameNeatwork.<tr><th>Ref</th><th>Nominal</th><th>SDR</th><th>Internal_Diameter</th><th>Unit_cost</th>")
						+ Messages.getString("FrameNeatwork.<th>Max_Pressure</th><th>Type</th><th>Roughness</th>")
						+ Messages.getString("FrameNeatwork.<th>Total_length</th><th>Total_cost</th></tr>"));

				Vector v = new Vector(dsg.getDiamTable().keySet());
				Collections.sort(v);

				for (Enumeration en = v.elements(); en.hasMoreElements();) {
					String k = en.nextElement().toString();
					Diameter d = (Diameter) dsg.getDiamTable().get(k);
					double l = ((usedlength.get(k) == null) ? 0 : Double.parseDouble(usedlength.get(k).toString()));
					s += "<tr>";
					s += ("<td>" + k + "</td>");
					s += ("<td>" + d.getNominal() + "</td>");
					s += ("<td>" + d.getSdr() + "</td>");
					s += ("<td>" + d.getDiameter() + "</td>");
					s += ("<td>" + Tools.doubleFormat("0.##", d.getCost()) + "</td>");
					s += ("<td>" + d.getMaxLength() + "</td>");
					s += ("<td>" + d.getType() + "</td>");
					s += ("<td>" + d.getRoughness() + "</td>");
					s += ("<td>" + l + "</td>");
					s += ("<td>" + (l * d.getCost()) + "</td>");
					s += "</tr>";
				}

				s += "</table>";

				if (dsg.getOrifices().size() > 0) {
					s += ("<h2>" + Messages.getString("FrameNeatwork.Available_orifices") + "</h2>"); // $NON-NLS-3$
					s += "<table BORDER CELLPADDING=0 CELLSPACING=0>";
					s += Messages.getString("FrameNeatwork.<tr><th>Diameter</th></tr>");

					for (Enumeration en = dsg.getOrifices().elements(); en.hasMoreElements();) {
						s += "<tr>";
						s += ("<td>" + en.nextElement().toString() + "</td>");
						s += "</tr>";
					}

					s += "</table>";
				}

				// s += dsg.getSummary();
				s += ("<h2>" + Messages.getString("FrameNeatwork.Summary") + "</h2>"); // $NON-NLS-3$
				s += ("<h3>" + Messages.getString("FrameNeatwork.Project_cost") + Tools.doubleFormat("0", dsg.getCost())
						+ "</h3>");

				Properties p = dsg.getProperties();
				s += ("<h3>" + Messages.getString("FrameNeatwork.Global_Parameter") + "</h3>"); // $NON-NLS-3$
				s += (Messages.getString("topo.watertemp.name") + " : " + p.getProperty("topo.watertemp.value")
						+ "<br>");
				s += (Messages.getString("topo.pipelength.name") + " : " + p.getProperty("topo.pipelength.value")
						+ "<br>");
				s += ("<h3>" + "Design Parameter" + "</h3>"); //$NON-NLS-3$
				s += (Messages.getString("topo.opentaps.name") + " : " + p.getProperty("topo.opentaps.value") + "<br>");
				s += (Messages.getString("topo.servicequal.name") + " : " + p.getProperty("topo.servicequal.value")
						+ "<br>");
				s += (Messages.getString("topo.targetflow.name") + " : " + p.getProperty("topo.targetflow.value")
						+ "<br>");
				s += (Messages.getString("topo.limitbudget.name") + " : " + p.getProperty("topo.limitbudget.value")
						+ "<br>");
				s += ("<h3>" + Messages.getString("FrameNeatwork.Advanced_Parameter") + "</h3>"); // $NON-NLS-3$
				s += (Messages.getString("topo.orifcoef.name") + " : " + p.getProperty("topo.orifcoef.value") + "<br>");
				s += (Messages.getString("topo.faucetcoef.name") + " : " + p.getProperty("topo.faucetcoef.value")
						+ "<br>");
				s += ("<h3>" + Messages.getString("FrameNeatwork.Structure") + "</h3>"); // $NON-NLS-3$
				s += (Messages.getString("FrameNeatwork.<B>Number_of_Nodes__</B>") + dsg.getNbNodes()
						+ Messages.getString("FrameNeatwork.<UL>_270")
						+ Messages.getString("FrameNeatwork.<LI>_<i>Branching_nodes__</i>")
						+ dsg.getNbNodes(Node.TYPE_DISPATCH)
						+ Messages.getString("FrameNeatwork.<LI>_<i>Faucet_nodes__</i>")
						+ dsg.getNbNodes(Node.TYPE_FAUCET) + Messages.getString("FrameNeatwork._(with")
						+ dsg.getNbTotalTaps() + " " + Messages.getString("FrameNeatwork._individual_faucets)")
						+ "</UL>" + Messages.getString("FrameNeatwork.<B>Total_height_change__</B>")
						+ Tools.doubleFormat("#", dsg.getTotalHeightChange()) + " m<BR><BR>"
						+ Messages.getString("FrameNeatwork.<B>Number_of_Pipes__</B>") + dsg.getNbPipes() + " <BR>"
						+ Messages.getString("FrameNeatwork.<B>Total_length__</B>")
						+ Tools.doubleFormat("#", dsg.getTotalLength()) + " m ");
				s += ("<p>" + new Date().toString() + "</p>");
				Tools.enregFich(s);
			}
		};

		designLoadFactorAction = new NeatworkAction(Messages.getString("FrameNeatwork.Design_Load_Factors..."),
				designParameterIcon, Messages.getString("FrameNeatwork.Design_Load_Factors"), 'U') {

			private static final long serialVersionUID = 1338453491121668634L;

			public void actionPerformed(ActionEvent e) {
				Design design = (Design) projectManager.getCurrentProject();
				JDialog dialog = new DesignLoadFactorDialog(FrameNeatwork.this, design);
				dialog.setVisible(true);
			}
		};
	}

	/** definit les menus */
	private void defineMenu() {
		boolean standalone = properties.getProperty("appli.standalone").equals("true");

		// Barre de menu
		menuBar = new JMenuBar();

		// Menu File
		fileMenu = new JMenu(Messages.getString("FrameNeatwork.File"));
		fileMenu.setMnemonic('F');
		fileMenu.add(newTopoAction).setIcon(null);
		fileMenu.addSeparator();
		fileMenu.add(openTopoAction).setIcon(null);
		fileMenu.add(openDesignAction).setIcon(null);
		fileMenu.addSeparator();
		fileMenu.add(closeAction).setIcon(null);
		fileMenu.add(closeAllAction).setIcon(null);
		fileMenu.addSeparator();
		fileMenu.add(saveAction).setIcon(null);
		fileMenu.add(saveAsAction).setIcon(null);
		fileMenu.addSeparator();
		fileMenu.add(deleteTopoAction).setIcon(null);
		fileMenu.add(deleteDesignAction).setIcon(null);
		fileMenu.addSeparator();
		fileMenu.add(exitAction).setIcon(null);

		if (!standalone) {
			exitAction.putValue(Action.SHORT_DESCRIPTION, Messages.getString("FrameNeatwork.Hide"));
		}

		saveAsAction.setEnabled(false);
		saveAction.setEnabled(false);
		closeAction.setEnabled(false);

		// topographie menu
		topographyMenu = new JMenu(Messages.getString("FrameNeatwork.Topography"));
		topographyMenu.setVisible(false);
		topographyMenu.setMnemonic('T');
		topographyMenu.add(quickCheckAction).setIcon(null);
		topographyMenu.add(topoStatAction).setIcon(null);

		if (standalone) {
			topographyMenu.add(reportTopoAction).setIcon(null);
		}

		topographyMenu.addSeparator();
		topographyMenu.add(makeDesignAction).setIcon(null);

		// design menu
		designMenu = new JMenu(Messages.getString("FrameNeatwork.Design"));
		designMenu.setVisible(false);
		designMenu.setMnemonic('D');
		designMenu.add(designExtractTopoAction).setIcon(null);

		if (standalone) {
			designMenu.add(reportDesignAction).setIcon(null);
		}

		designMenu.addSeparator();
		designMenu.add(designParameterAction).setIcon(null);
		designMenu.add(designLoadFactorAction).setIcon(null);

		// database Menu
		databaseMenu = new JMenu(Messages.getString("FrameNeatwork.Database"));
		databaseMenu.setMnemonic('O');
		databaseMenu.add(databaseAction).setIcon(null);

		// Menu Language
		langMenu = new JMenu(Messages.getString("FrameNeatwork.Language"));

		ButtonGroup group = new ButtonGroup();
		JMenuItem[] langItem = new JRadioButtonMenuItem[langList.length];

		for (int i = 0; i < langItem.length; i++) {
			langItem[i] = new JRadioButtonMenuItem();

			Action a = new LocaleAction(langcode[i], this);
			langItem[i].setAction(a);
			a.putValue(Action.NAME, langList[i]);
			langItem[i].setSelected(false);
			group.add(langItem[i]);

			if (properties.getProperty("appli.locale", "null").equals(langcode[i])) {
				langItem[i].setSelected(true);
			}

			langMenu.add(langItem[i]);
		}

		// Menu Help
		helpMenu = new JMenu(Messages.getString("FrameNeatwork.Help"));
		helpMenu.setMnemonic('H');
		helpMenu.add(unitAction);
		helpMenu.add(langMenu);
		helpMenu.add(aboutAction);

		menuBar.add(fileMenu);
		menuBar.add(topographyMenu);
		menuBar.add(designMenu);
		menuBar.add(databaseMenu);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
	}

	/** quitte l'application */
	private void exitApplication() {
		boolean allFramesClosed = false;

		if (JOptionPane.showConfirmDialog(null, Messages.getString("FrameNeatwork.Do_you_really_want_to_quit"),
				Messages.getString("FrameNeatwork.Confirmation_Dialog"),
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			allFramesClosed = true;
		}

		if (allFramesClosed) {
			if (properties.getProperty("appli.standalone").equals("true")) {
				System.exit(0);
			} else {
				this.setVisible(false);
			}
		}
	}

	/** execute le make design */
	public void runMakeDesign(MakeDesignDialog dialog) {
		AbstractSolver solver = null;

		if (properties.getProperty("solver.distant").equals("true")) {
			solver = new SolverClient(properties);
		} else {
			solver = new SolverDisk();
		}

		solver.init();

		// lance le thread
		ThreadMakeDesign thread = new ThreadMakeDesign(dialog, solver);

		// affiche la barre de progression
		SolverProgressDialog solverDialog = new SolverProgressDialog(solver, thread);
		solverDialog.setVisible(true);

		// verifie si le probleme est realisable
		Vector resultNodePressure = solver.getNodePressureMakeDesign();

		boolean isOk = false;

		if (resultNodePressure != null) {
			Vector line = (Vector) resultNodePressure.get(1);

			try {
				new Double(line.get(2).toString());
				isOk = true;
			} catch (NumberFormatException e) {
			}
		}

		if (!isOk) {
			JOptionPane.showMessageDialog(this, Messages.getString("FrameNeatwork.there_is_no_feasible_solution__!"),
					Messages.getString("FrameNeatwork.Optimization_warning"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			// affiche les informations de pressions
			NodeTapPressureDialog dialog2 = new NodeTapPressureDialog(resultNodePressure, this);
			dialog2.setVisible(true);

			// recupere le design
			Design design = new Design(dialog.getTopographie().getName(), solver.getDesignContentMakeDesign(),
					properties);

			design.setName(fileManager.getFirstNameCompatible(design));
			design.setName(projectManager.isAlreadyLoaded(design));
			design.setProperties(dialog.getTopographie().getProperties());
			design.setLoadFactorTable(dialog.getLoadFactors());
			projectManager.addProject(design);
		}
	}

	/** gere les interactions */
	public void update(Observable observable, Object param) {
		// ProjectManager
		if (observable.getClass().isInstance(projectManager)) {
			int choix = ((Integer) param).intValue();

			switch (choix) {
			case ProjectManager.MODIF_SETINDEX:
				saveAction.setEnabled(projectManager.getIndex() > -1);
				saveAsAction.setEnabled(projectManager.getIndex() > -1);
				closeAction.setEnabled(projectManager.getNbProject() > 0);
				closeAllAction.setEnabled(projectManager.getNbProject() > 1);

				Project p = projectManager.getCurrentProject();

				if (p != null) {
					// topo
					makeDesignAction.setEnabled(p.getType() == Project.TYPE_TOPO);
					quickCheckAction.setEnabled(p.getType() == Project.TYPE_TOPO);
					topographyMenu.setVisible(p.getType() == Project.TYPE_TOPO);

					// design
					designParameterAction.setEnabled(p.getType() == Project.TYPE_DESIGN);
					designMenu.setVisible(p.getType() == Project.TYPE_DESIGN);
				}
			}
		}

		// Database
		if (observable.getClass().isInstance(database)) {
			int choix = ((Integer) param).intValue();

			switch (choix) {
			case Database.MODIF_DIAMETER:

				Iterator<Project> iter = projectManager.getProjectIterator();

				while (iter.hasNext()) {
					Project item = (Project) iter.next();

					if (item.getType() == Project.TYPE_DESIGN) {
						((Design) item).refreshDesign(database);
					}
				}
			}
		}
	}

	// LANCE LES THREADS
	public void run() {
	}

	public void itemStateChanged(ItemEvent e) {
		if ((e.getItem().equals(menuItemSolverLocal)) || (e.getItem().equals(menuItemSolverLocal))) {
			properties.setProperty("solver.distant", ((menuItemSolverLocal.isSelected()) ? "false" : "true"));
		}
	}
}
