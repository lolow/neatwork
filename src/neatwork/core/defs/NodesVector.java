package neatwork.core.defs;

import java.util.*;


/** definition d'un vecteur de noeud*/
public class NodesVector extends Vector {
    public void addNodes(Nodes n) {
        addElement(n);
    }

    public void addNodes(String ID, double heigth, int pseudoTaps) {
        if (!existNodes(ID)) {
            Nodes nodes = new Nodes(ID, heigth, pseudoTaps);
            addNodes(nodes);
        }
    }

    /** Cette procedure verifie si un nodes a deja ete cree pour un noeud donne */
    public boolean existNodes(String n) {
        boolean bool = false;
        Nodes nodes;

        for (int i = 0; i < size(); i++) {
            nodes = (Nodes) elementAt(i);

            if (nodes.nodes.equalsIgnoreCase(n)) {
                bool = true;
            }
        }

        return bool;
    }

    /** renvoie le noeud ID*/
    public Nodes getNodes(String ID) {
        Nodes nodes = (Nodes) elementAt(0);
        int i = 1;

        while (!nodes.nodes.equalsIgnoreCase(ID)) {
            nodes = (Nodes) elementAt(i);
            i++;
        }

        return nodes;
    }

    /** Initialise les tableaux permettant de sauvegarder les simulations */
    public void initializeSimulation(int nbSim) {
        Nodes nodes;

        for (int i = 0; i < size(); i++) {
            nodes = (Nodes) elementAt(i);
            nodes.pressim = new double[nbSim];
        }
    }

    /** Renvoie le nombre de robinets d'un noeud */
    public int getNbTaps(String ID) {
        int i = 0;
        Nodes nodes = (Nodes) elementAt(0);

        while (!nodes.nodes.equalsIgnoreCase(ID)) {
            i++;
            nodes = (Nodes) elementAt(i);
        }

        return nodes.nbTaps;
    }

    /** Renvoie la hauteur d'un noeud */
    public double getHeight(String n) {
        int i = 0;
        Nodes nodes = (Nodes) elementAt(0);

        while (!nodes.nodes.equalsIgnoreCase(n)) {
            i++;
            nodes = (Nodes) elementAt(i);
        }

        return nodes.height;
    }

    /** Renvoie la position d'un noeud dans le vecteur */
    public int getPosition(String n) {
        int i = 0;
        Nodes nodes = (Nodes) elementAt(0);

        while (!nodes.nodes.equalsIgnoreCase(n)) {
            i++;
            nodes = (Nodes) elementAt(i);
        }

        return i;
    }

    /* this function return the nodes which corresponds with
      the number of open taps for a multitaps */
    public Nodes GetNodes(int indexgrouptaps, int numberofopentaps) {
        Nodes nodes = (Nodes) elementAt(0);
        int index = 0;

        while (nodes.indexgrouptaps != indexgrouptaps) {
            index++;
            nodes = (Nodes) elementAt(index);
        }

        nodes = (Nodes) elementAt((index + numberofopentaps) - 1);

        return nodes;
    }

    /* return the number of open taps for a multitaps */
    public int GetNumberOfOpenGroupTaps(int indexgrouptaps) {
        int number = 0;
        Nodes nodes;

        for (int i = 0; i < size(); i++) {
            nodes = (Nodes) elementAt(i);

            if (nodes.indexgrouptaps == indexgrouptaps) {
                number++;
            }
        }

        return number;
    }
}
