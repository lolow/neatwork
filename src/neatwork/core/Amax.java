package neatwork.core;

import java.util.Arrays;

public class Amax {
    
    public static class ResultAmax {
        public double a;
        public double[] x;
        public int i;

        public ResultAmax(double a, double[] x, int i) {
            this.a = a;
            this.x = x;
            this.i = i;
        }
    }

    public static ResultAmax amax(double[] x, double[] y, double[] dx, double[] gradf, double[][] Hred, Dat dat) {
        int itermax = 30;
        double k = 0.8; // réduction du pas
        double gain = 0.5; // gain demandé par rapport à l'approximation quadratique

        int i = 0;
        int convergence = 0;
        double a = 1;

        y = new double[dat.S.length];
        for (int j = 0; j < dat.S.length; j++) {
            for (int l = 0; l < dat.S[j].length; l++) {
                y[j] += dat.S[j][l] * x[l];
            }
        }

        double[] yx = new double[y.length + x.length];
        System.arraycopy(y, 0, yx, 0, y.length);
        System.arraycopy(x, 0, yx, y.length, x.length);

        double Finit = 0;
        for (int j = 0; j < yx.length; j++) {
            Finit += dat.beta[j] * Math.pow(yx[j], dat.lbd + 1);
        }
        Finit /= (dat.lbd + 1);

        double sum = 0;
        for (int j = 0; j < x.length; j++) {
            sum += dat.alpha[j] * Math.pow(x[j], 3);
        }

        Finit += sum / 3;

        double[] hSubset = Arrays.copyOfRange(dat.h, dat.nB, dat.h.length);

        double result = 0;
        for (int j = 0; j < hSubset.length; j++) {
            result += hSubset[j] * x[j];
        }

        Finit += result;

        while (convergence == 0 && i < itermax) {
            i++;
            double[] xa = new double[x.length];
            for (int j = 0; j < x.length; j++) {
                xa[j] = Math.max(0, x[j] + a * dx[j]);
            }

            double[] ya = new double[dat.S.length];
            for (int j = 0; j < dat.S.length; j++) {
                for (int l = 0; l < dat.S[j].length; l++) {
                    ya[j] += dat.S[j][l] * xa[l];
                }
            }

            double DeltaQxa = 0.0;

            for (int j = 0; j < gradf.length; j++) {
                DeltaQxa += gradf[j] * (xa[j] - x[j]);
            }

            for (int l = 0; l < Hred.length; l++) {
                for (int j = 0; j < Hred[l].length; j++) {
                    DeltaQxa += 0.5 * Hred[l][j] * (xa[l] - x[l]) * (xa[j] - x[j]);
                }
            }

            double Fxa = 0.0;

            for (int j = 0; j < ya.length; j++) {
                Fxa += dat.beta[j] * Math.pow(ya[j], dat.lbd + 1);
            }
            for (int j = 0; j < xa.length; j++) {
                Fxa += dat.beta[j + ya.length] * Math.pow(xa[j], dat.lbd + 1);
            }
            Fxa /= (dat.lbd + 1);

            for (int j = 0; j < xa.length; j++) {
                Fxa += dat.alpha[j] * Math.pow(xa[j], 3) / 3;
            }

            for (int j = 0; j < xa.length; j++) {
                Fxa += dat.h[dat.nB + j] * xa[j];
            }

            if (Finit - Fxa > -gain * DeltaQxa) {
                convergence = 1;
            } else {
                a = k * a;
            }
        }

        for (int j = 0; j < x.length; j++) {
            x[j] = Math.max(0, x[j] + a * dx[j]);
        }

        return new ResultAmax(a, x, i);
    }
}
