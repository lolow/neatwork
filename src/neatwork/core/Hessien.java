package neatwork.core;

public class Hessien {

    /* Calcule les dérivées secondes de l'objectif par rapport à x
    Input : x = débit aux robinets ; y = débit dans les intermédiaires
    dat = structure contenant les dat.paramètres.
    Output : H = matrice des dérivées secondes */
    public static double[][] hessien(double[] x, double[] y, Dat dat) {

        int nB = dat.nB;
        int nT = dat.nT;

        double[][] H = new double[nT][nT];

        // Contribution des arcs intermédiaires en fonction de y
        double[] d2F_y = new double[nB];
        for (int i = 0; i < nB; i++) {
            d2F_y[i] = dat.lbd * dat.beta[i] * Math.pow(y[i], dat.lbd - 1);
        }

        // Contribution des arcs intermédiaires en fonction de x
        // La matrice d2F_x est positive semi-définie
        int[][] sTranspose = Utils.transposeMatrix(dat.S);
        double[] d2F_yArray = new double[nB];
        for (int i = 0; i < nB; i++) {
            d2F_yArray[i] = dat.lbd * dat.beta[i] * Math.pow(y[i], dat.lbd - 1);
        }
        double[][] d2F_yMatrix = Utils.createDiagonalMatrix(d2F_yArray);

        double[][] temp1 = Utils.multiplyMatrices(Utils.convertIntToDoubleMatrix(sTranspose), d2F_yMatrix);
        double[][] temp2 = Utils.multiplyMatrices(temp1, Utils.convertIntToDoubleMatrix(dat.S));

        double[][] identityMatrix = Utils.createIdentityMatrix(x.length);
        double[][] d2F_x = Utils.addMatrices(temp2, Utils.scalarMultiplyMatrix(identityMatrix, 1e-6));

        // Ajout des effets (diagonaux) sur les arcs terminaux
        double[] dDiag = new double[nT];
        for (int i = 0; i < nT; i++) {
            dDiag[i] = dat.lbd * dat.beta[nB + i] * Math.pow(dat.nbouvert[i] * x[i], dat.lbd - 1);
        }

        // Addition des contributions(diagonales) des orifices et robinets
        for (int i = 0; i < nT; i++) {
            dDiag[i] = dDiag[i] + 2 * dat.alpha[i] * x[i];
        }

        // dDiag peut avoir des composantes nulles. Ajout d'un terme de sécurité.
        for (int i = 0; i < nT; i++) {
            dDiag[i] = dDiag[i] + 1e-6;
        }

        // Hessien : dérivées secondes par rapport à x
        for (int i = 0; i < nT; i++) {
            for (int j = 0; j < nT; j++) {
                H[i][j] = d2F_x[i][j] + (i == j ? dDiag[i] : 0);
            }
        }

        return H;
    }
}
