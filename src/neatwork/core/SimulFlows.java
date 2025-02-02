package neatwork.core;

/* Ce programme calcule les dÃ©bits stationnaires dans un rÃ©seau arborescent dont certains des
robinets peuvent Ãªtre fermÃ©s. Le programme simule des configurations de robinets ouverts par
tirage Monte-Carlo ; les ouverture des robinets sont identiquement distiribuÃ©es et indÃ©pendantes
suivant un probabilitÃ© donnÃ©e p.
Les dÃ©bits sont calculÃ©s par une mÃ©thode de direction de Newton rÃ©duite, proche de la mÃ©thode
de Bertsekas. Les robinets ouverts dÃ©finissent un ensemble actif dont l'entrÃ©e et la sortie des
Ã©lÃ©ments se fait en ligne. Le pas est projetÃ© sur l'orthant positif.
Une recherche linÃ©aire Ã  partir du pas de Newton complet est effectuÃ©e
pour garantir une dÃ©croissance suffisante de la fonction objectif suivant
un criÃ¨re simple adaptÃ© de la mÃ©thode de Armijo.
Auteur. Jean-Philippe Vial 15 octobre 2022.
D. P. Bertsekas, â€œProjected Newton methods for optimization problems with simple constraints,â€�
SIAM J. Control and Optimization, vol. 20, pp. 221â€“246, March 1982. */

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.ArrayList;

import neatwork.core.defs.Pipes;
import neatwork.core.defs.PipesVector;

import neatwork.core.defs.TapsVector;

public class SimulFlows {

    //private static final String length = null;

    public static void run (double[] dual, double[] F, TapsVector nodelist, PipesVector pipelist, int nB, int nT, double[] length, double[] h, double invest,
    double cible, double lbd, int[][] S, int[][] Sb, double[] nbouvert, double[] alpha, double[] beta,
    int maxiter, double tolr, double tolx, int simulmax, double opentaps, int indexSimu) {

        /* ParamÃ¨tres pour le calcul des dÃ©bits via l'optimisation
        - maxiter limite le nombre d'itÃ©rations dans le calcul des dÃ©bits
        - tolr dÃ©finit la convergence en terme de gradient actif.  Cette borne
        doit Ãªtre en relation avec l'incertitude sur la partie fixe du gradient qu'est
        la dÃ©nivellation. Cette derniÃ¨re est au mieux de l'ordre du dÃ©cimÃ¨tre
        de colonne d'eau (le mÃ¨tre est l'unitÃ© de mesure de la pression)
        Un dÃ©bit infÃ©rieur Ã  tolx sera considÃ©rÃ© nul.
        - tolx dÃ©finit le seuil Ã  partir duquel un dÃ©bit est considÃ©rÃ© nul.
        Les valeurs proposÃ©es ci-dessous sont en relation avec les cas traitÃ©s
        par APLV. */
        Dat dat = new Dat(nB, nT, length, h, invest, cible, lbd, S, nbouvert, alpha, beta, maxiter, tolr, tolx);
    
        // Variables pour stocker les temps cumulÃ©s
        // long totalHessienTime = 0;
        // long totalNdirTime = 0;
        // long totalAmaxTime = 0;
        // long totalGradientTime = 0;
        // long pressionDuration = 0;


        // // DÃ©but de la mesure du temps de prÃ©traitement
        // long preTraitementStartTime = System.currentTimeMillis();



        /* RÃ©colte des donnÃ©es et initialisation de base
        Le nom du fichier est le nom du projet + Dsg, suivi de
        _poids, _proba ouverture, _budget ; poids = kappa */

        System.out.printf("RÃ©seau %s;   coÃ»t : %4.2f;  DiamÃ¨tres : %s%n", "design_name", dat.invest, "base_name");
        
        // datsave archive la structure dat qui elle peut changer dans les simulations.
        Dat datsave = new Dat(dat);
        
        // ParamÃ¨tres concernant les simulations
        // double opentaps = 0.5;
        System.out.printf("Nb de simulations %d ;   proba  %10.2f%n", simulmax, opentaps);
        
        // SÃ©curitÃ© sur les boucles de simulation
        if (opentaps < 0.001) {
            throw new IllegalArgumentException("opentaps doit Ãªtre supÃ©rieur Ã  0.0001");
        }

        //int k = indexSimu + 1; // Compteur du nb de simulations
        int alerte = 0; // Compteur des dÃ©faillances de convergence

        /* Initialisation du post-traitement
        iter  garde trace du nombre d'itÃ©rations par simulation
        X : stocke les dÃ©bits par simulation
        Fail : stocke le nombre d'Ã©checs par simulation (rob ne coulant pas)
        T  matrice triangulaire infÃ©rieure de 1 pour calculer les pressions.
        P : Table des sommes des charges aux nÅ“uds intermÃ©diaires
        Tab : Table du nombre d'Ã©checs par simulation
        iter vecteur du nombre d'itÃ©rations par simulation */

        double[] P = new double[dat.nB];
        
        long startTime = System.currentTimeMillis();

        /* La structure dat des dat.paramÃ¨tres est sauvegardÃ©e, car elle Ã©volue Ã  chaque simulation
        et doit Ãªtre rÃ©actualisÃ©e pour la simulation suivante. Saugegarde de la sturcture */
        dat = new Dat(datsave);
        
        dat.nbouvert = new double[dat.nT];
        Arrays.fill(dat.nbouvert, 1.0);

        // iterations de la mÃ©thode de newton reduite
        // initialisation
        double[] x = new double[dat.nT];
        Arrays.fill(x, dat.cible);

        List<Integer> Izero = new ArrayList<>();

        double convergence = 10000;

        int i = 0;


        // Calcul du gradient et du Hessien au point de dÃ©part
        double[] y = new double[dat.S.length];

        
        for (int j = 0; j < pipelist.size() - nodelist.size(); j++) {
            Pipes pipes = (Pipes) pipelist.elementAt(j);
        
            int n_end;
        
            n_end = Integer.parseInt(pipes.nodes_end);
            
        
            for (int l = 0; l < nodelist.size(); l++) {
                y[n_end - 2] += dat.S[j][l] * x[l];
            }
        }
        
        double[] gradf = Gradient.gradient(x, y, dat, dual, Sb, pipelist);

        double[][] H = Hessien.hessien(x, y, dat,pipelist);



        while (i < dat.maxiter && convergence > dat.tolr) {
            // iterations de la mÃ©thode de Newton projetÃ©e-rÃ©duite
            i = i + 1;

            H = Hessien.hessien(x, y, dat,pipelist);

            Ndir.ResultNdir resultNdir = Ndir.ndir(H, gradf, Izero);

            Amax.ResultAmax resultAmax = Amax.amax(x, y, resultNdir.dx_red, gradf, resultNdir.Hred, dat,pipelist);
            x = resultAmax.x;


            // Calculer le gradient au nouvel itÃ©rÃ©
            y = new double[dat.S.length];
            
            
            
            for (int j = 0; j < dat.S.length; j++) {
                Pipes pipes = (Pipes) pipelist.elementAt(j);
            
                int n_end;
            
                n_end = Integer.parseInt(pipes.nodes_end);
                
            
                for (int l = 0; l < dat.S[j].length; l++) {
                    y[n_end - 2] += dat.S[j][l] * x[l];
                }
            }



            
            gradf = Gradient.gradient(x, y, dat, dual, Sb, pipelist);

            List<Integer> tempIzero = new ArrayList<>();
            for (int j = 0; j < x.length; j++) {
                if (x[j] < dat.tolx && gradf[j] > 1e-3) {
                    tempIzero.add(j);
                }
            }
        
            Izero = tempIzero; // Mise Ã  jour de Izero avec les Ã©lÃ©ments filtrÃ©s
        
            // Calcul du rÃ©sidu et de la convergence
            double[] residu = Arrays.copyOf(gradf, gradf.length);
            for (Integer index : Izero) {
                residu[index] = Math.min(residu[index], 0);
            }
            double maxAbsResidu = Arrays.stream(residu).map(Math::abs).max().orElse(0.0);
            convergence = maxAbsResidu;


        }



        // Calcul des pressions aux nÅ“uds intermÃ©diaires
        double[] pb = new double[nB];
        for (int j = 0; j < nB; j++) {
            pb[j] = dat.beta[j] * Math.pow(y[j], dat.lbd);
        }

        int n = Sb.length;  // Nombre de lignes de la matrice S
        int m = Sb[0].length;  // Nombre de colonnes de la matrice S

        // CrÃ©er une liste pour stocker les paires (node_end, index)
        List<int[]> nodeList = new ArrayList<>();

        for (int f = 0; f < pipelist.size(); f++) {
            if (nodeList.size() >= n) {
                break;  // Ne pas ajouter plus d'entrÃ©es que la taille de dat.S
            }

            Pipes pipes = (Pipes) pipelist.elementAt(f);
            int n_end;

            n_end = Integer.parseInt(pipes.nodes_end);
            

            // Ajouter le node_end et l'index dans la liste
            nodeList.add(new int[]{n_end, f});
        }

        // Trier la liste par node_end en ordre croissant
        Collections.sort(nodeList, Comparator.comparingInt(a -> a[0]));

        // RÃ©organiser les lignes de la matrice S en fonction de nodeList triÃ©
        int[][] reorderedS = new int[n][m];
        for (int f = 0; f < nodeList.size(); f++) {
            int correctIndex = nodeList.get(f)[1];  // Obtenir l'index correct
            for (int j = 0; j < m; j++) {
                reorderedS[f][j] = Sb[correctIndex][j];  // Copier chaque Ã©lÃ©ment de la ligne
            }
        }


        // Calcul des pressions aux nÅ“uds intermÃ©diaires en utilisant la matrice transposÃ©e
        double[] ch = Utils.multiplyVectorAndMatrixTransposed(reorderedS, pb); // MÃ©thode correcte basÃ©e sur la matrice des chemins.

        // Mise Ã  jour des pressions pour les nÅ“uds intermÃ©diaires et terminaux
        for (int j = 0; j < nB; j++) {
            P[j] = ch[j] + dat.h[j]; // Ajout de la hauteur aux pressions calculÃ©es.
        }

        for (int p = 1; p < dat.S.length; p++) {
            dual[p] = P[p];
        }




        long TempsCalcul = System.currentTimeMillis() - startTime;

        System.out.printf("Temps de calcul %2.2f secondes, divergences %d%n", TempsCalcul / 1000.0, alerte);


        //Post-traitement

        for (int j = 0; j < dat.nT; j++) {
            // VÃ©rifiez si la valeur de X[j] est valide, sinon mettez Ã  0
            F[1 + j + dat.nB] = x[j] >= 0 ? x[j] * 1000 : 0.0; // Conversion si nÃ©cessaire
        }

        
    }
        
}