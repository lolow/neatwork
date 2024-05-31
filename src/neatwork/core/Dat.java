package neatwork.core;

public class Dat {

    public int nB;
    public int nT;
    public double[] length;
    public double[] h;
    public double invest;
    public double cible;
    public double lbd;
    public int[][] S;
    public double[] nbouvert;
    public double[] alpha;
    public double[] beta;
    public int maxiter; // nombre maximal d'itérations de Newton. Si ce nombre est atteint on considère que la méthode n'a pas convergé.
    public double tolr;
    public double tolx;

    public Dat() {
    }

    public Dat(int nB, int nT, double[] length, double[] h, double invest, double cible, double lbd,
               int[][] S, double[] nbouvert, double[] alpha, double[] beta, int maxiter, double tolr, double tolx) {
        this.nB = nB;
        this.nT = nT;
        this.length = length;
        this.h = h;
        this.invest = invest;
        this.cible = cible;
        this.lbd = lbd;
        this.S = S;
        this.nbouvert = nbouvert;
        this.alpha = alpha;
        this.beta = beta;
        this.maxiter = maxiter;
        this.tolr = tolr;
        this.tolx = tolx;
    }

    public Dat(Dat original) {
        this.nB = original.nB;
        this.nT = original.nT;
        this.length = original.length;
        this.h = original.h;
        this.invest = original.invest;
        this.cible = original.cible;
        this.lbd = original.lbd;
        this.S = original.S;
        this.nbouvert = original.nbouvert;
        this.alpha = original.alpha;
        this.beta = original.beta;
        this.maxiter = original.maxiter;
        this.tolr = original.tolr;
        this.tolx = original.tolx;
    }
}

