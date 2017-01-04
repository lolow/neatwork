package neatwork.project;

import neatwork.utils.*;

import java.util.*;


/**
 * Classe definissant une topographie
 * @author L. DROUET
 * @version 1.0
 */
public class Topographie extends Network {
    //PARTIE CALCULATOIRE
    private List nodeList2;
    private List pipeList2;

    public Topographie(String name, String content, Properties defaultProperties) {
        super(Project.TYPE_TOPO, name);
        setProperties(defaultProperties);
        this.setContent(content);
    }

    //GESTION DE LA TOPOGRAPHIE//

    /** ajout une topographie*/
    public void setContent(String content) {
        //nettoie les erreurs
        freeInfoModif();

        //decoupage des lignes
        Vector v = Tools.readCSV(content);

        //mise a jour
        Properties properties = new Properties(getProperties());
        removeNetwork();

        //passe 1 : node et properties
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Vector t = (Vector) e.nextElement();

            //node
            if (t.size() == 6) {
                addNode(t);
            }

            //properties
            if ((t.size() == 2)) {
                properties.setProperty("topo." + t.get(0).toString() + 
                    ".value", t.get(1).toString()); 
            }
        }

        //passe 2 : pipes
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Vector t = (Vector) e.nextElement();

            //pipe
            if (t.size() == 3) {
                addPipe(t);
            }
        }

        //SI pas de source ajout d'une source
        if (!thereIsAlreadyASource()) {
            addNewSource();
        }

        //trie des noeuds
        Collections.sort(nodeList, new Node());

        if (getInfoModif().indexOf(Project.ERROR) == -1) {
            setProperties(properties);
            super.setContent(getContent());
        }
    }

    /** renvoie le string qui contient la topographie*/
    public String getContent() {
        String content = ""; 

        //ajoute les noeuds (6 champs)
        content += getNodesContent();

        //ajoute les pipes ( 3 champs)
        content += getPipesContent();

        //ajoute les properties (2 champs)
        content += getPropertiesContent();

        return content;
    }

    public String getNodesContent() {
        String content = ""; 

        //ajoute les noeuds (6 champs)
        content += "!Nodes\n"; 

        //content += "!Name-Heigth-X-Y-NbTaps-Type\n";
        Iterator iter = getNodeIterator();

        while (iter.hasNext())
            content += (getNodeCSV((Node) iter.next()) + "\n"); 

        return content;
    }

    public String getPipesContent() {
        String content = ""; 

        //ajoute les pipes ( 3 champs)
        content += "!Pipes\n"; 

        //content += "!Beg-End-Length\n";
        Iterator iter = getPipeIterator();

        while (iter.hasNext())
            content += (getPipeCSV((Pipe) iter.next()) + "\n"); 

        return content;
    }

    public String getPropertiesContent() {
        String content = ""; 

        //ajoute les properties ( 2 champs)
        content += "!Default properties\n"; 

        //content += "!Name-Value\n";
        Enumeration iter = getProperties().propertyNames();

        while (iter.hasMoreElements()) {
            String name = iter.nextElement().toString();

            if (name.startsWith("topo.") && name.endsWith(".value")) {  
                content += (name.substring(5, name.length() - 6) + "," + 
                getProperties().getProperty(name) + "\n"); 
            }
        }

        return content;
    }

    /** renvoie le csv d'un node pour un topo*/
    public static String getNodeCSV(Node node) {
        return node.getName() + "," + node.getHeight() + "," +  
        node.getCoordX() + "," + node.getCoordY() + "," + node.getNbTaps() +  
        "," + node.getType(); 
    }

    /** renvoie le csv d'un pipe pour un topo*/
    public static String getPipeCSV(Pipe pipe) {
        return pipe.getBegin() + "," + pipe.getEnd() + "," + pipe.getLength();  
    }

    /** ajoute le noeud*/
    private void addNode(Vector v) {
        boolean isOk = true;
        Node n = new Node();

        //name
        n.setName(v.get(0).toString());

        //height
        try {
            n.setHeight(Double.parseDouble(v.get(1).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //coordX
        try {
            n.setCoordX(Double.parseDouble(v.get(2).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //coordY
        try {
            n.setCoordY(Double.parseDouble(v.get(3).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //nbTaps
        try {
            n.setNbTaps(Integer.parseInt(v.get(4).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //type
        try {
            n.setType(Integer.parseInt(v.get(5).toString()));

            if ((n.getType() > 2) || (n.getType() < 0)) {
                isOk = false;
            }
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //ajout
        if (isOk) {
            addNode(n);
        }
    }

    /** ajoute le tuyau*/
    private void addPipe(Vector v) {
        boolean isOk = true;
        Pipe p = new Pipe();

        //begin
        String begin = v.get(0).toString();
        p.setBegin(begin);

        //end
        String end = v.get(1).toString();
        p.setEnd(end);

        //length
        try {
            p.setLength(Double.parseDouble(v.get(2).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //ajout
        if (isOk) {
            addPipe(p);
        }
    }

    /**
    * renvoie pour chaque noeud le nombre de robinet e charge
    * Attention, ne fonctionne qu'avec un topo deja deployer
    */
    public Hashtable getLoadTaps() {
        //remplit la hashtable
        Hashtable table = new Hashtable();
        Iterator iter = nodeList2.iterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();
            table.put(node.getName(), "0"); 
        }

        //pour chaque faucet on, remonte a la source en incrementant
        iter = nodeList2.iterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();

            if (node.getType() == Node.TYPE_FAUCET) {
                Node pion = new Node(node);

                while (pion.getType() != Node.TYPE_RESERVOIR) {
                    String pred = getPredPipe(pion, pipeList2).getBegin();
                    int passage = Integer.parseInt(table.get(pred).toString());
                    table.put(pred, "" + ++passage); 
                    pion = getNode(pred, nodeList2);
                }

                table.put(node.getName(), "1"); 
            }
        }

        return table;
    }

    /**
    * renvoie pour chaque noeud le facteur de charge
    * Attention, ne fonctionne qu'avec un topo deja deployer
    */
    public Hashtable getLoadFactor(Hashtable loadTaps) {
        double targetflow = Double.parseDouble(getProperties().getProperty("topo.targetflow.value")); 
        double opentaps = Double.parseDouble(getProperties().getProperty("topo.opentaps.value")); 
        double qualite = Double.parseDouble(getProperties().getProperty("topo.servicequal.value")); 

        //Calcul des probabilites
        double[] proba = calculProba(targetflow,
                getNbNode(Node.TYPE_FAUCET, nodeList2), opentaps, qualite);

        //affect les load factors
        Hashtable table = new Hashtable();
        Enumeration e = loadTaps.keys();

        while (e.hasMoreElements()) {
            String item = e.nextElement().toString();
            int nbTaps = Integer.parseInt(loadTaps.get(item).toString());
            table.put(item,
                new Double(Tools.doubleFormat("0.##", proba[nbTaps] / targetflow))); 
        }

        return table;
    }

    //renvoie le noeud "name" de la liste "list"
    private Node getNode(String name, List nodelist) {
        Node node = null;
        Iterator iter = nodelist.iterator();

        while (iter.hasNext() && (node == null)) {
            Node item = (Node) iter.next();

            if (item.getName().equals(name)) {
                node = item;
            }
        }

        return node;
    }

    //renvoie le nombre de noeud d'un type "type" de la liste "list"
    private int getNbNode(int type, List nodelist) {
        int cpt = 0;
        Iterator iter = nodelist.iterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getType() == type) {
                cpt++;
            }
        }

        return cpt;
    }

    /** renvoie le tuyau predecesseur du noeud node dans la liste*/
    private Pipe getPredPipe(Node node, List pipelist) {
        Iterator iter = pipelist.iterator();
        Pipe p = null;

        while (iter.hasNext() && (p == null)) {
            Pipe pipe = (Pipe) iter.next();

            if (pipe.getEnd().equals(node.getName())) {
                p = pipe;
            }
        }

        return p;
    }

    public Pipe getExpandedPredPipe(Node node) {
        return getPredPipe(node, pipeList2);
    }

    //construit le topo modifie avec les points faucet e plusieurs robinets
    public void makeExpandedTopo() {
        nodeList2 = new Vector();
        pipeList2 = new Vector();

        //gere les faucet avec 0 robinets
        Iterator iter = nodeList.iterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if ((item.getType() == Node.TYPE_FAUCET) && (item.getNbTaps() < 1)) {
                item.setNbTaps(1);
            }
        }

        //ajout de tous les tuyaux existants
        iter = pipeList.iterator();

        while (iter.hasNext()) {
            Pipe p = new Pipe((Pipe) iter.next());
            pipeList2.add(p);
        }

        //ajout des noeuds
        iter = nodeList.iterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if ((item.getType() == Node.TYPE_FAUCET) && (item.getNbTaps() > 1)) {
                char a = 'a';

                //ajout du noeud modifie
                Node oldnode = new Node(item);
                oldnode.setType(Node.TYPE_DISPATCH);
                oldnode.setNbTaps(0);
                nodeList2.add(oldnode);

                //ajout des branches necessaires
                for (int i = 0; i < item.getNbTaps(); i++) {
                    //ajout d'un noeud
                    Node n = new Node(item);
                    n.setName(item.getName() + "_" + a++); 
                    n.setNbTaps(1);
                    nodeList2.add(n);

                    //ajout d'un pipe
                    Pipe p = new Pipe();
                    p.setBegin(item.getName());
                    p.setEnd(n.getName());
                    p.setLength(1);
                    pipeList2.add(p);
                }
            } else {
                //ajout du noeud simple
                Node n = new Node(item);
                nodeList2.add(n);
            }
        }

        //trie des noeuds
        Collections.sort(nodeList2, new Node());
    }

    public Iterator getExpandedNodeIterator() {
        return nodeList2.iterator();
    }

    public Iterator getExpandedPipeIterator() {
        return pipeList2.iterator();
    }

    //calcul de proba/////////////////////////////////////////////////

    /** effectue le calcul du tableau de proba*/
    private double[] calculProba(double outflow, int n, double p, double qualite) {
        double[] proba = new double[n + 1];
        proba[1] = outflow;

        for (int i = 2; i < (n + 1); i++) {
            proba[i] = Math.min(outflow * calculProba2(i, p, qualite),
                    outflow * i);

            if (proba[i] < outflow) {
                proba[i] = outflow;
            }
        }

        return proba;
    }

    private double calculProba2(int n, double pfrac, double proba) {
        double resu = 0;

        // Calcul de la qualite de service
        double proba1 = (proba * (1 - Math.pow(1 - pfrac, n))) +
            Math.pow((1 - pfrac), n);

        // Recherche dans la table de t = (resu-np)/(npq)
        double t = LoiNormale.param(proba1);

        // Calcul du resu = t * npq + np
        resu = (t * Math.pow(n * pfrac * (1 - pfrac), 0.5)) + (n * pfrac);

        return resu;
    }

    //CORE
    /** renvoie le vecteur core correspondant au project*/
    public Vector getCoreVector() {
        //tri des noeuds
        Collections.sort(nodeList2, new Node());

        Vector data = new Vector();

        //source
        String source = getSource().getName();
        Vector pipe = new Vector();
        pipe.add(source);
        pipe.add(source);
        pipe.add(""+getSource().getHeight()); 
        pipe.add("0"); 
        pipe.add("0"); 
        data.add(pipe);

        //autre
        for (Iterator i = nodeList2.iterator(); i.hasNext();) {
            Node node = (Node) i.next();

            if (node.getType() != Node.TYPE_RESERVOIR) {
                Pipe pred = getExpandedPredPipe(node);
                Vector line = new Vector();
                line.add(node.getName()); //node ID
                line.add(pred.getBegin()); //pred ID
                line.add("" + node.getHeight()); //height 
                line.add("" + pred.getLength()); //length 
                line.add("" + node.getNbTaps()); //#taps 
                data.add(line);
            }
        }

        return data;
    }
}
