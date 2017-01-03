package neatwork.core.defs;

import java.util.*;


/** d\u00E9finition of the vector of pipes*/
public class PipesVector extends Vector {
    public PipesVector() {
    }

    public void addPipes(Pipes p) {
        addElement(p);
    }

    public void UpDatePipes() {
        int i;
        Pipes pipes;

        for (i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);
            pipes.moyenne = 0;
            pipes.moyennec = 0;
            pipes.max = 0;
            pipes.min = 0;
            pipes.nbsim = 0;
            pipes.seuil = 0;
            pipes.speed = 0;
            pipes.speedmax = 0;
            pipes.speedmin = 0;
        }
    }

    public void addPipes(String n_beg, String n_end, double l) {
        Pipes pipes = new Pipes(n_beg, n_end, n_beg, l);
        addPipes(pipes);
    }

    /* renvoie le noeud precedent*/
    public String GetPred(String n) {
        int i = 0;
        int bool = 0;
        String ret = null;

        while ((i != size()) && (bool == 0)) {
            Pipes pipes = (Pipes) elementAt(i);

            if (pipes.nodes_end.equalsIgnoreCase(n)) {
                bool = 1;
                ret = pipes.nodes_beg;
            }

            i++;
        }

        return ret;
    }

    public void initializeSimulation(int NbSim) {
        Pipes pipes;

        for (int i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);
            pipes.simulation = new double[NbSim];
        }

        UpDatePipes();
    }

    public void getPath(String n, Path path, NodesVector nvector, int taps) {
        int i;

        while (n != null) {
            path.addNodes(n);

            /*a chaque fois que l'on rencontre un noeud on incremente le nb*/
            /*de robinet a sa base*/
            if (taps == 1) {
                i = 0;

                Nodes nodes = (Nodes) nvector.elementAt(i);

                while (!nodes.nodes.equalsIgnoreCase(n)) {
                    i++;
                    nodes = (Nodes) nvector.elementAt(i);
                }

                nodes.nbTaps++;
            }

            n = GetPred(n);
        }
    }

    public int getPosition(String n_end) {
        int i = 0;
        Pipes pipes = (Pipes) elementAt(0);

        while (!pipes.nodes_end.equalsIgnoreCase(n_end)) {
            i++;
            pipes = (Pipes) elementAt(i);
        }

        return i;
    }

    public int GetPosition(String n_beg, String n_end) {
        int i = 0;
        Pipes pipes = (Pipes) elementAt(0);

        while ((!pipes.nodes_end.equalsIgnoreCase(n_end)) ||
                (!pipes.nodes_beg.equalsIgnoreCase(n_beg))) {
            i++;
            pipes = (Pipes) elementAt(i);
        }

        return i;
    }

    /* return the number of open taps for a multitaps */
    public int GetNumberOfOpenGroupTaps(int indexgrouptaps) {
        int number = 0;
        Pipes pipes;

        for (int i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);

            if (pipes.indexgrouptaps == indexgrouptaps) {
                number++;
            }
        }

        return number;
    }

    /* this function return the pipes which corresponds with
      the number of open taps for a multitaps */
    public Pipes GetPipes(int indexgrouptaps, int numberofopentaps) {
        Pipes pipes = (Pipes) elementAt(0);
        int index = 0;

        while (pipes.indexgrouptaps != indexgrouptaps) {
            index++;
            pipes = (Pipes) elementAt(index);
        }

        pipes = (Pipes) elementAt((index + numberofopentaps) - 1);

        return pipes;
    }

    /* Cette procedure recherche tous les pred et les suiv
    d'un noeud donne */
    public void GetSuivPred(Nodes nodes) {
        Pipes pipes;
        nodes.suiv = new Vector();
        nodes.pred = new Vector();

        for (int i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);

            if (nodes.nodes.equalsIgnoreCase(pipes.nodes_beg)) {
                nodes.suiv.addElement(pipes.nodes_end);
            }

            if (nodes.nodes.equalsIgnoreCase(pipes.nodes_end)) {
                nodes.pred.addElement(pipes.nodes_beg);
            }
        }
    }

    /* Renvoie le nombre de predecesseurs d'un noeud donne
      Cette fonction sert e definir si le rseau est un arbre
      ou s'il contient des boucles.
      si le nombre de predecesseurs est > 1 alors il y a des boucles */
    public int GetNumberOfPred(Nodes nodes) {
        int numberofpred = 0;
        Pipes pipes;

        for (int i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);

            if (nodes.nodes.equalsIgnoreCase(pipes.nodes_end)) {
                numberofpred++;
            }
        }

        return numberofpred;
    }

    /* calcule la vitesse de l'eau qui circule dans
    les tuyaux */
    public void CalculSpeed() {
        int i;
        Pipes pipes;

        for (i = 0; i < size(); i++) {
            pipes = (Pipes) elementAt(i);
            pipes.speed = (pipes.moyenne / 1000 * 4) / 3.14 / Math.pow(pipes.d1,
                    2);
            pipes.speedmin = (pipes.min / 1000 * 4) / 3.14 / Math.pow(pipes.d1,
                    2);
            pipes.speedmax = (pipes.max / 1000 * 4) / 3.14 / Math.pow(pipes.d1,
                    2);
        }
    }
}
