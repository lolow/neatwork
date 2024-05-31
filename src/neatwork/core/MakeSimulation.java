package neatwork.core;

import neatwork.*;

import neatwork.core.defs.*;

import neatwork.core.run.*;

import neatwork.solver.*;

import neatwork.utils.*;

import java.util.*;

/**
 * Classe qui execute la simulation
 * <p>
 * voici les param\u00E8tres du make design : <br>
 * - <i>typesimu</i> = type de simulation (="random" ou "tapbytap" ou
 * "handmade").<br>
 * - <i>typeorifice</i> = type d'orifice utilise (="ideal" ou "commercial").<br>
 * - <i>nbsimu</i> = nombre de simulation.<br>
 * - <i>minoutflow</i> = flot minimum en sortie<br>
 * - <i>simopentaps</i> = fraction de robinets ouverts durant la simu <br>
 * - <i>mincriticalflow</i> = flot minimal critique (pour les resultats)<br>
 * - <i>maxcriticalflow</i> = flot maximal critique (pour les resultats)<br>
 * - <i>alpha</i> = coefficient de calcul (a ne pas redefinir) <br>
 * - <i>coefforifice</i> = coefficient des orifices <br>
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class MakeSimulation {
	private CoreDesign dsg;
	private Properties properties;

	public MakeSimulation(CoreDesign design, DiametersVector dvector, OrificesVector ovector, Properties prop,
			Hashtable faucetRef, AbstractSolver solver) {
		this.dsg = design;
		this.properties = prop;

		// extraction des properties
		String typeOrifice = prop.getProperty("simu.typeorifice.value", "ideal");
		String typeSimulation = prop.getProperty("simu.typesimu.value", "random");
		int nbSim = Integer.parseInt(prop.getProperty("simu.nbsim.value", "10"));
		double outflow = Double.parseDouble(prop.getProperty("topo.targetflow.value", "0.2")) / 1000;
		double alpha = Double.parseDouble(prop.getProperty("topo.faucetcoef.value", "0.00000002"));
		double rate1 = 1;

		try {
			rate1 = Double.parseDouble(prop.getProperty("simu.simopentaps.value", "0.4"));
		} catch (NumberFormatException ex) {
		}

		double seuil = Double.parseDouble(prop.getProperty("simu.mincriticalflow.value", "0.1"));
		double seuil2 = Double.parseDouble(prop.getProperty("simu.maxcriticalflow.value", "0.3"));
		double coeffOrifice = Double.parseDouble(prop.getProperty("topo.orifcoef.value", "0.59"));

		double lbd = ((Pipes) dsg.pvector.elementAt(0)).p1;
		double invest = Double.parseDouble(prop.getProperty("topo.limitbudget.value", "500"));
		int maxiter = 20;
		double tolr = 1e-2;
		double tolx = 1e-6;
		double cible = Double.parseDouble(prop.getProperty("simu.targetflow.value", "1.2e-4"));
		double[] nbouvert = new double[0];
		// double opentaps = 0.9;

		// ajout de de noeuds intermediaires pour chaque branche possedant deux
		// tuyaux differents
		//ajoutNodes();

		// si le reseau contient des boucles, on ajoute des branches inverses
		if (!isTree()) {
			addInversBranch();
		}

		// type d'orifice
		if (typeOrifice.equals("ideal")) {

			for (int i = 0; i < dsg.tvector.size(); i++) {
				Taps taps = (Taps) dsg.tvector.get(i);
				taps.orifice = taps.orif_ideal;
			}
		} else {
			for (int i = 0; i < dsg.tvector.size(); i++) {
				Taps taps = (Taps) dsg.tvector.get(i);
				taps.orifice = taps.orif_com;
			}
		}

		dvector.size();
		dsg.pvector.size();
		dsg.nvector.size();
		dsg.nvector.size();
		dsg.pvector.size();

		// Initialise le vecteur de flot
		double[] F = new double[dsg.tvector.size() + (dsg.pvector.size() * 2) + 1];

		// affectation des alpha
		for (int k = 0; k < dsg.tvector.size(); k++) {
			((Taps) dsg.tvector.get(k)).faucetCoef = alpha;
		}

		// simulation au hasard
		if (typeSimulation.equals("random")) {

			// stat a zero
			dsg.pvector.initializeSimulation(nbSim);
			dsg.nvector.initializeSimulation(nbSim);

			int i;
			// resolution
			for (i = 0; i < nbSim; i++) {
				solver.setProgress(Math.round((float) i / nbSim * 100));

				new RunSimulation(F, dsg.nvector, dsg.pvector, dsg.tvector, outflow, rate1, seuil, seuil2,
						typeSimulation, i, coeffOrifice,null, lbd, invest,
						maxiter, tolr, tolx, cible, nbouvert, alpha, coeffOrifice, nbSim);
			}

		}

		// simulation robinet par robinet
		if (typeSimulation.equals("tapbytap")) {

			// Remet les stats des precedentes simulation e 0
			dsg.pvector.initializeSimulation(dsg.tvector.size());
			dsg.nvector.initializeSimulation(dsg.tvector.size());

			new RunSimulation(F, dsg.nvector, dsg.pvector, dsg.tvector, outflow, rate1, seuil, seuil2, typeSimulation,
					dsg.tvector.size(), coeffOrifice, solver, lbd, invest,
					maxiter, tolr, tolx, cible, nbouvert, alpha, coeffOrifice, nbSim);
		}

		// simulation handmade
		if (typeSimulation.equals("handmade")) {

			Enumeration enun = dsg.tvector.elements();

			while (enun.hasMoreElements()) {
				Taps tap = (Taps) enun.nextElement();
				tap.select = "close";

				if (faucetRef.get(tap.taps) != null) {
					if (faucetRef.get(tap.taps).equals(new Boolean(true))) {
						tap.select = "open";
					}
				}
			}

			dsg.pvector.initializeSimulation(1);
			dsg.nvector.initializeSimulation(1);
			solver.setProgress(50);

			new RunSimulation(F, dsg.nvector, dsg.pvector, dsg.tvector, outflow, rate1, seuil, seuil2, typeSimulation,
					0, coeffOrifice,null, lbd, invest,
					maxiter, tolr, tolx, cible, nbouvert, alpha, coeffOrifice, nbSim);
		}

		// Calcul les vitesses dans chaque tuyau
		dsg.pvector.CalculSpeed();

		// quartiles
		calculQuartile();

		// stats sur les pressions
		calculStatPressure();
	}

	/** Ajoute des noeuds intermediaires pour les tuyaux coupes en 2 */
	private void ajoutNodes() {
		Pipes pipes;
		Pipes pip;
		Nodes nodes;
		Nodes n1;
		Nodes n2;
		int i = 0;

		while (i < dsg.pvector.size()) {
			pipes = (Pipes) dsg.pvector.elementAt(i);

			//
			if (pipes.l2 != 0) {
				n1 = (Nodes) dsg.nvector.elementAt(dsg.nvector.getPosition(pipes.nodes_beg));
				n2 = (Nodes) dsg.nvector.elementAt(dsg.nvector.getPosition(pipes.nodes_end));
				pip = new Pipes(pipes.nodes_beg + "*" + pipes.nodes_end, pipes.nodes_end,
						pipes.nodes_beg + "*" + pipes.nodes_end, 0);
				pipes.nodes_end = pipes.nodes_beg + "*" + pipes.nodes_end;
				pip.l1 = pipes.l2;
				pip.d1 = pipes.d2;
				pip.p1 = pipes.p2;
				pip.q1 = pipes.q2;
				pip.beta1 = pipes.beta2;

				nodes = new Nodes(pipes.nodes_end, 0, 0);
				nodes.ajout = 1;
				nodes.height = n1.height + ((n2.height - n1.height) / pipes.length * pipes.l1);

				if (i >= (dsg.pvector.size() - 1 - dsg.tvector.size())) {
					i++;
					dsg.nvector.add(dsg.nvector.size() - dsg.tvector.size(), nodes);
					dsg.pvector.add(dsg.pvector.size() - dsg.tvector.size(), pip);
				} else {
					dsg.nvector.add(i + 1, nodes);
					dsg.pvector.add(i + 1, pip);
				}
			}

			i++;
		}
	}

	public String getPropertiesContent() {
		String content = "";

		// ajoute les properties ( 3 champs)
		content += Messages.getString("MakeSimulation.Default_properties");

		// content += "!Name-Value\n";
		Enumeration iter = properties.propertyNames();

		while (iter.hasMoreElements()) {
			String name = iter.nextElement().toString();

			if (name.startsWith("simu.") && name.endsWith(".value")) {
				content += (name.substring(5, name.length() - 6) + "," + properties.getProperty(name) + ",N\n");
			}
		}

		return content;
	}

	/**
	 * renvoie les flots de sortie pour chaque robinets
	 * <p>
	 * Le format de sortie est le suivant: tap ID, moyenne de flots, min, max et
	 * detail des simulations.
	 */
	public Vector getResultsSimu() {
		Vector v = new Vector();

		for (int i = dsg.pvector.size() - dsg.tvector.size(); i < dsg.pvector.size(); i++) {
			Pipes pipes = (Pipes) dsg.pvector.get(i);

			if (dsg.nvector.getNbTaps(pipes.nodes_end) == 0) {
				String name = pipes.nodes_end;

				if (name.lastIndexOf("*") > 0) {
					name = name.substring(name.lastIndexOf("*") + 1, name.length());
				}

				if (name.lastIndexOf("_") > 0) {

					int n = name.substring(name.lastIndexOf("_") + 1, name.length()).charAt(0) - 'a' + 1;
					name = name.substring(0, name.lastIndexOf("_") + 1) + n;
				}

				Vector line = new Vector();
				line.add(name);
				line.add(Tools.doubleFormat("0.####", pipes.min));
				line.add(Tools.doubleFormat("0.####", pipes.moyenne));
				line.add(Tools.doubleFormat("0.####", pipes.max));

				for (int j = 0; j < pipes.simulation.length; j++) {
					line.add(Tools.doubleFormat("0.####", pipes.simulation[j]));
				}

				v.add(line);
			}
		}

		return v;
	}

	/**
	 * renvoie les flots de sortie pour chaque robinets
	 * <p>
	 * Le format de sortie est le suivant: tap ID, moyenne de flots, min, max et
	 * detail des simulations.
	 */
	public Vector getSimpleResultsSimu() {
		Vector v = new Vector();
		int totsim = 0;
		int tots1 = 0;
		int tots2 = 0;
		int totfail = 0;
		double totmoy = 0;

		// robinets
		for (int i = dsg.pvector.size() - dsg.tvector.size(); i < dsg.pvector.size(); i++) {
			Pipes pipes = (Pipes) dsg.pvector.get(i);
			Vector line = new Vector();

			String name = pipes.nodes_end;

			if (name.lastIndexOf("*") > 0) {
				name = name.substring(name.lastIndexOf("*") + 1, name.length());
			}

			if (name.lastIndexOf("_") > 0) {

				int n = name.substring(name.lastIndexOf("_") + 1, name.length()).charAt(0) - 'a' + 1;
				name = name.substring(0, name.lastIndexOf("_") + 1) + n;
			}

			line.add(name);
			line.add("" + pipes.nbsim);
			totsim += pipes.nbsim;
			line.add(Tools.doubleFormat("0.####", pipes.min));

			line.add(Tools.doubleFormat("0.####", pipes.moyenne));
			totmoy += (pipes.moyenne * pipes.nbsim);
			line.add(Tools.doubleFormat("0.####", pipes.max));

			double variability = 0;

			try {
				variability = Math.sqrt(pipes.moyennec - (pipes.moyenne * pipes.moyenne)) / pipes.moyenne * 100;
			} catch (Exception ex) {
			}

			line.add(Tools.doubleFormat("0.##", variability));
			line.add("" + Tools.doubleFormat("0.##", ((double) pipes.seuil) / pipes.nbsim * 100));
			tots1 += pipes.seuil;
			line.add("" + Tools.doubleFormat("0.##", ((double) pipes.seuil2) / pipes.nbsim * 100));
			tots2 += pipes.seuil2;
			line.add("" + pipes.failure);
			totfail += pipes.failure;
			line.add("-1");
			v.add(line);
		}

		Vector line = new Vector();
		line.add(Messages.getString("MakeSimulation.Global_average"));
		line.add("-");
		line.add("-");
		line.add(Tools.doubleFormat("0.####", totmoy / totsim));
		line.add("-");
		line.add("?");
		line.add("" + Tools.doubleFormat("0.##", ((double) tots1) / totsim * 100));
		line.add("" + Tools.doubleFormat("0.##", ((double) tots2) / totsim * 100));
		line.add("" + totfail);
		line.add("-1");
		v.insertElementAt(line, 0);

		return v;
	}

	/**
	 * renvoie les statistiques de pression
	 * <p>
	 * Le format de sortie est le suivant: node ID, average pressure, minimum
	 * pressure, maximum pressure
	 */
	public Vector getPressureSimu() {
		Vector v = new Vector();

		for (int i = 0; i < dsg.nvector.size(); i++) {
			Nodes nodes = (Nodes) dsg.nvector.get(i);
			Vector line = new Vector();
			line.add(nodes.nodes);
			line.add(Tools.doubleFormat("0.##", nodes.minpress));
			line.add(Tools.doubleFormat("0.##", nodes.averpress));
			line.add(Tools.doubleFormat("0.##", nodes.maxpress));
			v.add(line);
		}

		return v;
	}

	/**
	 * renvoie les statistiques de vitesse de l'eau dans les tuyaux
	 * <p>
	 * Le format de sortie est le suivant:
	 */
	public Vector getSpeedSimu() {
		Vector v = new Vector();

		for (int i = 0; i < dsg.pvector.size(); i++) {
			Pipes pipes = (Pipes) dsg.pvector.get(i);
			Vector line = new Vector();
			String name = pipes.nodes_end;

			if (name.lastIndexOf("_") > 0) {

				int n = name.substring(name.lastIndexOf("_") + 1, name.length()).charAt(0) - 'a' + 1;
				name = name.substring(0, name.lastIndexOf("_") + 1) + n;
			}

			line.add(pipes.nodes_beg + " -> " + name);
			line.add("" + pipes.nbsim);
			line.add(Tools.doubleFormat("0.##", pipes.speed));
			line.add(Tools.doubleFormat("0.##", pipes.speedmax));
			line.add("-1");
			v.add(line);
		}

		return v;
	}

	/**
	 * renvoie les statistiques de vitesse de l'eau
	 * <p>
	 * Le format de sortie est le suivant:
	 */
	public Vector getQuartileSimu() {
		Vector v = new Vector();

		for (int i = dsg.pvector.size() - dsg.tvector.size(); i < dsg.pvector.size(); i++) {
			Pipes pipes = (Pipes) dsg.pvector.get(i);
			Vector line = new Vector();
			String name = pipes.nodes_end;

			if (name.lastIndexOf("*") > 0) {
				name = name.substring(name.lastIndexOf("*") + 1, name.length());
			}

			if (name.lastIndexOf("_") > 0) {

				int n = name.substring(name.lastIndexOf("_") + 1, name.length()).charAt(0) - 'a' + 1;
				name = name.substring(0, name.lastIndexOf("_") + 1) + n;
			}

			line.add(name);
			line.add("" + pipes.nbsim);
			line.add(Tools.doubleFormat("0.####", pipes.min));
			line.add(Tools.doubleFormat("0.####", pipes.quart10));
			line.add(Tools.doubleFormat("0.####", pipes.quart25));
			line.add(Tools.doubleFormat("0.####", pipes.quart50));
			line.add(Tools.doubleFormat("0.####", pipes.quart75));
			line.add(Tools.doubleFormat("0.####", pipes.quart90));
			line.add(Tools.doubleFormat("0.####", pipes.max));
			line.add("N");
			line.add("N");
			v.add(line);
		}

		return v;
	}

	/** calcul les differents quartiles sur les simulations */
	private void calculQuartile() {
		for (int k = dsg.pvector.size() - dsg.tvector.size(); k < dsg.pvector.size(); k++) {
			Pipes pipes = (Pipes) dsg.pvector.get(k);

			// copie du tableau de simu
			double[] simu = new double[pipes.simulation.length];

			for (int i = 0; i < pipes.simulation.length; i++) {
				simu[i] = pipes.simulation[i];
			}

			// tri
			boolean bool = false;

			while (bool == false) {
				bool = true;

				for (int i = 0; i < (simu.length - 1); i++) {
					if (((simu[i] > simu[i + 1]) && (simu[i + 1] != 0))
							|| ((simu[i] < simu[i + 1]) && (simu[i] == 0))) {
						// if (simu[i] > simu[i + 1]) {
						bool = false;

						double tamp = simu[i];
						simu[i] = simu[i + 1];
						simu[i + 1] = tamp;
					}
				}
			}

			// puts failures first
			for (int i = simu.length - 1; i > pipes.failure - 1; i--) {
				simu[i] = simu[i - pipes.failure];
			}
			for (int i = 0; i < (pipes.failure - 1); i++) {
				simu[i] = 0;
			}

			// affectation quartile
			pipes.quart10 = simu[(int) Math.floor(pipes.nbsim * 0.1)];
			pipes.quart25 = simu[(int) Math.floor(pipes.nbsim * 0.25)];
			pipes.quart50 = simu[(int) Math.floor(pipes.nbsim * 0.5)];
			pipes.quart75 = simu[(int) Math.floor(pipes.nbsim * 0.75)];
			pipes.quart90 = simu[(int) Math.floor(pipes.nbsim * 0.9)];

			// affectation effectif
			double ecart = pipes.max - pipes.min;
			pipes.quarteff10 = 0;
			pipes.quarteff25 = 0;
			pipes.quarteff50 = 0;
			pipes.quarteff75 = 0;
			pipes.quarteff100 = pipes.nbsim;

			for (int i = 0; i < simu.length; i++) {
				if (simu[i] <= ((ecart * 0.1) + pipes.min)) {
					pipes.quarteff10++;
				}

				if (simu[i] <= ((ecart * 0.25) + pipes.min)) {
					pipes.quarteff25++;
				}

				if (simu[i] <= ((ecart * 0.50) + pipes.min)) {
					pipes.quarteff50++;
				}

				if (simu[i] <= ((ecart * 0.75) + pipes.min)) {
					pipes.quarteff75++;
				}

				if (simu[i] <= ((ecart * 0.90) + pipes.min)) {
					pipes.quarteff90++;
				}
			}

			pipes.quarteff100 -= pipes.quart90;
			pipes.quarteff100 *= (100 / 10);
			pipes.quarteff90 -= pipes.quart75;
			pipes.quarteff90 *= (100 / 15);
			pipes.quarteff75 -= pipes.quart50;
			pipes.quarteff75 *= (100 / 25);
			pipes.quarteff50 -= pipes.quart25;
			pipes.quarteff50 *= (100 / 25);
			pipes.quarteff25 -= pipes.quart10;
			pipes.quarteff50 *= (100 / 15);
			pipes.quarteff10 *= (100 / 10);
		}
	}

	/** calcul les pressions observees */
	private void calculStatPressure() {
		for (int i = 1; i < dsg.nvector.size(); i++) {
			Nodes nodes = (Nodes) dsg.nvector.get(i);
			nodes.minpress = 10000;
			nodes.maxpress = nodes.pressim[0];

			for (int j = 0; j < nodes.pressim.length; j++) {
				if (nodes.pressim[j] == 0) {
					nodes.pressim[j] = -nodes.height;
				}

				/* moyenne */
				nodes.averpress = nodes.averpress + nodes.pressim[j];

				/* min */
				if (nodes.pressim[j] < nodes.minpress) {
					nodes.minpress = nodes.pressim[j];
				}

				/* max */
				if (nodes.pressim[j] > nodes.maxpress) {
					nodes.maxpress = nodes.pressim[j];
				}
			}

			nodes.averpress = nodes.averpress / nodes.pressim.length;
		}
	}

	/**
	 * Cette procedure verifie que le reseau est un arbre ou non. Si un noeud a
	 * un nombre de predecesseurs sup a 1 alors le reseau n'est pas un arbre
	 */
	private boolean isTree() {
		boolean tree = true;

		/*
		 * Nodes nodes; for(int i = 0 ; i < dsg.nvector.size() ; i++){ nodes =
		 * (Nodes) dsg.nvector.elementAt(i);
		 * if(dsg.pvector.GetNumberOfPred(nodes) > 1){ Tree = false; i =
		 * dsg.nvector.size(); } }
		 */
		Enumeration e = dsg.nvector.elements();

		while ((tree) && (e.hasMoreElements())) {
			Nodes n = (Nodes) e.nextElement();

			if (dsg.pvector.GetNumberOfPred(n) > 1) {
				tree = false;
			}
		}

		return tree;
	}

	/**
	 * Ajoute pour chaque branche de l'arbre une branche inverse si elle
	 * n'existe pas deja
	 */
	private void addInversBranch() {
		Pipes pipes; /* Pipes traite */
		Pipes invpipes; /* Pipes inverse */
		Nodes source = (Nodes) dsg.nvector
				.elementAt(0); /* on prend la source */

		for (int i = 0; i < dsg.pvector.size(); i++) {
			pipes = (Pipes) dsg.pvector.elementAt(i);

			/* si la branche inverse n'exste pas deja (a faire) */
			/* ne pas effectuer l'operation sur les robinets et sur la source */
			if ((dsg.nvector.getNbTaps(pipes.nodes_end) == 0) && (dsg.tvector.isTaps(pipes.nodes_end) == false)
					&& (!pipes.nodes_beg.equalsIgnoreCase(source.nodes))) {
				invpipes = new Pipes(pipes.nodes_end, pipes.nodes_beg, pipes.nodes_end,
						0 /* pipes.length */);
				invpipes.l1 = pipes.l1;
				invpipes.l2 = pipes.l2;
				invpipes.d1 = pipes.d1;
				invpipes.d2 = pipes.d2;
				invpipes.beta1 = pipes.beta1;
				invpipes.beta2 = pipes.beta2;
				invpipes.p1 = pipes.p1;
				invpipes.p2 = pipes.p2;
				invpipes.q1 = pipes.q1;
				invpipes.q2 = pipes.q2;

				// dsg.pvector.add(i+1,invpipes);
				dsg.pvector.insertElementAt(invpipes, 1);
				i++;
			}
		}
	}
}
