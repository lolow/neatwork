package neatwork.core.run;

import neatwork.core.defs.*;


/** classe core : ne doit jamais etre appel\u00E9 directement*/
public class Decoupe {
    public Decoupe(double[] x, double lcom, int NbPipes,
        DiametersVector dvector, PipesVector pvector) {
        int NbDiam = dvector.size();
        int indice1 = 0;
        int indice2 = -1;
        double d1 = 0;
        double d2 = 0;
        double l1;
        double l2;
        double E1;
        double E2;
        Diameters diam;
        Pipes pipes;

        // Pour tout les pipes du reseau
        for (int i = 0; i < NbPipes; i++) {
            pipes = (Pipes) pvector.elementAt(i);
            pipes.l2 = 0;
            indice2 = -1;

            int j = 0;

            // on recupere la premiere longueur de tuyau
            while ((j < NbDiam) && (x[(i * NbDiam) + j] < 0.1)) {
                j++;
            }

            if (j != NbDiam) {
                indice1 = (i * NbDiam) + j;
                diam = (Diameters) dvector.elementAt(j);
                d1 = diam.diam;
                pipes.d1 = d1;
                pipes.l1 = x[indice1];
                pipes.p1 = diam.p;
                pipes.q1 = diam.q;
                pipes.beta1 = diam.beta;

                /////LOLOW ADDITION////////////////////////////////////////
                pipes.refDiam1 = "" + j; //$NON-NLS-1$

                ///////////////////////////////////////////////////////////
                j++;
            }

            // on recupere la deuxieme longueur de tuyau si elle existe
            while ((j < NbDiam) && (x[(i * NbDiam) + j] < 0.1)) {
                j++;
            }

            // si il y a une deuxieme longeur on la sauvegarde
            if (j != NbDiam) {
                pipes.d2 = pipes.d1;
                pipes.l2 = pipes.l1;
                pipes.p2 = pipes.p1;
                pipes.q2 = pipes.q1;
                pipes.beta2 = pipes.beta1;
                pipes.refDiam2 = pipes.refDiam1;

                indice2 = (i * NbDiam) + j;
                diam = (Diameters) dvector.elementAt(j);
                d2 = diam.diam;
                pipes.d1 = d2;
                pipes.l1 = x[indice2];
                pipes.p1 = diam.p;
                pipes.q1 = diam.q;
                pipes.beta1 = diam.beta;

                /////LOLOW ADDITION////////////////////////////////////////
                pipes.refDiam1 = "" + j; //$NON-NLS-1$

                ///////////////////////////////////////////////////////////
            }

            // Si il existe deux longueurs
            if (indice2 != -1) {
                // calcul des longueurs supplementaires
                l1 = GetLength(pipes.l1, lcom);
                l2 = GetLength(pipes.l2, lcom);

                // si la somme des longueurs depasse lcom
                if ((l1 + l2) > lcom) {
                    l2 = lcom - l1;
                    l1 = lcom - l2;
                }

                if (pipes.imposdiam1 == 0) {
                    //calcul des pertes d'energies dus a un transfere de longueur/
                    E1 = CalculEnergy(l1, l2, pipes.d1, pipes.d2, pipes.q1,
                            pipes.q2);
                    E2 = CalculEnergy(l2, l1, pipes.d2, pipes.d1, pipes.q2,
                            pipes.q1);

                    if (E1 < E2) {
                        pipes.l1 = pipes.l1 + l2;
                        pipes.l2 = pipes.l2 - l2;
                        x[indice1] = pipes.l2;
                        x[indice2] = pipes.l1;
                    } else {
                        pipes.l1 = pipes.l1 - l1;
                        pipes.l2 = pipes.l2 + l1;
                        x[indice1] = pipes.l2;
                        x[indice2] = pipes.l1;
                    }
                }
            }
        }
    }

    public double GetLength(double x, double lcom) {
        double l = 0;

        if (Math.round(x / lcom) < (x / lcom)) {
            l = ((x / lcom) - Math.round(x / lcom)) * lcom;
        } else {
            l = ((x / lcom) - Math.round(x / lcom) + 1) * lcom;
        }

        return l;
    }

    public double CalculEnergy(double l1, double l2, double d1, double d2,
        double q1, double q2) {
        double E = Math.abs(((l1 / Math.pow(d1, q1)) + (l2 / Math.pow(d2, q2))) -
                ((l1 + l2) / Math.pow(d1, q1)));

        return E;
    }
}
