package neatwork.core.run;

import java.util.Collections;

import mosek.boundkey;
import neatwork.core.defs.*;
import neatwork.solver.Solver;

public class RunMakeDesign {
	// variables generales du probleme Design
	// private double alpha;
	public double CoefOrif;

	// variables de dimensionnement du probleme en question/
	public int NbDiam;

	public int NbPipes;

	public int NbNodes;

	public int NbTaps;

	public int n;

	public int m;

	public double[] Cste;

	// Constructeur
	public RunMakeDesign(double[] x, NodesVector nvector, PipesVector pvector, TapsVector tvector,
			DiametersVector dvector, OrificesVector ovector, double[] LoadFactor, double outflow, double lcom, int n1,
			int m1, double PrixMax, double CoefOrif1) {
		NbDiam = dvector.size();
		NbPipes = pvector.size();
		NbNodes = nvector.size();
		NbTaps = tvector.size();
		n = n1;
		m = m1;

		CoefOrif = CoefOrif1;
		Cste = new double[m];

		int L = LengthTab(nvector); /* A number of element */

		// definition of A*
		double[] ACoeff = new double[L];
		int[] AIRow = new int[L];
		int[] AIColumn = new int[((NbDiam * NbPipes) + NbNodes) - 1];
		int[] AIColumn2 = new int[((NbDiam * NbPipes) + NbNodes) - 1];

		MatriceA(ACoeff, AIRow, AIColumn, AIColumn2, pvector, nvector, dvector, LoadFactor, L, n);

		/* definition of C */
		double[] c = Cvector(x, n, dvector, pvector);

		/* Variables definition */
		mosek.boundkey[] bkx = new mosek.boundkey[n];
		double[] blx = new double[n];
		double[] bux = new double[n];

		InitializeVariable(x, pvector, bkx, blx, bux, dvector, nvector, outflow);
		Resolution(x, nvector, pvector, dvector, tvector, outflow, PrixMax, ACoeff, AIRow, AIColumn, AIColumn2, L, c,
				bkx, blx, bux, lcom);

		new Decoupe(x, lcom, pvector.size(), dvector, pvector);
		double[] CstValue = CstValue2(x, n, m, nvector, pvector, dvector, tvector, LoadFactor, outflow);
		GestionOrificeParfait(x, nvector, pvector, dvector, ovector, LoadFactor, Cste, outflow, tvector, CstValue);

		if (ovector.size() > 0) {
			GestionOrificeCommercial(tvector, ovector, CstValue);
		}

		CstValue2(x, n, m, nvector, pvector, dvector, tvector, LoadFactor, outflow);

		GetSuction(x, nvector, dvector);
	}

	public void GetSuction(double[] x, NodesVector nvector, DiametersVector dvector) {
		Nodes nodes;

		for (int i = 1; i < nvector.size(); i++) {
			nodes = (Nodes) nvector.elementAt(i);
			nodes.suction = x[(i + (dvector.size() * (nvector.size() - 1))) - 1];
		}
	}

	/* Gestion des orifices commerciaux */
	public void GestionOrificeCommercial(TapsVector tvector, OrificesVector ovector, double[] CstValue) {
		// Step 1 : Add artificial Orifices
		double dmin = 0.000001;
		double dmax = 1;
		ovector.addDiameters(dmin);
		ovector.addDiameters(dmax);

		// Sort orifice liste
		Collections.sort(ovector, new Orifices(0));

		for (int i = NbNodes - NbTaps - 1; i < (NbNodes - 1); i++) {
			Taps tap = (Taps) tvector.get(i - NbNodes + NbTaps + 1);

			// Step 2 : Bracket
			double orif_sup = dmin;
			double orif_inf = dmin;
			int j = 0;

			while ((j < ovector.size()) && (tap.orif_ideal > orif_sup)) {
				orif_sup = ((Orifices) ovector.get(j)).diam;

				if (j > 0) {
					orif_inf = ((Orifices) ovector.get(j - 1)).diam;
				}

				j++;
			}

			// Step 3 : Assignation
			if (tap.orif_ideal >= (Math.sqrt(2) * orif_inf)) {
				tap.orif_com = orif_sup;
			} else if ((orif_inf * orif_sup) <= (tap.orif_ideal * tap.orif_ideal)) {
				tap.orif_com = orif_sup;
			} else {
				tap.orif_com = orif_inf;
			}

			// Step 4 : bounds
			if (tap.orif_com == dmin) {
				tap.orif_com = Orifices.MAXDIAM;
			}

			if (tap.orif_com == dmax) {
				tap.orif_com = Orifices.MAXDIAM;
			}
		}
	}

	/* Gestion des orifices parfait */
	public void GestionOrificeParfait(double[] x, NodesVector nvector, PipesVector pvector, DiametersVector dvector,
			OrificesVector ovector, double[] LoadFactor, double[] Cste1, double outflow, TapsVector tvector,
			double[] CstValue) {
		double d = 0;
		Taps taps;

		for (int i = NbNodes - NbTaps - 1; i < (NbNodes - 1); i++) {
			// test d'accessibilite au calcul
			if (Cste1[i] > CstValue[i]) {
				taps = (Taps) tvector.elementAt(i - NbNodes + NbTaps + 1);
				d = Math.pow((Math.pow(CoefOrif, 4) * Math.pow(outflow, 2))
						/ (Cste1[i] - CstValue[i] + (Math.pow(CoefOrif / taps.orifice, 4) * Math.pow(outflow, 2))),
						0.25);
				taps.orif_ideal = d;
				taps.orifice = d;
			}
		}
	}

	/* Resolution du probleme Design */
	public void Resolution(double[] x, NodesVector nvector, PipesVector pvector, DiametersVector dvector,
			TapsVector tvector, double outflow, double PrixMax, double[] ACoeff, int[] AIRow, int[] AIColumn,
			int[] AIColumn2, int L, double[] c, boundkey[] bkx, double[] blx, double[] bux, double lcom) {
		/* constraints definition */
		mosek.boundkey[] bkc = new mosek.boundkey[m];
		double[] blc = new double[m];
		InitializeConstraints(bkc, blc, nvector, pvector, tvector, outflow, PrixMax, lcom);

		/* Call solver */
		Solver solver = null;

		try {
			solver = new Solver();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		solver.lp(m, n, L, bkc, blc, Cste, bkx, blx, bux, AIColumn, AIColumn2, AIRow, ACoeff, x, c);
	}

	/* definition de la matrice A sous forme creuse */
	public void MatriceA(double[] ACoeff, /* Tableau des coeff */
			int[] AIRow, /* Tableau d'indice de ligne */
			int[] AIColumn, int[] AIColumn2, PipesVector pvector, NodesVector nvector, DiametersVector dvector,
			double[] LoadFactor, int L, int n1) {
		Pipes pipes;
		Nodes nodes;
		Diameters diam = null;
		int IPath;
		String n_end;
		String n_beg;
		int I1 = 0;
		int I2 = 0;
		int I3 = 0;

		for (int IPipes = 0; IPipes < NbPipes; IPipes++) {
			for (int IDiam = 0; IDiam < NbDiam; IDiam++) {
				AIColumn[I3] = I1;

				/* creation du deuxieme vecteur necessaire a mosek */
				if (I3 != 0) {
					AIColumn2[I3 - 1] = AIColumn[I3];
				}

				pipes = (Pipes) pvector.elementAt(IPipes);

				for (int INodes = 1; INodes < NbNodes; INodes++) {
					nodes = (Nodes) nvector.elementAt(INodes);
					IPath = nodes.path.size() - 1;

					while ((IPath > 0)) {
						n_beg = (String) nodes.path.elementAt(IPath);
						n_end = (String) nodes.path.elementAt(IPath - 1);

						if ((pipes.nodes_beg.equalsIgnoreCase(n_beg)) && (pipes.nodes_end.equalsIgnoreCase(n_end))) {
							IPath = 0;
							diam = (Diameters) dvector.elementAt(IDiam);
							ACoeff[I1] = Math.pow(LoadFactor[IPipes], diam.p) / Math.pow(diam.diam, diam.q) * diam.beta;
							AIRow[I2] = INodes - 1;
							I1++;
							I2++;
						} else {
							IPath--;
						}
					}
				}

				/* Contrainte de longueur */
				ACoeff[I1] = 1;
				AIRow[I2] = (NbNodes + IPipes) - 1;
				I1++;
				I2++;

				/* Contrainte de prix max */
				ACoeff[I1] = diam.cost;
				AIRow[I2] = (NbNodes + NbPipes) - 1;
				I1++;
				I2++;

				I3++;
			}
		}

		/* Les variables d'ecarts */
		for (int i = L - NbNodes + 1; i < L; i++) {
			AIColumn[I3] = I1;
			AIColumn2[I3 - 1] = AIColumn[I3];
			ACoeff[I1] = -1;
			AIRow[I2] = i - (L - NbNodes + 1);
			I1++;
			I2++;
			I3++;
		}

		AIColumn2[n1 - 1] = L;
	}

	/* renvoie le nombre d'uunts non nul dans la matrice A */
	public int LengthTab(NodesVector nvector) {
		int L = 0;
		Nodes nodes;

		for (int i = 1; i < nvector.size(); i++) {
			nodes = (Nodes) nvector.elementAt(i);
			L = (L + nodes.path.size()) - 1;
		}

		return (((L + NbPipes) * NbDiam) + NbNodes) - 1 + (NbDiam * NbPipes);
	}

	/* Initialise les valeurs constantes et le type des contraintes */
	public void InitializeConstraints(boundkey[] bkc, double[] blc, NodesVector nvector, PipesVector pvector,
			TapsVector tvector, double outflow, double PrixMax, double lcom) {
		Nodes nodes;
		Pipes pipes;
		Taps taps;

		/* Contraintes sur les Nodes - taps */
		for (int i = 0; i < (NbNodes - NbTaps - 1); i++) {
			nodes = (Nodes) nvector.elementAt(i + 1);
			Cste[i] = -nodes.height;
			bkc[i] = mosek.boundkey.up;
			blc[i] = -1.0e30;
		}

		/* Contraintes sur les Taps */
		for (int i = NbNodes - NbTaps - 1; i < (NbNodes - 1); i++) {
			nodes = (Nodes) nvector.elementAt(i + 1);
			taps = (Taps) tvector.elementAt(i - (NbNodes - NbTaps - 1));

			if (taps.orifice == Orifices.MAXDIAM) {
				Cste[i] = -nodes.height - ((outflow * outflow) / taps.faucetCoef);
			} else {
				Cste[i] = (-nodes.height
						- (outflow * outflow * ((1 / taps.faucetCoef) + Math.pow(CoefOrif / taps.orifice, 4))));
			}

			bkc[i] = mosek.boundkey.up;
			blc[i] = -1.0e30;
		}

		/* Contraintes sur les longueurs des Pipes */
		for (int i = NbNodes - 1; i < (m - 1); i++) {
			pipes = (Pipes) pvector.elementAt(i - NbNodes + 1);

			Cste[i] = pipes.length;

			bkc[i] = mosek.boundkey.fx;
			blc[i] = Cste[i];
		}

		/* contrainte de prix max */
		Cste[m - 1] = PrixMax;
		bkc[m - 1] = mosek.boundkey.up;
		blc[m - 1] = -1.0e30;
	}

	/* Initialise les bornes inferieures et superieures des variables */
	public void InitializeVariable(double[] x, PipesVector pvector, boundkey[] bkx, double[] blx, double[] bux,
			DiametersVector dvector, NodesVector nvector, double outflow) {
		Pipes pipes;
		Diameters diam;
		Nodes nodes;

		/* les variables de longeurs */
		for (int i = 0; i < NbPipes; i++) {
			pipes = (Pipes) pvector.elementAt(i);
			nodes = (Nodes) nvector.elementAt(nvector.getPosition(pipes.nodes_end));

			for (int j = 0; j < NbDiam; j++) {
				diam = (Diameters) dvector.elementAt(j);

				/* control du SDR */
				if ((-nodes.height < diam.pression) && (diam.diam > pipes.imposdiammin)
						&& (diam.diam < pipes.imposdiammax)) {
					blx[(i * NbDiam) + j] = 0;
					bux[(i * NbDiam) + j] = pipes.length;
					bkx[(i * NbDiam) + j] = mosek.boundkey.ra;

				} else {
					blx[(i * NbDiam) + j] = 0;
					bux[(i * NbDiam) + j] = 0;
					bkx[(i * NbDiam) + j] = mosek.boundkey.fx;
				}

				if (pipes.imposdiam1 == diam.diam) {
					bux[(i * NbDiam) + j] = pipes.imposlength1;
					blx[(i * NbDiam) + j] = bux[(i * NbDiam) + j];
					bkx[(i * NbDiam) + j] = mosek.boundkey.fx;
				}

				if (pipes.imposdiam2 == diam.diam) {
					bux[(i * NbDiam) + j] = pipes.length - pipes.imposlength1;
					blx[(i * NbDiam) + j] = bux[(i * NbDiam) + j];
					bkx[(i * NbDiam) + j] = mosek.boundkey.fx;
				}
			}
		}

		// Slack variables
		for (int i = NbPipes * NbDiam; i < (((NbPipes * NbDiam) + NbNodes) - 1); i++) {
			bux[i] = 1.0e30; // borne superieure infinie
			blx[i] = 0; // borne inferieure finie
			bkx[i] = mosek.boundkey.lo; /* bornee inferieurement */
		}
	}

	/* Calcul des valeurs des contraintes avec les l1 et l2 */
	public double[] CstValue2(double[] x, int n1, int m1, NodesVector nvector, PipesVector pvector,
			DiametersVector dvector, TapsVector tvector, double[] LoadFactor, double outflow) {
		double[] Cst = new double[m1];
		Nodes nodes;
		Pipes pipes;
		String n_end;

		/* Les noeuds sans les robinets */
		for (int i = 1; i < (nvector.size() - tvector.size()); i++) {
			Cst[i - 1] = 0;
			nodes = (Nodes) nvector.elementAt(i);

			for (int j = 0; j < (nodes.path.size() - 1); j++) {
				n_end = (String) nodes.path.elementAt(j);

				pipes = (Pipes) pvector.elementAt(pvector.getPosition(n_end));

				Cst[i - 1] = Cst[i - 1]
						+ ((pipes.l1 * Math.pow(LoadFactor[pvector.getPosition(n_end)], pipes.p1))
								/ Math.pow(pipes.d1, pipes.q1) * pipes.beta1)
						+ ((pipes.l2 * Math.pow(LoadFactor[pvector.getPosition(n_end)], pipes.p2))
								/ Math.pow(pipes.d2, pipes.q2) * pipes.beta2);
			}

			nodes.pressure = Cst[i - 1];
		}

		/* Les robinets */
		for (int i = nvector.size() - tvector.size(); i < nvector.size(); i++) {
			Cst[i - 1] = 0;
			nodes = (Nodes) nvector.elementAt(i);

			for (int j = 0; j < (nodes.path.size() - 1); j++) {
				n_end = (String) nodes.path.elementAt(j);
				pipes = (Pipes) pvector.elementAt(pvector.getPosition(n_end));

				Cst[i - 1] = Cst[i - 1]
						+ ((pipes.l1 * Math.pow(LoadFactor[pvector.getPosition(n_end)], pipes.p1))
								/ Math.pow(pipes.d1, pipes.q1) * pipes.beta1)
						+ ((pipes.l2 * Math.pow(LoadFactor[pvector.getPosition(n_end)], pipes.p2))
								/ Math.pow(pipes.d2, pipes.q2) * pipes.beta2);
			}

			nodes.pressure = Cst[i - 1];
		}

		return Cst;
	}

	public double[] Cvector(double[] x, int n1, DiametersVector dvector, PipesVector pvector) {
		double[] grad = new double[n1];
		Diameters diam;
		int j = 0;

		for (int i = 0; i < (n1 - NbNodes + 1); i++) {
			diam = (Diameters) dvector.elementAt(j);
			grad[i] = diam.cost;

			if (j < (dvector.size() - 1)) {
				j++;
			} else {
				j = 0;
			}
		}

		double CoeffM = CoeffM(pvector, dvector);

		for (int i = n1 - NbNodes + 1; i < n1; i++) {
			grad[i] = CoeffM;
		}

		return grad;
	}

	/* Calcul le coeff de la variable d'ecart dans la fonction objectif */
	public double CoeffM(PipesVector pvector, DiametersVector dvector) {
		double Coeff = 0;
		Pipes pipes;
		Diameters diam = (Diameters) dvector.lastElement();

		for (int i = 0; i < pvector.size(); i++) {
			pipes = (Pipes) pvector.elementAt(i);
			Coeff = Coeff + pipes.length;
		}

		return Coeff * diam.cost;
	}
}