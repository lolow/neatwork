package neatwork.project;

import neatwork.utils.*;

import java.util.*;


/**
 * Class defining a design
 * @author L. DROUET
 * @version 1.0
 */
public class Design extends Network {
    private Hashtable diamTable;
    private Hashtable loadFactor;
    private Vector<Object> orifice;

    public Design(String name, String content, Properties defaultProperties) {
        super(Project.TYPE_DESIGN, name);
        setProperties(defaultProperties);
        this.setContent(content);
    }

    //GESTION DU DESIGN//

    /** mise a jour du design */
    public void setContent(String content) {
        //nettoie les erreurs
        freeInfoModif();

        //decoupage des lignes
        Vector v = Tools.readCSV(content);

        //mise a jour
        Properties properties = new Properties(getProperties());
        diamTable = new Hashtable();
        loadFactor = new Hashtable();
        orifice = new Vector<Object>();
        removeNetwork();

        //passe 1 : node et properties
        for (Enumeration<?> e = v.elements(); e.hasMoreElements();) {
            Vector<Object> t = (Vector<Object>) e.nextElement();

            //diametre
            if ((t.size() == 9)) {
                addDiameter(t);
            }

            //node
            if (t.size() == 7) {
                addNode(t);
            }

            //properties
            if ((t.size() == 2)) {
                properties.setProperty("topo." + t.get(0).toString() + 
                    ".value", t.get(1).toString()); 
            }

            //loadfactor
            if ((t.size() == 3)) {
                loadFactor.put(t.get(0).toString(), t.get(1).toString());
            }

            //orifice
            if ((t.size() == 6)) {
                orifice.add(t.get(0));
            }
        }

        //passe 2 : pipes
        for (Enumeration<?> e = v.elements(); e.hasMoreElements();) {
            Vector<Object> t = (Vector<Object>) e.nextElement();

            //pipe
            if (t.size() == 8) {
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

    /** renvoie le vecteur des orifices*/
    public Vector<Object> getOrifices() {
        return orifice;
    }

    /** renvoie le string qui contient la topographie*/
    public String getContent() {
        String content = ""; 

        //ajoute les noeuds (7 champs)
        content += getNodesContent();

        //ajoute les pipes ( 8 champs)
        content += getPipesContent();

        //ajoute les properties (2 champs)
        content += getPropertiesContent();

        //ajoute les diametres (9 champs)
        content += getDiametersContent();

        //ajoute les orifices (6 champs)
        content += getOrificesContent();

        return content;
    }

    public String getNodesContent() {
        String content = ""; 

        //ajoute les noeuds (7 champs)
        content += "!Nodes\n"; 

        //content += "!Name-Heigth-X-Y-orifideal-orifcomercial-Type\n";
        Iterator iter = getNodeIterator();

        while (iter.hasNext())
            content += (getNodeCSV((Node) iter.next()) + "\n"); 

        return content;
    }

    /** renvoie le csv d'un node pour un design*/
    private String getNodeCSV(Node node) {
        return node.getName() + "," + node.getHeight() + "," +  
        node.getCoordX() + "," + node.getCoordY() + "," + node.getOrifice() +  
        "," + node.getComercialOrifice() + "," + node.getType();  
    }

    public String getPipesContent() {
        String content = ""; 

        //ajoute les pipes ( 8 champs)
        content += "!Pipes\n"; 

        //content += "!Beg-End-Length\n";
        Iterator iter = getPipeIterator();

        while (iter.hasNext())
            content += (getPipeCSV((Pipe) iter.next()) + "\n"); 

        return content;
    }

    /** renvoie le csv d'un pipe pour un topo*/
    private String getPipeCSV(Pipe pipe) {
        return pipe.getBegin() + "," + pipe.getEnd() + "," + pipe.getLength() +  
        "," + pipe.getLength1() + "," + pipe.getRefDiam1() + "," +   //$NON-NLS-3$
        pipe.getLength2() + "," + pipe.getRefDiam2() + "," + "N";   //$NON-NLS-3$
    }

    public String getDiametersContent() {
        String content = ""; 

        //ajoute les diametres ( 9 champs)
        content += "!Diameters\n"; 

        Iterator iter = diamTable.keySet().iterator();

        while (iter.hasNext())
            content += (getDiameterCSV(iter.next().toString()) + "\n"); 

        return content;
    }

    public String getOrificesContent() {
        String content = ""; 

        //ajoute les orifices ( 6 champs)
        content += "!Orifices\n"; 

        Iterator iter = orifice.iterator();

        while (iter.hasNext())
            content += (iter.next().toString() + ",N,N,N,N,N\n"); 

        return content;
    }

    public Hashtable getDiamTable() {
        return diamTable;
    }

    public Hashtable getLoadFactorTable() {
        return loadFactor;
    }

    public void setLoadFactorTable(Hashtable table) {
        loadFactor.clear();
        loadFactor.putAll(table);
    }

    /** renvoie le csv d'un pipe pour un topo*/
    private String getDiameterCSV(String ref) {
        Diameter diam = (Diameter) diamTable.get(ref);

        return ref + "," + diam.getNominal() + "," + diam.getSdr() + "," +   //$NON-NLS-3$
        diam.getDiameter() + "," + diam.getCost() + "," + diam.getMaxLength() +  
        "," + diam.getType() + "," + diam.getRoughness() + "," + "N";   //$NON-NLS-3$ //$NON-NLS-4$
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

        //ajoute les loadfactors ( 3 champs )
        content += "!loadfactor\n"; 
        iter = loadFactor.keys();

        while (iter.hasMoreElements()) {
            String name = iter.nextElement().toString();
            content += (name + "," + loadFactor.get(name).toString() + ",0\n");  
        }

        return content;
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

        //orifice ideal
        try {
            n.setOrifice(Double.parseDouble(v.get(4).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //orifice commercial
        try {
            n.setComercialOrifice(Double.parseDouble(v.get(5).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //type
        try {
            n.setType(Integer.parseInt(v.get(6).toString()));

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
        p.setBegin(v.get(0).toString());

        //end
        p.setEnd(v.get(1).toString());

        //length
        try {
            p.setLength(Double.parseDouble(v.get(2).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //length1
        try {
            p.setLength1(Double.parseDouble(v.get(3).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //refDiam1
        p.setRefDiam1(v.get(4).toString());

        //length2
        try {
            p.setLength2(Double.parseDouble(v.get(5).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //refDiam2
        p.setRefDiam2(v.get(6).toString());

        //ajout
        if (isOk) {
            addPipe(p);
        }
    }

    /** ajoute le diametre*/
    private void addDiameter(Vector v) {
        boolean isOk = true;
        Diameter d = new Diameter();

        // nominal;
        d.setNominal(v.get(1).toString());

        // sdr;
        try {
            d.setSdr(Double.parseDouble(v.get(2).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        // diameter;
        try {
            d.setDiameter(Double.parseDouble(v.get(3).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        // cost;
        try {
            d.setCost(Double.parseDouble(v.get(4).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        // maxLength;
        try {
            d.setMaxLength(Double.parseDouble(v.get(5).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        // type;
        try {
            d.setType(Integer.parseInt(v.get(6).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        // roughness;
        try {
            d.setRoughness(Double.parseDouble(v.get(7).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //ajout
        if (isOk) {
            diamTable.put(v.get(0), d);
        }
    }

    //rafraichit les ref au design avec les donnees de la database
    public void refreshDesign(Database database) {
        Enumeration e = diamTable.keys();

        while (e.hasMoreElements()) {
            String clef = e.nextElement().toString();
            Diameter item = (Diameter) diamTable.get(clef);
            Diameter finded = database.getDiameter(item);

            if (finded != null) {
                diamTable.put(clef, finded);
            }
        }

        setChanged();
        notifyObservers(new Integer(MODIF_CONTENT));
    }

    //renvoie le cout du design
    public double getCost() {
        double cost = 0;
        Iterator iter = getPipeIterator();

        while (iter.hasNext()) {
            Pipe item = (Pipe) iter.next();

            if (item.getLength1() > 0) {
                Diameter diam = (Diameter) diamTable.get(item.getRefDiam1());
                cost += (diam.getCost() * item.getLength1());
            }

            if (item.getLength2() > 0) {
                Diameter diam = (Diameter) diamTable.get(item.getRefDiam2());
                cost += (diam.getCost() * item.getLength2());
            }
        }

        return cost;
    }

    //renvoie le cout du design
    public Hashtable getSummary() {
        Hashtable h = new Hashtable();

        for (Iterator i = getPipeIterator(); i.hasNext();) {
            Pipe p = (Pipe) i.next();

            if (h.get(p.getRefDiam1()) == null) {
                h.put(p.getRefDiam1(), new Double(0));
            }

            if (h.get(p.getRefDiam2()) == null) {
                h.put(p.getRefDiam2(), new Double(0));
            }

            if (!p.getRefDiam1().equals("0")) { 
                h.put(p.getRefDiam1(),
                    new Double(((Double) h.get(p.getRefDiam1())).doubleValue() +
                        p.getLength1()));
            }

            if (!p.getRefDiam2().equals("0")) { 
                h.put(p.getRefDiam2(),
                    new Double(((Double) h.get(p.getRefDiam2())).doubleValue() +
                        p.getLength2()));
            }
        }

        h.remove("0"); 

        return h;
    }

    //core

    /** renvoie un vecteur correspondant au core design*/
    public Vector getCoreDesign() {
    	double hsource = getSource().getHeight();
        //tri des noeuds
        Collections.sort(nodeList, new Node());

        Vector data = new Vector();

        //source
        String source = getSource().getName();
        Vector pipe0 = new Vector();
        pipe0.add(source);
        pipe0.add(source);

        for (int i = 0; i < 9; i++)
            pipe0.add("0"); 

        data.add(pipe0);

        //autre
        for (Iterator i = pipeList.iterator(); i.hasNext();) {
            Pipe pipe = (Pipe) i.next();
            Node node = (Node) getNode(pipe.getEnd());
            Vector line = new Vector();
            line.add(pipe.getEnd()); //end ID
            line.add(pipe.getBegin()); //pred ID
            line.add("" + (node.getHeight() - hsource)); //height 
            line.add("" + pipe.getLength()); //length 
            line.add((node.getType() == Node.TYPE_FAUCET) ? "1" : "0"); //#taps  
            line.add("" + pipe.getLength1()); 
            line.add("" + pipe.getLength2()); 

            Diameter diam = (Diameter) diamTable.get(pipe.getRefDiam1());
            line.add("" + diam.getDiameter()); 

            if (pipe.getLength2() > 0) {
                diam = (Diameter) diamTable.get(pipe.getRefDiam2());
                line.add("" + diam.getDiameter()); 
            } else {
                line.add("0"); 
            }

            line.add("" + node.getOrifice()); 
            line.add("" + node.getComercialOrifice()); 
            data.add(line);
        }

        //tri des donnees
        Object source0 = data.remove(0);
        Collections.sort(data, new VectorComparator(0));
        Collections.sort(data, new VectorComparator(4));
        data.insertElementAt(source0, 0);

        return data;
    }

    /** renvoie un vecteur correspondant au core orifice*/
    public Vector getCoreOrifice() {
        TreeSet data = new TreeSet();

        for (Iterator i = nodeList.iterator(); i.hasNext();) {
            data.add("" + ((Node) i.next()).getComercialOrifice()); 
        }

        Vector data0 = new Vector();
        Iterator e = data.iterator();

        while (e.hasNext()) {
            Vector line = new Vector();
            line.add(e.next());
            data0.add(line);
        }

        return data0;
    }

    /** renvoie un vecteur correspondant au core Diameter*/
    public Vector getCoreDiameter() {
        Vector data = new Vector();
        Enumeration e = diamTable.elements();

        while (e.hasMoreElements()) {
            Diameter diam = (Diameter) e.nextElement();
            Vector line = new Vector();
            line.add(diam.getNominal());
            line.add("" + diam.getSdr()); 
            line.add("" + diam.getDiameter()); 
            line.add("" + diam.getCost()); 
            line.add("" + diam.getMaxLength()); 
            line.add("" + diam.getType()); 
            line.add("" + diam.getRoughness()); 
            data.add(line);
        }

        return data;
    }

    //extract topo

    /**
     * extrait le content de la topography correspondant au design
     */
    public String extractTopoContent() {
        String content = ""; 
        Hashtable nbfaucet = new Hashtable();

        //nodes
        //nb faucet
        Iterator iter = getNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();
            item.setNbTaps(0);

            if (item.getType() == Node.TYPE_FAUCET) {
                item.setNbTaps(1);
            }

            String name = item.getName();

            if (name.indexOf("_") > 0) { 
                name = name.substring(0, name.indexOf("_")); 

                int nb = 1;

                if (nbfaucet.get(name) != null) {
                    nb = ((Integer) nbfaucet.get(name)).intValue() + 1;
                }

                nbfaucet.put(name, new Integer(nb));
            }
        }

        //nodes
        iter = getNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getName().indexOf("_") < 0) { 

                if (nbfaucet.get(item.getName()) != null) {
                    item.setNbTaps(((Integer) nbfaucet.get(item.getName())).intValue());
                }

                content += (Topographie.getNodeCSV(item) + "\n"); 
            }
        }

        //pipes
        iter = getPipeIterator();

        while (iter.hasNext()) {
            Pipe item = (Pipe) iter.next();

            if ((item.getBegin().indexOf("_") < 0) && 
                    (item.getEnd().indexOf("_") < 0)) { 
                content += (Topographie.getPipeCSV(item) + "\n"); 
            }
        }

        //properties
        content += "!Default properties\n"; 

        Enumeration e = getProperties().propertyNames();

        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();

            if (name.startsWith("topo.") && name.endsWith(".value")) {  
                content += (name.substring(5, name.length() - 6) + "," + 
                getProperties().getProperty(name) + "\n"); 
            }
        }

        return content;
    }
}
