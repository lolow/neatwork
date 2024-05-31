package neatwork.core.defs;

import java.util.*;


/** 
 * definition of a node, ancienne version
 */
public class Nodes {
    //DESCRIPTION
    public String nodes;
    public double height;
    public Path path; /*chemin du noeud a la source*/
    public int nbTaps = 0; /*nb de robinet a sa base*/
    public int taps; /*permet de savoir s'il s'agit d'un robinet ou non*/
    public int pseudoTaps; /* quand il y a plusieurs riobinets a une extremite*/
    public Vector suiv;
    public Vector pred;

    //DESIGN
    public double pressure;

    //SIMULATION
    public int ajout = 0; // =1 si noeud ajoute pour la simulation
    public double suction;
    public double[] pressim;
    public double averpress;
    public double minpress;
    public double maxpress;
    public int indexgrouptaps = 0;

    public Nodes(String ID, double height, int pseudoTaps) {
        nodes = ID;
        this.height = height;
        this.pseudoTaps = pseudoTaps;
        path = new Path(ID);
    }

    public String toString() {
        return "[" + nodes + ",h=" + height + ",#t=" + pseudoTaps + "]";   //$NON-NLS-3$ //$NON-NLS-4$
    }
}
