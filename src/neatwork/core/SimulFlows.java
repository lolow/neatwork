package neatwork.core;

/* Ce programme calcule les débits stationnaires dans un réseau arborescent dont certains des
robinets peuvent être fermés. Le programme simule des configurations de robinets ouverts par
tirage Monte-Carlo ; les ouverture des robinets sont identiquement distiribuées et indépendantes
suivant un probabilité donnée p.
Les débits sont calculés par une méthode de direction de Newton réduite, proche de la méthode
de Bertsekas. Les robinets ouverts définissent un ensemble actif dont l'entrée et la sortie des
éléments se fait en ligne. Le pas est projeté sur l'orthant positif.
Une recherche linéaire à partir du pas de Newton complet est effectuée
pour garantir une décroissance suffisante de la fonction objectif suivant
un crière simple adapté de la méthode de Armijo.
Auteur. Jean-Philippe Vial 15 octobre 2022.
D. P. Bertsekas, “Projected Newton methods for optimization problems with simple constraints,”
SIAM J. Control and Optimization, vol. 20, pp. 221–246, March 1982. */

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.crypto.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import neatwork.core.defs.Pipes;
import neatwork.core.defs.PipesVector;
import neatwork.core.defs.Taps;
import neatwork.core.defs.TapsVector;

import org.apache.poi.ss.usermodel.CellType;

public class SimulFlows {

    private static final String length = null;


    public static void run (double[] dual, double[] F, TapsVector nodelist, PipesVector pipelist, int nB, int nT, double[] length, double[] h, double invest,
    double cible, double lbd, int[][] S, int[][] Sb, double[] nbouvert, double[] alpha, double[] beta,
    int maxiter, double tolr, double tolx, int simulmax, double opentaps, int indexSimu) {

        /* Paramètres pour le calcul des débits via l'optimisation
        - maxiter limite le nombre d'itérations dans le calcul des débits
        - tolr définit la convergence en terme de gradient actif.  Cette borne
        doit être en relation avec l'incertitude sur la partie fixe du gradient qu'est
        la dénivellation. Cette dernière est au mieux de l'ordre du décimètre
        de colonne d'eau (le mètre est l'unité de mesure de la pression)
        Un débit inférieur à tolx sera considéré nul.
        - tolx définit le seuil à partir duquel un débit est considéré nul.
        Les valeurs proposées ci-dessous sont en relation avec les cas traités
        par APLV. */
        Dat dat = new Dat(nB, nT, length, h, invest, cible, lbd, S, nbouvert, alpha, beta, maxiter, tolr, tolx);
    
        // Variables pour stocker les temps cumulés
        long totalHessienTime = 0;
        long totalNdirTime = 0;
        long totalAmaxTime = 0;
        long totalGradientTime = 0;
        long pressionDuration = 0;


        // // Début de la mesure du temps de prétraitement
        // long preTraitementStartTime = System.currentTimeMillis();



        /* Récolte des données et initialisation de base
        Le nom du fichier est le nom du projet + Dsg, suivi de
        _poids, _proba ouverture, _budget ; poids = kappa */

        System.out.printf("Réseau %s;   coût : %4.2f;  Diamètres : %s%n", "design_name", dat.invest, "base_name");
        
        // datsave archive la structure dat qui elle peut changer dans les simulations.
        Dat datsave = new Dat(dat);
        
        // Paramètres concernant les simulations
        // double opentaps = 0.5;
        System.out.printf("Nb de simulations %d ;   proba  %10.2f%n", simulmax, opentaps);
        
        // Sécurité sur les boucles de simulation
        if (opentaps < 0.001) {
            throw new IllegalArgumentException("opentaps doit être supérieur à 0.0001");
        }

        int k = indexSimu + 1; // Compteur du nb de simulations
        int alerte = 0; // Compteur des défaillances de convergence

        /* Initialisation du post-traitement
        iter  garde trace du nombre d'itérations par simulation
        X : stocke les débits par simulation
        Fail : stocke le nombre d'échecs par simulation (rob ne coulant pas)
        T  matrice triangulaire inférieure de 1 pour calculer les pressions.
        P : Table des sommes des charges aux nœuds intermédiaires
        Tab : Table du nombre d'échecs par simulation
        iter vecteur du nombre d'itérations par simulation */

        // int[] iter = new int[simulmax];
        // double[][] X = new double[dat.nT][simulmax];
        // // int[][] T = Utils.createLowerTriangularMatrix(dat.nB);
        // double[][] P = new double[dat.nB][simulmax];

        // int[] iter = new int[indexSimu+1];
        double[] X = new double[dat.nT];
        // int[][] T = Utils.createLowerTriangularMatrix(dat.nB);
        double[] P = new double[dat.nB];


        
        long startTime = System.currentTimeMillis();


        // // Fin de la mesure du temps de prétraitement
        // long preTraitementEndTime = System.currentTimeMillis();

        // // Calcul et affichage du temps de prétraitement
        // long preTraitementDuration = preTraitementEndTime - preTraitementStartTime;


        /* La structure dat des dat.paramètres est sauvegardée, car elle évolue à chaque simulation
        et doit être réactualisée pour la simulation suivante. Saugegarde de la sturcture */
        dat = new Dat(datsave);
        
        // Sélection des robinets fermés.
        // On répète le tirage pour avoir au moins un robinet ouvert (length(K) < dat.nT)
        List<Integer> K = new ArrayList<>();
        if (dat.nT > 1) { // on ne ferme aucun robinet dans le cas d'un seul robinet terminal
            if (opentaps < 1) {
                while (K.size() == 0 || K.size() == dat.nT) {
                    K.clear();
        
                    double[] randomVector = new double[dat.nT];
                    for (int i = 0; i < dat.nT; i++) {
                        randomVector[i] = Math.random();
                    }
        
                    for (int i = 0; i < dat.nT; i++) {
                        if (randomVector[i] > opentaps) {
                            K.add(i);
                        }
                    }
                }
            }
        }

    
        dat.nbouvert = new double[dat.nT];
        Arrays.fill(dat.nbouvert, 1.0);

        // iterations de la méthode de newton reduite
        // initialisation
        double[] x = new double[dat.nT];
        Arrays.fill(x, dat.cible);

        List<Integer> Izero = new ArrayList<>();

        double convergence = 10000;

        int i = 0;


        // Calcul du gradient et du Hessien au point de départ
        double[] y = new double[dat.S.length];

        
        for (int j = 0; j < pipelist.size() - nodelist.size(); j++) {
            Pipes pipes = (Pipes) pipelist.elementAt(j);
        
            int n_end;
        
            n_end = Integer.parseInt(pipes.nodes_end);
            
        
            for (int l = 0; l < nodelist.size(); l++) {
                y[n_end - 2] += dat.S[j][l] * x[l];
            }
        }
        

        // Timer pour Hessien
        // long hessien0StartTime = System.currentTimeMillis();
        double[] gradf = Gradient.gradient(x, y, dat, dual, Sb, pipelist);
        // long hessien0EndTime = System.currentTimeMillis();
        // totalHessienTime += hessien0EndTime - hessien0StartTime;

        // long gradient0StartTime = System.currentTimeMillis();
        double[][] H = Hessien.hessien(x, y, dat,pipelist);
        // long gradient0EndTime = System.currentTimeMillis();
        // totalGradientTime += gradient0EndTime - gradient0StartTime;


        while (i < dat.maxiter && convergence > dat.tolr) {
            // iterations de la méthode de Newton projetée-réduite
            i = i + 1;

            // Timer pour Hessien
            // long hessienStartTime = System.currentTimeMillis();
            H = Hessien.hessien(x, y, dat,pipelist);
            // long hessienEndTime = System.currentTimeMillis();
            // totalHessienTime += hessienEndTime - hessienStartTime;

            // Timer pour Ndir
            // long ndirStartTime = System.currentTimeMillis();
            Ndir.ResultNdir resultNdir = Ndir.ndir(H, gradf, Izero);
            // long ndirEndTime = System.currentTimeMillis();
            // totalNdirTime += ndirEndTime - ndirStartTime;

            // Timer pour Amax
            // long amaxStartTime = System.currentTimeMillis();
            Amax.ResultAmax resultAmax = Amax.amax(x, y, resultNdir.dx_red, gradf, resultNdir.Hred, dat,pipelist);
            x = resultAmax.x;
            // long amaxEndTime = System.currentTimeMillis();
            // totalAmaxTime += amaxEndTime - amaxStartTime;
    


            // Calculer le gradient au nouvel itéré
            y = new double[dat.S.length];
            
            
            
            for (int j = 0; j < dat.S.length; j++) {
                Pipes pipes = (Pipes) pipelist.elementAt(j);
            
                int n_end;
            
                n_end = Integer.parseInt(pipes.nodes_end);
                
            
                for (int l = 0; l < dat.S[j].length; l++) {
                    y[n_end - 2] += dat.S[j][l] * x[l];
                }
            }



            
            // Timer pour le calcul du gradient
            // long gradientStartTime = System.currentTimeMillis();
            gradf = Gradient.gradient(x, y, dat, dual, Sb, pipelist);

            // long gradientEndTime = System.currentTimeMillis();
            // totalGradientTime += gradientEndTime - gradientStartTime;

        
            List<Integer> tempIzero = new ArrayList<>();
            for (int j = 0; j < x.length; j++) {
                if (x[j] < dat.tolx && gradf[j] > 1e-3) {
                    tempIzero.add(j);
                }
            }
        
            Izero = tempIzero; // Mise à jour de Izero avec les éléments filtrés
        
            // Calcul du résidu et de la convergence
            double[] residu = Arrays.copyOf(gradf, gradf.length);
            for (Integer index : Izero) {
                residu[index] = Math.min(residu[index], 0);
            }
            double maxAbsResidu = Arrays.stream(residu).map(Math::abs).max().orElse(0.0);
            convergence = maxAbsResidu;


        }

        
        // Archivage des résultats pour traitement statistique ultérieur
        if (i >= dat.maxiter) {
            alerte++;
            k--;
        }
        else {

            // Début de la mesure du temps des pressions
            // long pressionStartTime = System.currentTimeMillis();


            nT = datsave.nT;
            nB = datsave.nB;
            double[] xstore = new double[nT];
            Arrays.fill(xstore, -1e-4); // pour permettre l'identification des robinets fermés.

            List<Integer> ensembleTotal = new ArrayList<>();
            for (int j = 0; j < nT; j++) {
                ensembleTotal.add(j);
            }

            // ensemble des robinets ouverts
            List<Integer> Ko = new ArrayList<>(ensembleTotal);
            List<Integer> listK = new ArrayList<>();
            //listK.addAll(K);
            for (int value : K) {
                listK.add(value);
            }
            Ko.removeAll(listK);

            for (int j = 0; j < Ko.size(); j++) {
                xstore[Ko.get(j)] = x[j];
            }

            for (int j = 0; j < nT; j++) {
                X[j] = xstore[j];
            }

            // iter[k - 1] = i;


            // Calcul des pressions aux nœuds intermédiaires
            double[] pb = new double[nB];
            for (int j = 0; j < nB; j++) {
                pb[j] = dat.beta[j] * Math.pow(y[j], dat.lbd);
            }

            int n = Sb.length;  // Nombre de lignes de la matrice S
            int m = Sb[0].length;  // Nombre de colonnes de la matrice S

            // Créer une liste pour stocker les paires (node_end, index)
            List<int[]> nodeList = new ArrayList<>();

            for (int f = 0; f < pipelist.size(); f++) {
                if (nodeList.size() >= n) {
                    break;  // Ne pas ajouter plus d'entrées que la taille de dat.S
                }

                Pipes pipes = (Pipes) pipelist.elementAt(f);
                int n_end;

                n_end = Integer.parseInt(pipes.nodes_end);
                

                // Ajouter le node_end et l'index dans la liste
                nodeList.add(new int[]{n_end, f});
            }

            // Trier la liste par node_end en ordre croissant
            Collections.sort(nodeList, Comparator.comparingInt(a -> a[0]));

            // Réorganiser les lignes de la matrice S en fonction de nodeList trié
            int[][] reorderedS = new int[n][m];
            for (int f = 0; f < nodeList.size(); f++) {
                int correctIndex = nodeList.get(f)[1];  // Obtenir l'index correct
                for (int j = 0; j < m; j++) {
                    reorderedS[f][j] = Sb[correctIndex][j];  // Copier chaque élément de la ligne
                }
            }


            // Calcul des pressions aux nœuds intermédiaires en utilisant la matrice transposée
            double[] ch = Utils.multiplyVectorAndMatrixTransposed(reorderedS, pb); // Méthode correcte basée sur la matrice des chemins.

            // Mise à jour des pressions pour les nœuds intermédiaires et terminaux
            for (int j = 0; j < nB; j++) {
                P[j] = ch[j] + dat.h[j]; // Ajout de la hauteur aux pressions calculées.
            }

            for (int p = 1; p < dat.S.length; p++) {
                dual[p] = P[p];
            }


            // // Fin de la mesure du temps des pressions
            // long pressionEndTime = System.currentTimeMillis();

            // // Calcul et affichage du temps des pressions
            // pressionDuration += pressionEndTime - pressionStartTime;

            // // Affichage du temps écoulé
            // System.out.printf("Temps pour pressionDuration : %d ms%n", pressionDuration);


        }

        

        // }


        long TempsCalcul = System.currentTimeMillis() - startTime;

        System.out.printf("Temps de calcul %2.2f secondes, divergences %d%n", TempsCalcul / 1000.0, alerte);

        // // Affichage des temps cumulés
        // System.out.println("Temps total d'exécution de Hessien: " + totalHessienTime + " ms");
        // System.out.println("Temps total d'exécution de Ndir: " + totalNdirTime + " ms");
        // System.out.println("Temps total d'exécution de Amax: " + totalAmaxTime + " ms");
        // System.out.println("Temps total d'exécution du calcul du gradient: " + totalGradientTime + " ms");
        // System.out.println("Temps de prétraitement : " + preTraitementDuration + " ms");
        // System.out.println("pressionDuration : " + pressionDuration + " ms");


        //Post-traitement

        // Début de la mesure du temps pour le remplissage de F
        // long fillFStartTime = System.currentTimeMillis();
    
        for (int j = 0; j < dat.nT; j++) {
            // Vérifiez si la valeur de X[j] est valide, sinon mettez à 0
            F[1 + j + dat.nB] = X[j] >= 0 ? X[j] * 1000 : 0.0; // Conversion si nécessaire
        }
    
        // // Fin de la mesure du temps pour le remplissage de F
        // long fillFEndTime = System.currentTimeMillis();
        // long fillFDuration = fillFEndTime - fillFStartTime;

    
        // Affichage du temps écoulé
        // System.out.printf("Temps pour remplir F : %d ms%n", fillFDuration);

        
    }
        
}