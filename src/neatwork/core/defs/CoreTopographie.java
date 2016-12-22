package neatwork.core.defs;

import java.util.*;


/**
 * Definition d'une topographie, ancienne version
 * 
 */
public class CoreTopographie {
    public PipesVector pvector = new PipesVector();
    public NodesVector nvector = new NodesVector();
    public TapsVector tvector = new TapsVector();
    private Vector data;

    /** construit une topographie \u00E0 l'aide d'un vecteur.
     * <p>
     * Le format du vecteur est:
     * node id, pred node, height, length, # taps.<br>
     * La source doit se trouvee sur la premiere ligne.
     */
    public CoreTopographie(Vector data) {
        this.data = data;

        //lecture de la source
        nvector.addNodes(((Vector) data.get(0)).get(0).toString(), 0, 0);

        Vector data0 = new Vector(data);
        data0.remove(0);

        int index = 0;

        for (Enumeration e = data0.elements(); e.hasMoreElements();) {
            Vector v = (Vector) e.nextElement();

            //lecture des donnees
            String nod = v.get(0).toString();
            String pred = v.get(1).toString();
            double height = 0;

            try {
                height = Double.parseDouble(v.get(2).toString());
            } catch (NumberFormatException ex) {
            }

            double length = 0;

            try {
                length = Double.parseDouble(v.get(3).toString());
            } catch (NumberFormatException ex) {
            }

            int tap = 0;

            try {
                tap = Integer.parseInt(v.get(4).toString());
            } catch (NumberFormatException ex) {
            }

            //validit\u00E9 des valeurs
            if (tap < 0) {
                throw new RuntimeException(
                    "fichier topo non valide (number of taps <0)"); 
            }

            //attribution des valeurs
            if (tap == 0) {
                nvector.addNodes(nod, height, tap);
                pvector.addPipes(pred, nod, length);
                index++;
            }

            if (tap == 1) {
                nvector.addNodes(nod, height, tap);
                pvector.addPipes(pred, nod, length);
            }

            if (tap > 1) {
                Nodes nodes = new Nodes(nod, height, tap);
                nvector.add(index + 1, nodes);

                Pipes pipes = new Pipes(pred, nod, pred, length);
                pvector.add(index, pipes);
                index++;
            }

            if (tap != 0) {
                if (tap == 1) {
                    int i = 0;
                    Nodes nodes;

                    do {
                        i++;
                        nodes = (Nodes) nvector.elementAt(i);
                    } while (!(nodes.nodes.equalsIgnoreCase(nod)));

                    nodes.taps = 1;
                    tvector.addTaps(nod);
                } else {
                    for (int i = 0; i < tap; i++) {
                        String taps;
                        taps = nod + " - " + i; 
                        nvector.addNodes(taps, height, 1);
                        pvector.addPipes(nod, taps, 1);

                        int j = 0;
                        Nodes nodes;

                        do {
                            j++;
                            nodes = (Nodes) nvector.elementAt(j);
                        } while (!(nodes.nodes.equalsIgnoreCase(taps)));

                        nodes.taps = 1;
                        tvector.addTaps(taps);
                    }
                }
            }
        }
    }

    public Vector getData() {
        return data;
    }
}
