package neatwork.core;

import java.util.List;

public class Ndir {
        
    public static class ResultNdir {
        public double[] dx_red;
        public double[][] Hred;

        public ResultNdir(double[] dx_red, double[][] Hred) {
            this.dx_red = dx_red;
            this.Hred = Hred;
        }
    }

    /* Calcul du Hessien et de la direction dans l'espace réduit I
    Input
    Hessien H dans l'espace plein, résidu rp
    Ensemble inactif I ; n estlength(rp) et (nxn) = size(H)
    Output
    H_red Hessien réduit. nT x nT composée de deux blocs
    diagonaux H(J,J) matrice pleine dans l'espace actif
    J = setdiff(1:nt,I)
    et une matrice diagonale definie positive dans l'espace inactif I. */
    public static ResultNdir ndir(double[][] H, double[] gradf, List<Integer> I) {

        int n = H.length;

        double[][] Hred = new double[n][n];
        double[] rp = new double[n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(H[i], 0, Hred[i], 0, n);
            rp[i] = -gradf[i];
        }

        if (I.size() > 0) {
            for (int i : I) {
                rp[i] = 0;
            }

            // Sécurisation de la factorisation de H dans l'espace inactif
            double[] d = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = Hred[i][i];
                if (I.contains(i)) {
                    d[i] = 1e8;
                }
            }
            
            for (int i = 0; i < n; i++) {
                Hred[i][i] -= d[i];
            }

            for (int i : I) {
                for (int j = 0; j < n; j++) {
                    Hred[i][j] = 0;
                    Hred[j][i] = 0;
                }
            }

            for (int i = 0; i < n; i++) {
                Hred[i][i] += d[i];
            }
        }

        double[][] R = Utils.chol(Hred);

        double[] dx_red = Utils.solveLinearSystem(R, Utils.transposeMatrix(R), rp);

        for (int i : I) {
            dx_red[i] = 0;
        }

        return new ResultNdir(dx_red, Hred);
    }
}
