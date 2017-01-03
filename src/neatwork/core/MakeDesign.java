package neatwork.core;

import neatwork.*;

import neatwork.core.defs.*;

import neatwork.core.run.*;

import neatwork.utils.*;

import java.util.*;


/**
 * Classe qui execute l'operation make design.
 */
public class MakeDesign {
    private double[] x;
    private CoreTopographie topo;
    private DiametersVector dvector;
    private Vector diametresContent;
    private Vector orificeContent;
    private Hashtable loadFactor;

    public MakeDesign(CoreTopographie topo, DiametersVector dvector,
        OrificesVector ovector, Properties prop, Hashtable LoadFactor) {
        this.topo = topo;
        this.dvector = dvector;
        this.loadFactor = LoadFactor;

        // Properties and default value
        double outflow = Double.parseDouble(prop.getProperty(
				"topo.targetflow.value", "0.2")) / 1000;
        double qualite = Double.parseDouble(prop.getProperty(
                    "topo.servicequal.value", "0.8"));
        double rated = Double.parseDouble(prop.getProperty(
                    "topo.opentaps.value", "0.4"));
        double length_com = Double.parseDouble(prop.getProperty(
                    "topo.pipelength.value", "6.0"));
        double alpha = Double.parseDouble(prop.getProperty(
                    "topo.faucetcoef.value", "0.00000002"));
        double prixMax = Double.parseDouble(prop.getProperty(
                    "topo.limitbudget.value", "1000000000"));
        double coeffOrifice = Double.parseDouble(prop.getProperty(
                    "topo.orifcoef.value", "0.59"));

        // Compute out-flow distributions
        double[] proba = calculProba(outflow, topo.tvector.size(), rated,
                qualite);

        //Calcul des chemins de chaque noeud jusque la source
        getAllPath();

        //Calcul des facteurs de charge sur les tuyaux
        double[] loadFactor1 = new double[topo.pvector.size()];

        for (int i = 0; i < topo.pvector.size(); i++) {
            Pipes pipes = (Pipes) topo.pvector.elementAt(i);
            loadFactor1[i] = Double.parseDouble(LoadFactor.get(pipes.nodes_end)
                                                         .toString()) * outflow;
        }

        // Design parameters
        int n = ((dvector.size() * topo.pvector.size()) + topo.nvector.size()) -
            1;
        int m = topo.nvector.size() + topo.pvector.size();
        x = new double[n];

        //affectation des alpha
        for (int i = 0; i < topo.tvector.size(); i++) {
            ((Taps) topo.tvector.get(i)).faucetCoef = alpha;
        }

        RunMakeDesign design = new RunMakeDesign(x, topo.nvector, topo.pvector,
                topo.tvector, dvector, ovector, loadFactor1, outflow,
                length_com, n, m, prixMax, coeffOrifice);

        // Diameter reference
        int cpt = 0;
        Hashtable dejaFait = new Hashtable();
        HashSet dejaFait2 = new HashSet(); //a refaire mas pas le temps
        Enumeration enun = topo.pvector.elements();

        while (enun.hasMoreElements()) {
            Pipes p = (Pipes) enun.nextElement();

            //pipe1
            Integer index = new Integer(p.refDiam1);

            if (!dejaFait.containsKey(index)) {
                Vector line = diametreVector(++cpt,
                        (Diameters) dvector.get(index.intValue()));
                dejaFait.put(index, line);
                dejaFait2.add((Diameters) dvector.get(index.intValue()));
                p.refDiam1 = line.get(0).toString();
            } else {
                Vector line = (Vector) dejaFait.get(index);
                p.refDiam1 = line.get(0).toString();
            }

            //pipe2
            index = new Integer(p.refDiam2);

            if (p.l2 != 0) {
                if (!dejaFait.containsKey(index)) {
                    Vector line = diametreVector(++cpt,
                            (Diameters) dvector.get(index.intValue()));
                    dejaFait.put(index, line);
                    dejaFait2.add((Diameters) dvector.get(index.intValue()));
                    p.refDiam2 = line.get(0).toString();
                } else {
                    Vector line = (Vector) dejaFait.get(index);
                    p.refDiam2 = line.get(0).toString();
                }
            }
        }

        // pipe list
        enun = dejaFait.elements();
        diametresContent = new Vector();

        while (enun.hasMoreElements()) {
            diametresContent.add((Vector) enun.nextElement());
        }

        //ajoute les diametres pas faits
        enun = dvector.elements();

        while (enun.hasMoreElements()) {
            Diameters item = (Diameters) enun.nextElement();

            if (!dejaFait2.contains(item)) {
                diametresContent.add(diametreVector(++cpt, item));
            }
        }

        //ajoute les orifices
        orificeContent = new Vector();

        for (Enumeration e = ovector.elements(); e.hasMoreElements();) {
            orificeContent.add("" + ((Orifices) e.nextElement()).diam); 
        }
    }

    private Vector diametreVector(int cpt, Diameters d) {
        Vector line = new Vector();
        line.add("D" + Tools.doubleFormat("000", cpt));  
        line.add(d.nominal);
        line.add("" + d.SDR); 
        line.add("" + d.diam); 
        line.add("" + d.cost); 
        line.add("" + d.pression); 
        line.add("" + d.type); 
        line.add("" + d.rugosite); 

        return line;
    }

    /** Identifie tous les chemins des noeuds a la source */
    private void getAllPath() {
        int i = 0;

        while (i != topo.nvector.size()) {
            Nodes nodes = (Nodes) topo.nvector.elementAt(i);
            topo.pvector.getPath(nodes.nodes, nodes.path, topo.nvector,
                nodes.taps);
            i++;
        }
    }

    /** renvoie le design resultat de make design*/
    public Vector getDesignData(double hsource) {
        Vector v = new Vector();

        //ajout de la source
        Vector source = new Vector();
        source.add(((Nodes) topo.nvector.get(0)).nodes);
        source.add(((Nodes) topo.nvector.get(0)).nodes);
        source.add(new Double(hsource));
        
        for (int i = 0; i < 8; i++)
            source.add("0"); 

        v.add(source);

        //tuyaux restant
        for (int i = 0; i < topo.pvector.size(); i++) {
            Pipes pipes = (Pipes) topo.pvector.get(i);
            Nodes nodes = (Nodes) topo.nvector.get(i + 1);

            Vector line = new Vector();
            line.add(pipes.nodes_end);
            line.add(pipes.nodes_beg);
            line.add(String.valueOf(nodes.height + hsource));
            line.add(String.valueOf(pipes.length));
            line.add(String.valueOf(nodes.taps));
            line.add(Tools.doubleFormat("0.###", pipes.l1)); 
            line.add(Tools.doubleFormat("0.###", pipes.l2)); 
            line.add(pipes.refDiam1);
            line.add(pipes.refDiam2);

            if (topo.tvector.isTaps(nodes.nodes)) {
                Taps taps = (Taps) topo.tvector.get(i - topo.pvector.size() +
                        topo.tvector.size());
                line.add(Tools.doubleFormat("0.########", taps.orif_ideal)); 
                line.add(Tools.doubleFormat("0.########", taps.orif_com)); 
            } else {
                line.add("0"); 
                line.add("0"); 
            }

            v.add(line);
        }

        //ajout des diametres
        Enumeration enun = diametresContent.elements();

        while (enun.hasMoreElements()) {
            v.add(enun.nextElement());
        }

        //ajout des orifices
        enun = orificeContent.elements();

        while (enun.hasMoreElements()) {
            Vector z = new Vector();
            z.add(enun.nextElement().toString());
            z.add("N"); 
            z.add("N"); 
            z.add("N"); 
            z.add("N"); 
            z.add("N"); 
            v.add(z);
        }

        return v;
    }

    /** renvoie les r\u00E9sultats de pression constat\u00E9*/
    public Vector getResultPressureData(double hsource) {
        Vector v = new Vector();

        //ajout de la source
        Vector source = new Vector();
        source.add(((Nodes) topo.nvector.get(0)).nodes);
        source.add(Tools.doubleFormat("0.##", hsource));
        for (int i = 0; i < 2; i++)
            source.add("0"); 

        v.add(source);

        //noeuds restants
        for (int i = 1; i < (topo.pvector.size() + 1); i++) {
            Nodes nodes = (Nodes) topo.nvector.get(i);
            Vector line = new Vector();
            line.add(nodes.nodes);
            line.add(Tools.doubleFormat("0.##", nodes.height + hsource)); 
            line.add(Tools.doubleFormat("0.##", nodes.pressure)); 
            line.add(Tools.doubleFormat("0.##", nodes.suction)); 
            v.add(line);
        }

        return v;
    }

    /** renvoie les r\u00E9sultats de pression constat\u00E9*/
    public static Vector getResultPressureHeader() {
        Vector v = new Vector();
        v.add(Messages.getString("MakeDesign.ID")); 
        v.add(Messages.getString("MakeDesign.Height")); 
        v.add(Messages.getString("MakeDesign.Pressure")); 
        v.add(Messages.getString("MakeDesign.Suction")); 

        return v;
    }

    static double[] calculProba(double outflow, int n, double p, double qualite) {
        //double pseudofrac = CalculSpeudoFrac( n ,  p , qualite );
        double[] proba = new double[n + 1];
        proba[1] = outflow;

        for (int i = 2; i < (n + 1); i++) {
            proba[i] = Math.min(outflow * CalculProba2(i, /*pseudofrac*/
                        p, qualite), outflow * i);

            if (proba[i] < outflow) {
                proba[i] = outflow;
            }
        }

        //proba[n] = outflow * p * n;
        return proba;
    }

    /* calcul de la pseudofrac telque source alimente 40 pour 100 des robinets */
    static double CalculSpeudoFrac(int n, double p, double qualite) {
        double pfrac = p;
        double l1 = qualite + ((1 - qualite) * Math.pow((1 - pfrac), n));
        double l2 = LoiNormale.probaInter(((n * p) - (n * pfrac)) / Math.pow(
                    n * pfrac * (1 - pfrac), 0.5));

        /* on cherche le speudofrac tel que la source debite pour
          40 pour 100 des robinets */
        while ((l1 < (l2 - 0.03)) || (l1 > (l2 + 0.03))) {
            pfrac = pfrac - 0.01;
            l1 = qualite + ((1 - qualite) * Math.pow((1 - pfrac), n));
            l2 = LoiNormale.probaInter(((n * p) - (n * pfrac)) / Math.pow(
                        n * pfrac * (1 - pfrac), 0.5));
        }

        return pfrac;
    }

    static double CalculProba2(int n, double pfrac, double proba) {
        double resu = 0;

        /* Calcul de la qualite de service */
        double proba1 = (proba * (1 - Math.pow(1 - pfrac, n))) +
            Math.pow((1 - pfrac), n);

        /* Recherche dans la table de t = (resu-np)/(npq) */
        double t = LoiNormale.param(proba1);

        /* Calcul du resu = t * npq + np */
        resu = (t * Math.pow(n * pfrac * (1 - pfrac), 0.5)) + (n * pfrac);

        return resu;
    }
}
