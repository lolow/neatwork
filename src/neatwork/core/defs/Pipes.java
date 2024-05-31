package neatwork.core.defs;


/** Definition of a pipe*/
public class Pipes {
    public String nodes_beg;
    public String nodes_end;
    public String nodes_des;
    public double length;
    public int indexgrouptaps = 0;
    public double d1 = 0;
    public double d2 = 1;
    public double l1 = 0;
    public double l2 = 0;
    public double p1 = 1;
    public double p2 = 1;
    public double q1 = 1;
    public double q2 = 1;
    public double beta1 = 1;
    public double beta2 = 1;
    public double imposdiammin = 0;
    public double imposdiammax = 10000;
    public double imposdiam1 = 0;
    public double imposdiam2 = 0;
    public double imposlength1 = 0;
    public double moyenne = 0;
    public double moyennec = 0;
    public double min = 0;
    public double max = 0;
    public double seuil = 0;
    public double seuil2 = 0;
    public int nbsim = 0;
    public int failure = 0;

    //  public double suction = 0;
    public double speed = 0;
    public double speedmin = 0;
    public double speedmax = 0;
    public double[] simulation;
    public double quart10 = 0;
    public double quart25 = 0;
    public double quart50 = 0;
    public double quart75 = 0;
    public double quart90 = 0;

    /////LOLOW ADDITION
    public int quarteff10 = 0;
    public int quarteff25 = 0;
    public int quarteff50 = 0;
    public int quarteff75 = 0;
    public int quarteff90 = 0;
    public int quarteff100 = 0;

    /////LOLOW ADDITION
    public String refDiam1 = "0"; 
    public String refDiam2 = "0"; 

    ///////////////////
    public Pipes(String n_beg, String n_end, String n_des, double l) {
        nodes_beg = n_beg;
        nodes_end = n_end;
        nodes_des = n_des;
        length = l;
    }

    public String toString() {
        String dir = (nodes_beg.equalsIgnoreCase(nodes_des))
            ? (nodes_beg + "->" + nodes_end) : (nodes_end + "->" + nodes_beg);  

        return "[" + dir + ",l=" + length + ", l1=" + l1 + ", l2=" + l2 +   //$NON-NLS-3$ //$NON-NLS-4$
        ", d1=" + d1 + ", p1=" + p1 + ", beta1=" + beta1 + ", q1=" + q1 + "]";   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
}
