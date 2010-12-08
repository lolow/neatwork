package neatwork.core.defs;

import neatwork.Messages;

import neatwork.utils.*;

import java.util.*;


/**
 * Definition d'un design, ancienne version
 * @author L. DROUET
 * @version 1.0
 */
public class CoreDesign {
    public PipesVector pvector = new PipesVector();
    public NodesVector nvector = new NodesVector();
    public TapsVector tvector = new TapsVector();
    private OrificesVector ovector;
    private DiametersVector dvector;

    /** construit un design \u00E0 l'aide d'un vecteur.
     * <p>
     * Le format du vecteur est:
     * node id, pred node, height, length, # taps, length pipe1,
     * length pipe2, diam pipe1, diam pipe2, orifice ideal, com orifice.<br>
     * La source doit se trouv\u00E9 sur la premi\u00E8re ligne.
     */
    public CoreDesign(Vector data, DiametersVector dvector,
        OrificesVector ovector) {
        String prefixgrouptaps = " "; //$NON-NLS-1$
        int indexgrouptaps = 1;

        this.dvector = dvector;
        this.ovector = ovector;

        //lecture de la source
        nvector.addNodes(((Vector) data.get(0)).get(0).toString(), 0, 0);
        data.remove(0);

        int index = 0;

        for (Enumeration e = data.elements(); e.hasMoreElements();) {
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

            double lp1 = 0;

            try {
                lp1 = Double.parseDouble(v.get(5).toString());
            } catch (NumberFormatException ex) {
            }

            double lp2 = 0;

            try {
                lp2 = Double.parseDouble(v.get(6).toString());
            } catch (NumberFormatException ex) {
            }

            double dp1 = 0;

            try {
                dp1 = Double.parseDouble(v.get(7).toString());
            } catch (NumberFormatException ex) {
            }

            double dp2 = 0;

            try {
                dp2 = Double.parseDouble(v.get(8).toString());
            } catch (NumberFormatException ex) {
            }

            double ori = 0;

            try {
                ori = Double.parseDouble(v.get(9).toString());
            } catch (NumberFormatException ex) {
            }

            double ori2 = 0;

            try {
                ori2 = Double.parseDouble(v.get(10).toString());
            } catch (NumberFormatException ex) {
            }

            //validit\u00E9 des valeurs
            /*if (tap < 0)
              throw new RuntimeException("fichier design non valide (number of taps <0)");
            if (tap > 1)
              throw new RuntimeException("fichier design non valide (number of taps >1)");
            */
            nvector.addNodes(nod, height, tap);
            pvector.addPipes(pred, nod, length);

            Pipes pipes = (Pipes) pvector.lastElement();
            pipes.l1 = lp1;
            pipes.l2 = lp2;
            pipes.d1 = dp1;
            pipes.d2 = dp2;

            Diameters diam1 = dvector.getDiameters(dp1);
            pipes.p1 = diam1.p;
            pipes.q1 = diam1.q;
            pipes.beta1 = diam1.beta;

            if (lp2 != 0) {
                Diameters diam2 = dvector.getDiameters(dp2);
                pipes.p2 = diam2.p;
                pipes.q2 = diam2.q;
                pipes.beta2 = diam2.beta;
            }

            /* Si le noeud est un robinet */
            if (tap == 1) {
                Nodes nodes;

                // On introduit un index de groupe de robinet si necessaire */
                StringTokenizer T = new StringTokenizer(pipes.nodes_end, "_"); //$NON-NLS-1$
                String prefix;

                if (T.countTokens() > 1) {
                    prefix = T.nextToken();
                    nodes = (Nodes) nvector.lastElement();

                    if (!prefixgrouptaps.equals(prefix)) {
                        indexgrouptaps++;
                    }

                    pipes.indexgrouptaps = indexgrouptaps;
                    nodes.indexgrouptaps = indexgrouptaps;
                    prefixgrouptaps = prefix;
                }

                int i = 0;

                /* Recherche du noeud */
                do {
                    i++;
                    nodes = (Nodes) nvector.elementAt(i);
                } while (!(nodes.nodes.equalsIgnoreCase(nod)));

                nodes.taps = 1;

                /* creation du taps */
                tvector.addTaps(nod);

                /* on rentre l'orifice */
                Taps taps = (Taps) tvector.lastElement();
                taps.orif_ideal = ori;
                taps.orif_com = ori2;
                taps.orifice = ori;
            }
        }

        //attribOrificeCom();
        attribSummary();
    }

    /** renvoie le cout du design (d\u00E9pend du jeux de diam\u00E8tres)*/
    public double getCost() {
        double cost = 0;

        for (int i = 0; i < pvector.size(); i++) {
            Pipes pipes = (Pipes) pvector.elementAt(i);
            double c1 = (pipes.l1 != 0) ? dvector.getCost(pipes.d1) : 0;
            double c2 = (pipes.l2 != 0) ? dvector.getCost(pipes.d2) : 0;
            cost += ((c1 * pipes.l1) + (c2 * pipes.l2));
        }

        return cost;
    }

    /** renvoie le resume des besoins en tuyaux (depend du jeux de diametres)*/
    public Vector getSummaryData() {
        Vector v = new Vector();

        //diametres
        for (int i = 0; i < dvector.size(); i++) {
            Diameters diam = (Diameters) dvector.get(i);

            if (diam.summary > 0.1) {
                Vector line = new Vector();
                line.add(diam.nominal);
                line.add(Tools.doubleFormat("0.##", diam.SDR)); //$NON-NLS-1$
                line.add(Tools.doubleFormat("0.####", diam.diam)); //$NON-NLS-1$
                line.add(Tools.doubleFormat("0.#", diam.summary)); //$NON-NLS-1$
                line.add(Tools.doubleFormat("0.#", diam.cost)); //$NON-NLS-1$
                line.add(Tools.doubleFormat("0.#", diam.cost * diam.summary)); //$NON-NLS-1$
                v.add(line);
            }
        }

        return v;
    }

    /** renvoie les enonces du resume des besoins en tuyaux */
    public static Vector getSummaryHeader() {
        Vector v = new Vector();
        v.add(Messages.getString("CoreDesign.Nominal")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.SDR")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Diameter")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Total_length")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Unit_Cost")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Total_cost")); //$NON-NLS-1$

        return v;
    }

    /** calcul le resume */
    private void attribSummary() {
        //mise \u00E0 zero des summary des diametres
        for (Enumeration e = dvector.elements(); e.hasMoreElements();)
            ((Diameters) e.nextElement()).summary = 0;

        //pour chaque tuyaux on ajoute la longueur au diam\u00E8tre correspondant
        for (Enumeration e = pvector.elements(); e.hasMoreElements();) {
            Pipes pipes = (Pipes) e.nextElement();
            dvector.getDiameters(pipes.d1).summary += pipes.l1;

            if (pipes.l2 != 0) {
                dvector.getDiameters(pipes.d2).summary += pipes.l2;
            }
        }
    }

    /** renvoie le design */
    public Vector getData() {
        Vector v = new Vector();

        //ajout de la source
        Vector source = new Vector();
        source.add(((Nodes) nvector.get(0)).nodes);
        source.add(((Nodes) nvector.get(0)).nodes);

        for (int i = 0; i < 9; i++)
            source.add("0"); //$NON-NLS-1$

        v.add(source);

        //tuyaux restant
        for (int i = 0; i < pvector.size(); i++) {
            Pipes pipes = (Pipes) pvector.get(i);
            Nodes nodes = (Nodes) nvector.get(i + 1);
            Vector line = new Vector();
            line.add(pipes.nodes_end);
            line.add(pipes.nodes_beg);
            line.add(String.valueOf(nodes.height));
            line.add(String.valueOf(pipes.length));
            line.add(String.valueOf(nodes.taps));
            line.add(String.valueOf(Math.round(pipes.l1)));
            line.add(String.valueOf(Math.round(pipes.l2)));
            line.add(Tools.doubleFormat("0.####", pipes.d1)); //$NON-NLS-1$
            line.add(Tools.doubleFormat("0.####", pipes.d2)); //$NON-NLS-1$

            if (tvector.isTaps(nodes.nodes)) {
                Taps taps = (Taps) tvector.get(i - pvector.size() +
                        tvector.size());
                line.add(Tools.doubleFormat("0.####", taps.orif_ideal)); //$NON-NLS-1$
                line.add(Tools.doubleFormat("0.####", taps.orif_com)); //$NON-NLS-1$
            } else {
                line.add("0"); //$NON-NLS-1$
                line.add("0"); //$NON-NLS-1$
            }

            v.add(line);
        }

        return v;
    }

    /** renvoie le titre des colonnes*/
    public static Vector getHeader() {
        Vector v = new Vector();
        v.add(Messages.getString("CoreDesign.Node_ID")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Pred._node")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Height")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.Length")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign._#_of_taps")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.length_pipe1")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.length_pipe2")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.diam_pipe1")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.diam_pipe2")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.ideal_orifice")); //$NON-NLS-1$
        v.add(Messages.getString("CoreDesign.comercial_orifice")); //$NON-NLS-1$

        return v;
    }
}
