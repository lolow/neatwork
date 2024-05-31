package neatwork.core;

public class Gradient {

    // Calcul des dérivées premières de l'objectif
    public static double[] gradient(double[] x, double[] y, Dat dat) {

        int nB = dat.S.length;
        int nT = dat.S[0].length;
        int n = nB + nT;

        // Friction sur les arcs intermédiaires en fonction des débits y
        double[] dF_y = new double[nB];
        for (int i = 0; i < nB; i++) {
            dF_y[i] = dat.beta[i] * Math.pow(y[i], dat.lbd);
        }

        // Même friction exprimée en fonction des débits terminaux x (y=S*x)
        double[] dF_x = new double[nT];
        for (int i = 0; i < nT; i++) {
            for (int j = 0; j < nB; j++) {
                dF_x[i] += dat.S[j][i] * dF_y[j];
            }
        }

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

        return gradf;
    }
}
