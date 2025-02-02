package neatwork.core;

import neatwork.core.defs.PipesVector;

public class Gradient {

    // Calcul des dérivées premières de l'objectif
    public static double[] gradient(double[] x, double[] y, Dat dat, double[] dual, int[][] Sb, PipesVector pipelist) {


        int nB = dat.S.length;
        int nT = dat.S[0].length;
        int n = nB + nT;

        // Friction sur les arcs intermédiaires en fonction des débits y
        double[] dF_y = new double[nB];
        for (int i = 0; i < nB; i++) {
            dF_y[i] = dat.beta[i] * Math.pow(y[i], dat.lbd);
        }

        // //mesurer le temps exécuté
        // long startTime = System.currentTimeMillis();
        // // Même friction exprimée en fonction des débits terminaux x (y=S*x)
        // double[] dF_x = new double[nT];
        // for (int i = 0; i < nT; i++) {
            
            
        //     for (int j = 0; j < nB; j++) {
        //         int n_end = -1; // Initialiser à une valeur invalide
        //         int n_index = -1; // Initialiser à une valeur invalide
            
        //         // Rechercher le nœud dans pipelist correspondant à j + 2
        //         for (int o = 0; o < pipelist.size(); o++) {
        //             Pipes pipes = (Pipes) pipelist.elementAt(o);
                    

        //             n_end = Integer.parseInt(pipes.nodes_end);
                   
        //             // Vérifier si n_end correspond à j + 2
        //             if (n_end == j + 2) {
        //                 n_index = o;
        //                 // On a trouvé le bon nœud, sortir de la boucle
        //                 break;
        //             }
        //         }
            
        //         // Si on a trouvé un nœud correspondant, effectuer les opérations
        //         if (n_end == j + 2) {
        //             dF_x[i] += dat.S[n_index][i] * dF_y[j];
        //         }
        //     }
            
        // }
        // //fin du temps exécuté
        // long endTime = System.nanoTime();
        // System.out.println("Temps exécuté dF_x : " + (endTime - startTime) + " ns");





        //mesurer le temps exécuté
        // long startTime = System.currentTimeMillis();
        // Même friction exprimée en fonction des débits terminaux x (y=S*x)
        double[] dF_x = new double[nT];
        for (int i = 0; i < nT; i++) {
            for (int j = 0; j < nB; j++) {
                dF_x[i] += dat.S[j][i] * dF_y[j];
            }
            
        }
        //fin du temps exécuté
        // long endTime = System.currentTimeMillis();
        // System.out.println("Temps exécuté dF_x : " + (endTime - startTime) + " ns");


        


        // mesurer temps exécution 
        // long start = System.currentTimeMillis();


        // Pertes de charges cumulées en chaque nœud intermédiaire
        double[] F_y = new double[nB];

        for (int i = 0; i < nB; i++) {
            for (int j = 0; j < nB; j++) {

                F_y[i] += Sb[j][i] * dF_y[j];

            }
        }

        //fin exécution temps
        // long end = System.currentTimeMillis();
        // System.out.println("Temps exécuté F_y : " + (end - start) + " ms");





        // // Pertes de charges cumulées en chaque nœud intermédiaire
        // double[] F_y = new double[nB];

        // for (int i = 0; i < nB; i++) {
        //     for (int j = 0; j < nB; j++) {
        //         int n_end = -1; // Initialiser à une valeur invalide

        //         // Rechercher le nœud dans pipelist correspondant à j + 2
        //         for (int o = 0; o < pipelist.size(); o++) {
        //             Pipes pipes = (Pipes) pipelist.elementAt(o);


        //             n_end = Integer.parseInt(pipes.nodes_end);

        //             // Vérifier si n_end correspond à j + 2
        //             if (n_end == j + 2) {
        //                 // On a trouvé le bon nœud, sortir de la boucle
        //                 break;
        //             }
        //         }

        //         // Si on a trouvé un nœud correspondant, effectuer les opérations
        //         if (n_end == j + 2) {
        //             F_y[i] += Sb[j][i] * dF_y[j];
        //         } else {
        //             // Gérer le cas où aucun nœud n'a été trouvé correspondant à j + 2
        //             throw new IllegalArgumentException("Aucun nœud correspondant trouvé pour j + 2 = " + (j + 2));
        //         }
        //     }
        // }

        // //fin exécution temps
        // long end = System.currentTimeMillis();
        // System.out.println("Temps exécuté F_y : " + (end - start) + " ms");






        // Ajout de la friction sur les arcs terminaux
        for (int i = 0; i < nT; i++) {
            dF_x[i] += dat.beta[i + nB] * Math.pow(dat.nbouvert[i] * x[i], dat.lbd);
        }

        // Pertes de charge aux robinets et aux orifices
        double[] dG_x = new double[nT];
        for (int i = 0; i < nT; i++) {
            dG_x[i] = dat.alpha[i] * Math.pow(x[i], 2);
        }

        // La pression à la sortie du robinet
        double[] pression = new double[nT];
        for (int i = 0; i < nT; i++) {
            pression[i] = dF_x[i] + dG_x[i];
        }

        double[] gradf = new double[nT];
        for (int i = nB; i < n; i++) {
            gradf[i - nB] = pression[i - nB] + dat.h[i];
        }

        // Pertes de charge pour chaque noeud de l'arbre (on concatene les intermediaires puis les terminaux)
        dual[0] = 0.0; // Valeur nulle pour la source
        for (int i = 1; i < nB+1; i++) {
            dual[i] = F_y[i - 1];
        }
        for (int i = nB+1; i < n+1; i++) {
            dual[i] = pression[i - nB -1];
        }

        return gradf;
    }
}
