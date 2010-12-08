package neatwork.core.run;

import neatwork.core.defs.Nodes;
import neatwork.core.defs.NodesVector;
import neatwork.core.defs.Pipes;
import neatwork.core.defs.PipesVector;
import neatwork.core.defs.Taps;
import neatwork.core.defs.TapsVector;
import neatwork.solver.AbstractSolver;
import neatwork.solver.Solver;


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

    /*variables de dimensionnement du probl�me en question*/
    public int NbDiam;
    public int NbPipes;
    public int NbNodes;
    public int NbTaps;
    public int n;
    public int m;

    /*Bornes inf�rieures et sup�rieures sur les variables*/
    public double[] Lower;
    public double[] Upper;
    public double[] F;
    public double[] y;
    public double[] Dual;

    /*Types et valeurs des Constantes des contraintes*/
    public int[] Type;
    public double[] Cste;
    public boolean TypePb = true;

    //public int MOSEKKEY[];
    public RunSimulation(double[] f, NodesVector nv, PipesVector pv,
        TapsVector tv, double outflow, double rate, double seuil,
        double seuil2, String operation, int index, double CoefOrif1) {
        this(f, nv, pv, tv, outflow, rate, seuil, seuil2, operation, index,
            CoefOrif1, null);
    }

    /* Constructeur */
    public RunSimulation(double[] f, NodesVector nv, PipesVector pv,
        TapsVector tv, double outflow, double rate, double seuil,
        double seuil2, String operation, int index, double CoefOrif1,
        AbstractSolver solver) {
        /* Cr�ation des sous vecteurs de pipes, de nodes et de taps */
        psubvector = new PipesVector();
        nsubvector = new NodesVector();
        tsubvector = new TapsVector();

        /* Initialisation des veceurs pour cette classe */
        pvector = pv;
        nvector = nv;
        tvector = tv;
        F = f; /* Vecteurs de flots r�sultant de l'optimisation */

        //alpha = alpha1; /* param�tres de r�solution */
        CoefOrif = CoefOrif1;

        /* dimensions des vecteurs pipes, nodes et taps*/
        NbPipes = pvector.size();
        NbNodes = nvector.size();
        NbTaps = tvector.size();

        /* selection aleatoire des robinets ouverts */
        if (operation.equals("random")) { //$NON-NLS-1$

            /* On selection un ensemble de robinets ouverts et on initialise
              le sous vecteur de taps*/
            SelectTaps(rate);

            /* On cr�e le sous r�seau (sous vecteur de pipes et de nodes)*/
            DoSubReseau();

            /* Nombre de variables */
            n = psubvector.size() + tsubvector.size() + 1;

            /* Nombre de contraintes */
            m = nsubvector.size();

            /* proc�dure de r�solution */
            Resolution(outflow);
            TapsStatistic(seuil2, seuil, index);
        }

        /* simulations des robinets un par un */
        if (operation.equals("tapbytap")) { //$NON-NLS-1$

            for (int i = 0; i < tvector.size(); i++) {
                //ligne de progression
                solver.setProgress(Math.round((float) i / tvector.size() * 100));

                psubvector = new PipesVector();
                nsubvector = new NodesVector();
                tsubvector = new TapsVector();

                Taps taps = (Taps) tvector.elementAt(i);
                taps.select = "open"; //$NON-NLS-1$
                tsubvector.addTaps(taps);

                DoSubReseau();
                n = psubvector.size() + tsubvector.size() + 1;
                m = nsubvector.size();

                Resolution(outflow);
                TapsStatistic(seuil2, seuil, i);
                tsubvector.removeElementAt(0);
            }
        }

        /* Simulation avec seulement les robinets selectionn�s */
        if (operation.equals("handmade")) { //$NON-NLS-1$

            Taps taps;

            for (int i = 0; i < tvector.size(); i++) {
                taps = (Taps) tvector.elementAt(i);

                if (taps.select.equals("open")) { //$NON-NLS-1$
                    tsubvector.addTaps(taps);
                }
            }

            DoSubReseau();
            n = psubvector.size() + tsubvector.size() + 1;
            m = nsubvector.size();

            Resolution(outflow);
            TapsStatistic(seuil2, seuil, index);
        }
    }

    /* Selection un ensemble de robinet ouvert pour une simulation */
    public void SelectTaps(double rate) {
        int i;
        int j;
        int k;
        long index;
        int NbPip = tvector.size();
        Taps taps = null;

        /* on ferme tous les robinets */
        /* tous les taps sont a close */
        for (i = 0; i < tvector.size(); i++) {
            ((Taps) tvector.elementAt(i)).select = "close"; //$NON-NLS-1$
        }

        /* On calcule le nombre de robinets a ouvrir */
        long NB = Math.round(Math.ceil(tvector.size() * rate));

        if (NB > tvector.size()) {
            NB = tvector.size();
        }

        /* on selectionne les robinets ouverts */
        for (i = 0; i < NB; i++) {
            index = Math.round((Math.random() * NbPip) + 0.5);
            NbPip--;
            j = 0;
            k = 0;

            while (j < index) {
                taps = (Taps) tvector.elementAt(k);
                k++;

                if (taps.select.equals("close")) { //$NON-NLS-1$
                    j++;
                }
            }

            taps.select = "open"; //$NON-NLS-1$
        }

        /* On initialise le sous vecteur de Taps */
        for (i = 0; i < tvector.size(); i++) {
            taps = (Taps) tvector.elementAt(i);

            if (taps.select.equals("open")) { //$NON-NLS-1$
                tsubvector.addTaps(taps);
            }
        }
    }

    /* Cette proc�dure permet de d\uFFFDfinir le sous r�seau associ�
      � l'ensemble des robinets ouverts*/
    public void DoSubReseau() {
        Nodes nodes;
        Taps taps;
        Pipes pipes;

        /* On conserve tous les noeuds interm�diaires */
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

        /* On conserve toutes branches interm�diaires */
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

    /* Cette proc�dure initialise tous les vecteurs de donn�es n�cessaires
      � Mosek pour la r�solution d'une simulation */
    public void Resolution(double outflow) {
        /*Recherhce des noeuds suivant et precedant de tous les noeuds */
        for (int i = 0; i < nsubvector.size(); i++) {
            Nodes nodes = (Nodes) nsubvector.elementAt(i);
            psubvector.GetSuivPred(nodes);
        }

        /* D�claration de toutes les variables Mosek */
        int[] bkc = new int[m]; /* types des contraintes */
        double[] blc = new double[m]; /* bornes inf des contraintes */
        double[] buc = new double[m]; /* bornes sup des contraintes */
        double[] c = new double[n]; /* coeff des variables lin\uFFFDaires dans la fonction obj*/
        int[] bkx = new int[n]; /* types des variables */
        double[] oprfo = new double[n - 1];
        double[] oprgo = new double[n - 1];
        double[] oprho = new double[n - 1];
        int[] opro = new int[n - 1];
        int[] oprjo = new int[n - 1];

        y = new double[m]; /* variables duales */

        Lower = new double[n];
        Upper = new double[n];

        Type = new int[m];
        Cste = new double[m];

        /* Initialisation des bornes des variables */
        InitializeVariable(outflow);

        /* Initialisation de la partie constante des contraintes */
        InitializeConstraints();

        /* Coefficients de la matrice A */
        double[] CstGradCoeff = new double[(2 * psubvector.size()) + 1 +
            tsubvector.size()];

        /* Indice de ligne du coeff de la matrice A */
        int[] CstGradIRow = new int[(2 * psubvector.size()) + 1 +
            tsubvector.size()];

        /* Indice du premier coeff de la colonne i */
        int[] CstGradIColumn = new int[n];

        /* Indice du dernier coeff de la colonne i */
        int[] CstGradIColumn2 = new int[n];
        MatriceA(CstGradCoeff, CstGradIRow, CstGradIColumn, CstGradIColumn2);

        /* les valeurs des coefficients dans la fonction objective
         * pour les variables Pipes et les variables Taps
         */
        double[] PipesConst = new double[psubvector.size()];
        double[] TapsConst1 = new double[tsubvector.size()];
        double[] TapsConst2 = new double[tsubvector.size()];
        Cvector(PipesConst, TapsConst1, TapsConst2);

        for (int j = 0; j < psubvector.size(); j++) {
            Pipes pipes = (Pipes) psubvector.elementAt(j);
            opro[j] = 3;
            oprjo[j] = j + 1;
            oprfo[j] = PipesConst[j];
            oprgo[j] = pipes.p1 + 1;
            oprho[j] = 0.0;
        }

        int L = (2 * psubvector.size()) + 1 + tsubvector.size();

        Solver solver = new Solver();

        solver.mainnlp(m, n, L, psubvector.size(), bkc, blc, buc, bkx,
            CstGradIColumn, CstGradIColumn2, Lower, Upper, F, y, c,
            CstGradIRow, CstGradCoeff, PipesConst, TapsConst1, TapsConst2,
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

    /* Initialise la partie constante des contraintes */
    public void InitializeConstraints() {
        for (int i = 0; i < m; i++)
            Cste[i] = 0;
    }

    /* Initialise les bornes inf et sup des varaibles */
    public void InitializeVariable(double outflow) {
        for (int i = 0; i < n; i++) {
            Lower[i] = 0;
            Upper[i] = tsubvector.size() * outflow * 1000 * 100;
        }
    }

    /* Cette proc\uFFFDdure calcule tous les coeff de la fonction objective */
    public void Cvector(double[] PipesConst, double[] TapsConst1,
        double[] TapsConst2) {
        for (int i = 0; i < psubvector.size(); i++) {
            Pipes pipes = (Pipes) psubvector.elementAt(i);

            PipesConst[i] = pipes.beta1 / (pipes.p1 + 1) * Math.pow(0.001,
                    pipes.p1) * (pipes.l1 / Math.pow(pipes.d1, pipes.q1));
        }

        for (int i = 0; i < tsubvector.size(); i++) {
            Taps taps = (Taps) tsubvector.elementAt(i);
            Nodes nodes = (Nodes) nsubvector.elementAt(nsubvector.size() -
                    tsubvector.size() + i);
            TapsConst1[i] = nodes.height;
            TapsConst2[i] = ((1 / (3 * taps.faucetCoef)) +
                (Math.pow(CoefOrif, 4) / (3 * Math.pow(taps.orifice, 4)))) * 0.000001;
        }
    }

    /* Cette proc\uFFFDdure initialise la matrice A du probl\uFFFDme de
      simulation */
    public void MatriceA(double[] CstGradCoeff, int[] CstGradIRow,
        int[] CstGradIColumn, int[] CstGradIColumn2) {
        int cpt1 = 1;
        CstGradCoeff[0] = 1;
        CstGradIRow[0] = 0;
        CstGradIColumn[0] = 0;

        for (int i = 1; i < (psubvector.size() + 1); i++) {
            CstGradIColumn[i] = cpt1;
            CstGradIColumn2[i - 1] = CstGradIColumn[i];

            Pipes pipes = (Pipes) psubvector.elementAt(i - 1);

            for (int j = 0; j < m; j++) {
                Nodes nodes = (Nodes) nsubvector.elementAt(j);

                if (nodes.nodes.equalsIgnoreCase(pipes.nodes_beg)) {
                    CstGradCoeff[cpt1] = -1;
                    CstGradIRow[cpt1] = j;
                    cpt1++;
                }

                if (nodes.nodes.equalsIgnoreCase(pipes.nodes_end)) {
                    CstGradCoeff[cpt1] = 1;
                    CstGradIRow[cpt1] = j;
                    cpt1++;
                }
            }
        }

        for (int i = psubvector.size() + 1; i < n; i++) {
            CstGradIColumn[i] = cpt1;
            CstGradIColumn2[i - 1] = CstGradIColumn[i];
            CstGradCoeff[cpt1] = -1;
            CstGradIRow[cpt1] = (m - tsubvector.size() + i) -
                psubvector.size() - 1;
            cpt1++;
        }

        CstGradIColumn2[n - 1] = cpt1;
    }

    public void TapsStatistic(double seuil2, double seuil, int index) {
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
}
