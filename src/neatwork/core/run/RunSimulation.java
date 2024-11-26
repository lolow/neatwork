package neatwork.core.run;

import neatwork.core.MakeSimulation;
import neatwork.core.SimulFlows;
import neatwork.core.defs.Nodes;
import neatwork.core.defs.NodesVector;
import neatwork.core.defs.Pipes;
import neatwork.core.defs.PipesVector;
import neatwork.core.defs.Taps;
import neatwork.core.defs.TapsVector;
import neatwork.solver.AbstractSolver;
import neatwork.solver.Solver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import mosek.*;

/**
 * run simulation operation
 * @author L. DROUET
 * @version 1.0
 */
public class RunSimulation {


    public PipesVector pvector;
    public PipesVector psubvector;
    public NodesVector nvector;
    public NodesVector nsubvector;
    public TapsVector tvector;
    public TapsVector tsubvector;

    /*variables generales du probleme Design*/

    //private double alpha ;
    public double CoefOrif;

    /*variables de dimensionnement du probleme en question*/
    public int NbDiam;
    public int NbPipes;
    public int NbNodes;
    public int NbTaps;
    public int n;
    public int m;

    /*Bornes inferieures et superieures sur les variables*/
    public double[] F;
    public double[] y;
    public double[] Dual;

    /*Types et valeurs des Constantes des contraintes*/
    public int[] Type;
    public boolean TypePb = true;

    /*Paramètres de la simu*/
    int[][] S;
    double length[];
    double height[];
    double alphaSimu[];
    double betaSimu[];
    double lbd;
    double invest;
    int maxiter;
    double tolr;
    double tolx;
    double outflow;
    double[] nbouvert;
    int nbSim;


    public RunSimulation(double[] f, NodesVector nv, PipesVector pv,
        TapsVector tv, double outflow, double rate, double seuil,
        double seuil2, String operation, int index, double CoefOrif1, double lbd, double invest, int maxiter, 
        double tolr, double tolx, double[] nbouvert, double alpha, double coeffOrifice, int nbSim) {
        this(f, nv, pv, tv, outflow, rate, seuil, seuil2, operation, index,
            CoefOrif1, null, lbd, invest,
            maxiter, tolr, tolx, nbouvert, alpha, coeffOrifice, nbSim);
    }

    /* Constructeur */
    public RunSimulation(double[] f, NodesVector nv, PipesVector pv,
        TapsVector tv, double outflow, double rate, double seuil,
        double seuil2, String operation, int index, double CoefOrif1,
        AbstractSolver solver, double lbd, double invest, int maxiter, 
        double tolr, double tolx, double[] nbouvert, double alpha, double coeffOrifice, int nbSim) {
    	
        /* Creation des sous vecteurs de pipes, de nodes et de taps */
        psubvector = new PipesVector();
        nsubvector = new NodesVector();
        tsubvector = new TapsVector();




        /* Initialisation des veceurs pour cette classe */
        pvector = pv;
        nvector = nv;
        tvector = tv;
        F = f; /* Vecteurs de flots resultant de l'optimisation */



        Taps taps2;
        Nodes nodes2;
        Vector<String> terminalNodes = new Vector<String>();
        Vector<String> intermNodes = new Vector<String>();
    
        for (int i = 0; i < tvector.size(); i++) {
            taps2 = (Taps) tvector.elementAt(i);
            terminalNodes.add(taps2.taps);
        }
    
        for (int i = 0; i < nvector.size(); i++) {
            nodes2 = (Nodes) nvector.elementAt(i);
            if (!terminalNodes.contains(nodes2.nodes) && nodes2.height != 0.0) {
                intermNodes.add(nodes2.nodes);
            }
        }
    

        HashMap<String, String> toInitNodes = SortNodeNames(nvector, tvector, pvector);

        //alpha = alpha1; /* parametres de resolution */
        CoefOrif = CoefOrif1;

        /* dimensions des vecteurs pipes, nodes et taps*/
        NbPipes = pvector.size();
        NbNodes = nvector.size();
        NbTaps = tvector.size();


        /* selection aleatoire des robinets ouverts */
        if (operation.equals("random")) { 

            /* On selection un ensemble de robinets ouverts et on initialise
              le sous vecteur de taps*/
            SelectTaps(rate);

            /* On cree le sous reseau (sous vecteur de pipes et de nodes)*/
            DoSubReseau();


            /* Nombre de variables */
            n = psubvector.size() + tsubvector.size() + 1;

            /* Nombre de contraintes */
            m = nsubvector.size();

            if (tsubvector.size() > 0) {
                double[] y = new double[nsubvector.size()];
                int[][] S = getPathMatrix(nsubvector, psubvector, tsubvector);
                int[][] Sb = getPathMatrixIntermNode(nsubvector, psubvector, tsubvector);
                double length[] = getLength(psubvector);
                double height[] = getHeight(nsubvector);
                double alphaSimu[] = getAlpha(alpha, coeffOrifice, tsubvector);
                double betaSimu[] = getBeta(nsubvector, psubvector, tsubvector);

                /* procedure de resolution */
                SimulFlows.run(y, F, tsubvector,psubvector, S.length, S[0].length, length, height, invest, outflow, lbd, S, Sb, nbouvert, alphaSimu, betaSimu, maxiter, tolr, tolx, nbSim, rate);
                  
                // Resolution(outflow);
                TapsStatistic(seuil2, seuil, index, y);
            }

            // Les indices des noeuds sont rétablis dans l'ordre d'origine
            RevertNodeNames(nvector, pvector, tvector, toInitNodes);

        }

        /* simulations des robinets un par un */
        if (operation.equals("tapbytap")) { 

            for (int i = 0; i < tvector.size(); i++) {
                //ligne de progression
                solver.setProgress(Math.round((float) i / tvector.size() * 100));

                psubvector = new PipesVector();
                nsubvector = new NodesVector();
                tsubvector = new TapsVector();




                Taps taps = (Taps) tvector.elementAt(i);
                taps.select = "open"; 
                tsubvector.addTaps(taps);

                DoSubReseau();
                n = psubvector.size() + tsubvector.size() + 1;
                m = nsubvector.size();

                if (tsubvector.size() > 0) {
                    double[] y = new double[nsubvector.size()];
                    int[][] S = getPathMatrix(nsubvector, psubvector, tsubvector);
                    int[][] Sb = getPathMatrixIntermNode(nsubvector, psubvector, tsubvector);
                    double length[] = getLength(psubvector);
                    double height[] = getHeight(nsubvector);
                    double alphaSimu[] = getAlpha(alpha, coeffOrifice, tsubvector);
                    double betaSimu[] = getBeta(nsubvector, psubvector, tsubvector);

                    // Resolution(outflow);
                    SimulFlows.run(y, F, tsubvector,psubvector, S.length, S[0].length, length, height, invest, outflow, lbd, S, Sb, nbouvert, alphaSimu, betaSimu, maxiter, tolr, tolx, nbSim, rate);
                    
                    
                    TapsStatistic(seuil2, seuil, i, y);
                    tsubvector.removeElementAt(0);
                }
                
            }
            // Les indices des noeuds sont rétablis dans l'ordre d'origine
            RevertNodeNames(nvector, pvector, tvector, toInitNodes);
        }

        /* Simulation avec seulement les robinets selectionnes */
        if (operation.equals("handmade")) { 

            Taps taps;

            for (int i = 0; i < tvector.size(); i++) {
                taps = (Taps) tvector.elementAt(i);

                if (taps.select.equals("open")) { 
                    tsubvector.addTaps(taps);
                }
            }

            DoSubReseau();
            n = psubvector.size() + tsubvector.size() + 1;
            m = nsubvector.size();

            if (tsubvector.size() > 0) {
                double[] y = new double[nsubvector.size()];
                int[][] S = getPathMatrix(nsubvector, psubvector, tsubvector);
                int[][] Sb = getPathMatrixIntermNode(nsubvector, psubvector, tsubvector);
                double length[] = getLength(psubvector);
                double height[] = getHeight(nsubvector);
                double alphaSimu[] = getAlpha(alpha, coeffOrifice, tsubvector);
                double betaSimu[] = getBeta(nsubvector, psubvector, tsubvector);
                //Resolution(outflow);
                SimulFlows.run(y, F, tsubvector,psubvector, S.length, S[0].length, length, height, invest, outflow, lbd, S, Sb, nbouvert, alphaSimu, betaSimu, maxiter, tolr, tolx, nbSim, rate);
                
                TapsStatistic(seuil2, seuil, index, y);
            }

            // Les indices des noeuds sont rétablis dans l'ordre d'origine
            RevertNodeNames(nvector, pvector, tvector, toInitNodes);

        }
    }


    public double[] getBeta(NodesVector nsubVector, PipesVector psubvector, TapsVector tsubVector) {
        double[] beta = new double[psubvector.size()];
    
        Taps taps;
        Nodes nodes;
        Vector<String> terminalNodes = new Vector<String>();
        Vector<String> intermNodes = new Vector<String>();
    
        // Collecte des nœuds terminaux
        for (int i = 0; i < tsubVector.size(); i++) {
            taps = (Taps) tsubVector.elementAt(i);
            terminalNodes.add(taps.taps);
        }
    
        // Collecte des nœuds intermédiaires
        for (int i = 0; i < nsubVector.size(); i++) {
            nodes = (Nodes) nsubVector.elementAt(i);
            if (!terminalNodes.contains(nodes.nodes) && nodes.height != 0.0) {
                intermNodes.add(nodes.nodes);
            }
        }
    
        for (int i = 0; i < psubvector.size(); i++) {
            Pipes pipes = (Pipes) psubvector.elementAt(i);
             
            int n_end;

            n_end = Integer.parseInt(pipes.nodes_end);

            // Calcul de l'index ajusté pour beta en fonction des nœuds manquants
            int adjustedIndex = n_end - 2;
            int missingCount = 0;
    
            // Comptage des nœuds manquants entre 2 et n_end - 2
            for (int j = 2; j < n_end; j++) {
                if (isNodeMissing(j, intermNodes, terminalNodes)) {
                    missingCount++;
                }
            }
    
            // Ajuster l'index pour beta en fonction des nœuds manquants
            adjustedIndex -= missingCount;
    
            if (adjustedIndex < 0 || adjustedIndex >= beta.length) {
                throw new ArrayIndexOutOfBoundsException("Adjusted index out of range: " + adjustedIndex);
            }
    
            // Mise à jour de beta à l'index ajusté
            beta[adjustedIndex] = pipes.beta1 * pipes.length * (pipes.l1 / (pipes.length * Math.pow(pipes.d1, pipes.q1)));
    
            if (pipes.d2 != 0.0) {
                beta[adjustedIndex] += pipes.beta1 * pipes.length * (1 - pipes.l1 / pipes.length) / Math.pow(pipes.d2, pipes.q1);
            }
        }
    
        return beta;
    }
    
    // Méthode pour vérifier si un nœud est manquant
    private boolean isNodeMissing(int nodeValue, Vector<String> intermNodes, Vector<String> terminalNodes) {
        String nodeStr = String.valueOf(nodeValue);
        return !intermNodes.contains(nodeStr) && !terminalNodes.contains(nodeStr);
    }
    
    
    

	public double[] getAlpha(double alpha, double coeffOrifice, TapsVector tsubvector) {
		double[] alphaSimu = new double[tsubvector.size()];

		
		for (int i = 0; i < tsubvector.size(); i++) {
			Taps taps = (Taps) tsubvector.elementAt(i);

			alphaSimu[i] = 1 / alpha + Math.pow(coeffOrifice / taps.orifice, 4);
		}

		return alphaSimu;
	}

	public double[] getLength(PipesVector psubvector) {
		double[] length = new double[psubvector.size()];
		Pipes pipes;

		for (int i = 0; i < psubvector.size(); i++) {
			pipes = (Pipes) psubvector.elementAt(i);
			length[i] = pipes.length;
		}

		return length;
	}


	public double[] getHeight(NodesVector nsubVector) {
		double[] height = new double[nsubVector.size() - 1];
		Nodes nodes;

		// on commence a l'index 1 pour ne pas prendre en compte le noeud "Source"
		for (int i = 1; i < nsubVector.size(); i++) {
			nodes = (Nodes) nsubVector.elementAt(i);
			height[i-1] = nodes.height;
		}

		return height;
	}



    public int[][] getPathMatrix(NodesVector nsubVector, PipesVector psubvector, TapsVector tsubVector) { 
        int[][] S = new int[nsubVector.size() - tsubVector.size() - 1][tsubVector.size()];
        Pipes pipes;
        Taps taps;
        Nodes nodes;
        Vector<String> terminalNodes = new Vector<String>();
        Vector<String> intermNodes = new Vector<String>();
    
        for (int i = 0; i < tsubVector.size(); i++) {
            taps = (Taps) tsubVector.elementAt(i);
            terminalNodes.add(taps.taps);
        }
    
        for (int i = 0; i < nsubVector.size(); i++) {
            nodes = (Nodes) nsubVector.elementAt(i);
            if (!terminalNodes.contains(nodes.nodes) && nodes.height != 0.0) {
                intermNodes.add(nodes.nodes);
            }
        }
    

        for (int i = psubvector.size() -1; i >= 0; i--) {
            pipes = (Pipes) psubvector.elementAt(i);
            if (terminalNodes.contains(pipes.nodes_end)) {
                String tNode = pipes.nodes_end;
                String prevNode = pipes.nodes_beg;
    
                while (prevNode != null && !prevNode.isEmpty()) {
                    if (intermNodes.indexOf(prevNode) != -1) {
                        S[intermNodes.indexOf(prevNode)][terminalNodes.indexOf(tNode)] = 1;
                    }
    
                    boolean foundNextPipe = false;
                    for (int j = i - 1; j >= 0; j--) {
                        Pipes nextPipe = (Pipes) psubvector.elementAt(j);
                        if (nextPipe.nodes_end.equals(prevNode)) {
                            prevNode = nextPipe.nodes_beg;
                            foundNextPipe = true;
                            break;
                        }
                    }
    
                    if (!foundNextPipe) {
                        break;
                    }
                }
            }
        }
    

        return S;
    }
    

    public int[][] getPathMatrixIntermNode(NodesVector nsubVector, PipesVector psubvector, TapsVector tsubVector) { 
        int[][] Sb = new int[nsubVector.size() - tsubVector.size() - 1][nsubVector.size() - tsubVector.size() - 1];
        Pipes pipes;
        Taps taps;
        Nodes nodes;
        Vector<String> terminalNodes = new Vector<String>();
        Vector<String> intermNodes = new Vector<String>();
    
        // Remplissage des nœuds terminaux
        for (int i = 0; i < tsubVector.size(); i++) {
            taps = (Taps) tsubVector.elementAt(i);
            terminalNodes.add(taps.taps);
        }
    
        // Remplissage des nœuds intermédiaires
        for (int i = 0; i < nsubVector.size(); i++) {
            nodes = (Nodes) nsubVector.elementAt(i);
            if (!terminalNodes.contains(nodes.nodes) && nodes.height != 0.0) {
                intermNodes.add(nodes.nodes);
            }
        }
    
        // Boucle sur chaque tuyau
        for (int i = psubvector.size() - 1; i >= 0; i--) {
            pipes = (Pipes) psubvector.elementAt(i);
            if (intermNodes.contains(pipes.nodes_end)) {
                String iNode = pipes.nodes_end;
                String prevNode = pipes.nodes_beg;
    
                // Marquage du nœud sur lui-même
                Sb[intermNodes.indexOf(iNode)][intermNodes.indexOf(iNode)] = 1;
    
                if (intermNodes.indexOf(prevNode) != -1) {
                    Sb[intermNodes.indexOf(prevNode)][intermNodes.indexOf(iNode)] = 1;
                }
    
                int j = i - 1;
    
                // Remontée des chemins
                while (j >= 0) {
                    pipes = (Pipes) psubvector.elementAt(j);
                    if (pipes.nodes_end.equals(prevNode)) {
                        prevNode = pipes.nodes_beg;
                        if (intermNodes.indexOf(prevNode) != -1) {
                            Sb[intermNodes.indexOf(prevNode)][intermNodes.indexOf(iNode)] = 1;
                        }
                    }
                    j--;
                }
            }
        }
    
        return Sb;
    }
    



    /* Selection un ensemble de robinet ouvert pour une simulation */
    public void SelectTaps(double rate) {
        int i;
        /*int j;
        int k;
        long index;
        int NbPip = tvector.size();*/
        Taps taps = null;

        /* on ferme tous les robinets */
        /* tous les taps sont a close */
        for (i = 0; i < tvector.size(); i++) {
            ((Taps) tvector.elementAt(i)).select = "close"; 
        }
        /* Pour chaque robinet, on tire au hasard pour savoir s'il sera ouvert */

        for (i = 0; i < tvector.size(); i++) {
        	taps = (Taps) tvector.elementAt(i);
        	if (Math.random()<rate) { 
        		taps.select = "open";
        	}
        }
        
         /* On initialise le sous vecteur de Taps */
        for (i = 0; i < tvector.size(); i++) {
            taps = (Taps) tvector.elementAt(i);

            if (taps.select.equals("open")) { 
                tsubvector.addTaps(taps);
            }
        }
    }
        
        
        
        /* On calcule le nombre de robinets a ouvrir 
        long NB = Math.round(Math.ceil(tvector.size() * rate));

        if (NB > tvector.size()) {
            NB = tvector.size();
        }

        /* on selectionne les robinets ouverts */
        /* Ce qui était programmé correspond à choisir un nombre de robinets 
        égal à l'arrondi supérieur du nombre moyen de robinets ouverts. 
        Dans la nouvelle programmation, il faudra ce nombre de robinet sera égal
        au nombre de succès. Dans la boucle for, on parcourra la liste des 
        robinets. On tirera au hasard pour savoir si le robinet est ouvert.
        Si c'est le cas on incrémentera la variable index d'une unité, sinon
        index est conservé. A la fin index sera bien égal au nb total de rob ouverts.
        Avant la boucle on initialise index = 0.
        le test est si(Math.random() < rate){
        index++  ??? pour incrémenter index
        taps = (Taps) tvector.elementAt(k)
        taps.select = "open"
        }
        
        
        
        for (i = 0; i < NB; i++) {
            index = Math.round((Math.random() * NbPip) + 0.5);
            NbPip--;
            j = 0;
            k = 0;

            while (j < index) {
                taps = (Taps) tvector.elementAt(k);
                k++;

                if (taps.select.equals("close")) { 
                    j++;
                }
            }

            taps.select = "open"; 
        }

        /* On initialise le sous vecteur de Taps 
        for (i = 0; i < tvector.size(); i++) {
            taps = (Taps) tvector.elementAt(i);

            if (taps.select.equals("open")) { 
                tsubvector.addTaps(taps);
            }
        }
    }
    */

    /* Cette procedure permet de definir le sous reseau associe
      a l'ensemble des robinets ouverts*/
    public void DoSubReseau() {
        Nodes nodes;
        Taps taps;
        Pipes pipes;

        /* On conserve tous les noeuds intermediaires */
        for (int i = 0; i < (NbNodes - NbTaps); i++) {
            nodes = (Nodes) nvector.elementAt(i);
            nsubvector.addNodes(nodes);
        }

        /* ensuite on conserve seulement les nodes
          dont le robinet est ouvert */
        for (int i = NbNodes - NbTaps; i < NbNodes; i++) {
            nodes = (Nodes) nvector.elementAt(i);
            taps = (Taps) tvector.elementAt(i - (NbNodes - NbTaps));

            if (tsubvector.contains(taps)) {
                nsubvector.addNodes(nodes);
            }
        }

        /* On conserve toutes branches intermediaires */
        for (int i = 0; i < (NbPipes - NbTaps); i++) {
            pipes = (Pipes) pvector.elementAt(i);
            psubvector.addPipes(pipes);
        }

        /* ensuite on conserve seulement les pipes
          dont le robinet est ouvert */
        for (int i = NbPipes - NbTaps; i < NbPipes; i++) {
            pipes = (Pipes) pvector.elementAt(i);
            taps = (Taps) tvector.elementAt(i - (NbPipes - NbTaps));

            if (tsubvector.contains(taps)) {
                psubvector.addPipes(pipes);
            }
        }
    }

        
    // Preparation of the NLP simulation problem
    public void Resolution(double outflow) {

    	// Look for preceding, following nodes
        for (int i = 0; i < nsubvector.size(); i++) {
            Nodes nodes = (Nodes) nsubvector.elementAt(i);
            psubvector.GetSuivPred(nodes);
        }

        y = new double[m]; /* variables duales */

        Type = new int[m];
        
        // PROBLEM SIZE
        int numcon = nsubvector.size();
        int numvar = psubvector.size() + tsubvector.size() + 1;
        int numanz = (2 * psubvector.size()) + 1 + tsubvector.size(); // Number of non-zeros
        
        // VARIABLES
        boundkey[] bkx = new boundkey[numvar]; // Variable bound type
        double[] blx = new double[numvar];
        double[] bux = new double[numvar];
        
        // Variable bounds
        for (int i = 0; i < numvar; i++) {
            blx[i] = 0;
            bux[i] = tsubvector.size() * outflow * 1e3 * 1e2;
            bkx[i] = mosek.boundkey.ra;
        }
        
        // CONSTRAINTS
        boundkey[] bkc = new boundkey[numcon]; /* types des contraintes */
        double[] blc = new double[numcon]; /* bornes inf des contraintes */
        double[] buc = new double[numcon]; /* bornes sup des contraintes */  
        
        /* Initialisation de la partie constante des contraintes */
        for (int i = 0; i < numcon; i++){
            blc[i] = 0;
            buc[i] = 0;
            bkc[i] = mosek.boundkey.fx;
        }
        
        //LINEAR component of constraints: matrix A
		double[] val = new double[numanz]; // matrix coefficient
		int[] sub = new int[numanz]; // row index
		int[] ptrb = new int[numvar]; // first coeff in column i
		int[] ptre = new int[numvar]; // last coeff in column i

		int cpt1 = 1;

		// var1
		val[0] = 1;
		sub[0] = 0;
		ptrb[0] = 0;

		// columns for pipe variables
		for (int i = 1; i < (psubvector.size() + 1); i++) {
			ptrb[i] = cpt1;
			ptre[i - 1] = ptrb[i];

			Pipes pipes = (Pipes) psubvector.elementAt(i - 1);

			for (int j = 0; j < numcon; j++) {
				Nodes nodes = (Nodes) nsubvector.elementAt(j);

				if (nodes.nodes.equalsIgnoreCase(pipes.nodes_beg)) {
					val[cpt1] = -1;
					sub[cpt1] = j;
					cpt1++;
				}

				if (nodes.nodes.equalsIgnoreCase(pipes.nodes_end)) {
					val[cpt1] = 1;
					sub[cpt1] = j;
					cpt1++;
				}
			}
		}

		// columns for tap variables
        for (int i = psubvector.size() + 1; i < numvar; i++) {
            ptrb[i] = cpt1;
            ptre[i - 1] = ptrb[i];
            val[cpt1] = -1;
            sub[cpt1] = (numcon - tsubvector.size() + i) - psubvector.size() - 1;
            cpt1++;
        }

        ptre[numvar - 1] = cpt1;
        
        
        // OBJECTIVE FUNCTION
      
        // NON-LINEAR components of the objective function
        mosek.scopr[] opro;  // which method 
        int[]         oprjo; // variable index 
        double[]      oprfo; // f constant 
        double[]      oprgo; // g constant 
        double[]      oprho; // h constant
        
        double[] PipesConst = new double[psubvector.size()]; // keep oprfo for pipes
        
        // Init, number of nl components is number of variable -1
        opro = new mosek.scopr[numvar - 1];
        oprjo = new int[numvar - 1];
        oprfo = new double[numvar - 1];
        oprgo = new double[numvar - 1];
        oprho = new double[numvar - 1];
        
        // for each pipe, the nl objective component will be 
        // oprfo * ( x + oprho )^oprgo
        for (int j = 0; j < psubvector.size(); j++) {
            Pipes pipes = (Pipes) psubvector.elementAt(j);
            
            opro[j] = mosek.scopr.pow; // funtionnal form
            oprjo[j] = j + 1;
            oprfo[j] = pipes.beta1 / (pipes.p1 + 1) * Math.pow(0.001, pipes.p1) * (pipes.l1 / Math.pow(pipes.d1, pipes.q1));
            oprgo[j] = pipes.p1 + 1;
            oprho[j] = 0.0;
            
            PipesConst[j] = oprfo[j];
        }
        
        // Coefficients of the Tap variables
        double[] TapsConst1 = new double[tsubvector.size()];
        double[] TapsConst2 = new double[tsubvector.size()];
        for (int i = 0; i < tsubvector.size(); i++) {
            Taps taps = (Taps) tsubvector.elementAt(i);
            Nodes nodes = (Nodes) nsubvector.elementAt(nsubvector.size() -
                    tsubvector.size() + i);
            TapsConst1[i] = nodes.height;
            TapsConst2[i] = ((1 / (3 * taps.faucetCoef)) +
                (Math.pow(CoefOrif, 4) / (3 * Math.pow(taps.orifice, 4)))) * 1e-6;
        }
        
        // for each tap, the nl objective component will be 
        // oprfo * ( x + oprho )^oprgo
		for (int j = psubvector.size(); j < numvar - 1; j++) {
			opro[j] = mosek.scopr.pow; // funtionnal form
			oprjo[j] = j + 1;
			oprfo[j] = TapsConst2[j - psubvector.size()];
			oprgo[j] = 3.0;
			oprho[j] = 0.0;
		}
       
        // LINEAR components of the objective function
        double[] c = new double[numvar]; // linear coef in the obj func
        
		// var1 + pipe variables
		for (int j = 0; j < psubvector.size() + 1; j++) {
			c[j] = 0;
		}
		// tap variables
		for (int j = psubvector.size() + 1; j < numvar; j++) {
			c[j] = TapsConst1[j - psubvector.size() - 1];
		}
        
        Solver solver = new Solver();

        solver.nlp(numcon, numvar, numanz,
        		bkc, blc, buc, 
        		bkx, ptrb, ptre, blx, bux, F, y, c,
            sub, val,
            oprfo, oprgo, oprho, opro, oprjo);

    }

    public double[] GetDual(double[] PipesConst, double[] TapsConst1,
        double[] TapsConst2) {
        double[] dual = new double[psubvector.size()];
        String n_beg;
        String n_end;
        Pipes pipes;
        int pos;

        for (int i = psubvector.size() - tsubvector.size();
                i < psubvector.size(); i++) {
            pipes = (Pipes) psubvector.elementAt(i);
            dual[i] = (TapsConst1[i - psubvector.size() + tsubvector.size()] * 1000) +
                ((TapsConst2[i - psubvector.size() + tsubvector.size()] * 3 * Math.pow(F[1 +
                    i], 2)) * 1000);
            n_end = pipes.nodes_end;

            while (psubvector.GetPred(n_end) != null) {
                n_beg = psubvector.GetPred(n_end);
                pos = psubvector.GetPosition(n_beg, n_end);
                pipes = (Pipes) pvector.elementAt(pos);
                dual[i] = dual[i] +
                    (Math.pow(F[pos + 1], pipes.p1) * (pipes.p1 + 1) * PipesConst[pos] * 1000);
                n_end = n_beg;
            }
        }

        return dual;
    }

    public void TapsStatistic(double seuil2, double seuil, int index, double[] y) {
        int i;
        Pipes pipes;
        Nodes nodes;
        Taps taps;

        for (i = 0; i < nsubvector.size(); i++) {
            nodes = (Nodes) nsubvector.elementAt(i);

            if (nodes.indexgrouptaps != 0) {
                int number = nsubvector.GetNumberOfOpenGroupTaps(nodes.indexgrouptaps);
                nodes = nvector.GetNodes(nodes.indexgrouptaps, number);
                i = (i + number) - 1;
            }
            

            nodes.pressim[index] = -nodes.height - y[i];

            int ind = tsubvector.getIndex(nodes.nodes);

            if (ind != -1) {
                taps = (Taps) tsubvector.elementAt(ind);
                nodes.pressim[index] = nodes.pressim[index] -
                    (Math.pow(F[psubvector.size() + 1 + ind] / 1000, 2) * ((1 / taps.faucetCoef) +
                    Math.pow(CoefOrif / taps.orifice, 4)));
            }
        }

        for (i = 0; i < psubvector.size(); i++) {
            pipes = (Pipes) psubvector.elementAt(i);

            /* if the pipe is a multitap, we search the good pipes in the
                pvector and not the psubvector */
            if (pipes.indexgrouptaps != 0) {
                int number = psubvector.GetNumberOfOpenGroupTaps(pipes.indexgrouptaps);
                pipes = pvector.GetPipes(pipes.indexgrouptaps, number);
                i = (i + number) - 1;
            }

            pipes.nbsim++;

            if (F[1 + i] < 0.005) {
                F[1 + i] = 0;
                pipes.failure = pipes.failure + 1;
            }

            pipes.simulation[index] = F[1 + i];

            /* Moyenne */
            pipes.moyenne = ((pipes.moyenne * (pipes.nbsim - 1)) + F[1 + i]) / pipes.nbsim;

            /* Moyenne carre */
            pipes.moyennec = ((pipes.moyennec * (pipes.nbsim - 1)) +
                Math.pow(F[1 + i], 2)) / pipes.nbsim;

            /* Nombre de simulation inferieur au seuil*/
            if (F[1 + i] < seuil) {
                pipes.seuil++;
            }

            /* Nombre de simulation superieur au seuil2*/
            if (F[1 + i] > seuil2) {
                pipes.seuil2++;
            }

            /* Max */
            if (F[1 + i] > pipes.max) {
                pipes.max = F[1 + i];
            }

            /* Min */
            if (pipes.nbsim == 1) {
                pipes.min = F[1 + i];
            } else {
                if (F[1 + i] < pipes.min) {
                    pipes.min = F[1 + i];
                }
            }
        }
    }


// Méthode pour trier et renommer les noms des nœuds et les réaffecter aux éléments correspondants
public HashMap<String, String> SortNodeNames(NodesVector nodes, TapsVector taps, PipesVector pipes) {

    // HashMap pour stocker les correspondances initiales des noms de nœuds
    HashMap<String, String> toInitNodes = new HashMap<>();

    // HashMap pour stocker les nouveaux noms de nœuds
    HashMap<String, String> toNewNodes = new HashMap<>();

    // Itération sur le vecteur des nœuds pour générer de nouveaux noms séquentiels
    for (int i = 0; i < nodes.size(); i++) {
        // Ajout des noms de nœuds avec leur nouvelle valeur dans la HashMap
        toNewNodes.put(((Nodes)nodes.elementAt(i)).nodes, Integer.toString(i+1));
        toInitNodes.put(Integer.toString(i+1), ((Nodes)nodes.elementAt(i)).nodes);

        // Mise à jour du nom du nœud avec son nouvel identifiant
        ((Nodes)nodes.elementAt(i)).nodes = Integer.toString(i+1);
    }

    // Mise à jour des noms des taps (valves), en tenant compte de la taille des nœuds
    for (int i = 0; i < taps.size(); i++) {
        ((Taps) taps.elementAt(i)).taps = Integer.toString(i + nodes.size() - taps.size() + 1);
    }

    // Mise à jour des informations de connectivité pour les pipes (tuyaux)
    for (int i = 0; i < pipes.size(); i++) {
        ((Pipes)pipes.elementAt(i)).nodes_beg = toNewNodes.get(((Pipes)pipes.elementAt(i)).nodes_beg); // Début du tuyau
        ((Pipes)pipes.elementAt(i)).nodes_end = toNewNodes.get(((Pipes)pipes.elementAt(i)).nodes_end); // Fin du tuyau
        ((Pipes)pipes.elementAt(i)).nodes_des = toNewNodes.get(((Pipes)pipes.elementAt(i)).nodes_des); // Destination du tuyau
    }

    // Retourne la correspondance initiale des noms de nœuds pour une réversibilité éventuelle
    return toInitNodes;
}


// Méthode pour rétablir les noms initiaux des nœuds et réaffecter ces noms aux éléments associés
public void RevertNodeNames(NodesVector nodes, PipesVector pipes, TapsVector taps, HashMap<String, String> toInitNodes) {

    // Itération sur les nœuds pour restaurer les noms initiaux
    for (int i = 0; i < nodes.size(); i++) {
        ((Nodes)nodes.elementAt(i)).nodes = toInitNodes.get(((Nodes)nodes.elementAt(i)).nodes);
    }

    // Restauration des noms initiaux des taps (valves)
    for (int i = 0; i < taps.size(); i++) {
        ((Taps)taps.elementAt(i)).taps = toInitNodes.get(((Taps) taps.elementAt(i)).taps);
    }

    // Restauration des informations de connectivité pour les pipes (tuyaux)
    for (int i = 0; i < pipes.size(); i++) {
        ((Pipes)pipes.elementAt(i)).nodes_beg = toInitNodes.get(((Pipes)pipes.elementAt(i)).nodes_beg); // Début du tuyau
        ((Pipes)pipes.elementAt(i)).nodes_end = toInitNodes.get(((Pipes)pipes.elementAt(i)).nodes_end); // Fin du tuyau
        ((Pipes)pipes.elementAt(i)).nodes_des = toInitNodes.get(((Pipes)pipes.elementAt(i)).nodes_des); // Destination du tuyau
    }
}



}
