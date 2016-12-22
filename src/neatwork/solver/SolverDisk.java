package neatwork.solver;

import neatwork.Messages;

import neatwork.core.*;

import neatwork.core.defs.*;

import neatwork.project.*;

import neatwork.utils.*;

import java.util.*;


/**
 * solver du cot� serveur ou en local
 * @author L. DROUET
 * @version 1.0
 */
public class SolverDisk extends AbstractSolver {
    private MakeDesign makeDesign;
    private Topographie topographie;
    private double hsource;

    //SIMULATION
    private MakeSimulation makeSimulation;
    private Design design;

    public SolverDisk() {
    }

    //MAKE DESIGN
    public void makeDesign(Topographie topographie, String orificesSet,
        String diametersSet, Hashtable loadFactor, Vector constraints, double hsource) {
        setProgress(0);
        setStatus(Messages.getString("SolverDisk.Initialization...")); 

        long tick = getTick();

        this.topographie = topographie;
        this.hsource = hsource;

        //creation du core Topographie
        CoreTopographie topo = new CoreTopographie(topographie.getCoreVector());
        
        //affectation des contraintes
        Enumeration e = constraints.elements();

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();
            Pipe pipe = (Pipe) item.get(1);
            Pipes findedPipe = null;
            boolean find = false;
            Enumeration enum0 = topo.pvector.elements();

            while ((!find) && (enum0.hasMoreElements())) {
                Pipes item0 = (Pipes) enum0.nextElement();
                find = (item0.nodes_beg.equals(pipe.getBegin())) &&
                    (item0.nodes_end.equals(pipe.getEnd()));

                if (find) {
                    findedPipe = item0;
                }
            }

            if (findedPipe != null) {
                int type = ((Integer) item.get(0)).intValue();

                switch (type) {
                case 0:
                    findedPipe.imposdiammin = Double.parseDouble(item.get(2)
                                                                     .toString());

                    break;

                case 1:
                    findedPipe.imposdiammax = Double.parseDouble(item.get(2)
                                                                     .toString());

                    break;

                case 2:
                    findedPipe.imposdiam1 = Double.parseDouble(item.get(2)
                                                                   .toString());
                    findedPipe.imposlength1 = findedPipe.length;

                    break;

                case 3:
                    findedPipe.imposdiam1 = Double.parseDouble(item.get(2)
                                                                   .toString());
                    findedPipe.imposlength1 = Double.parseDouble(item.get(3)
                                                                     .toString());
                    findedPipe.imposdiam2 = Double.parseDouble(item.get(4)
                                                                   .toString());

                    break;
                }
            }
        }

        //creation des ovectors
        OrificesVector ovector = new OrificesVector(Tools.readCSV(orificesSet));

        //creation des dvectors
        DiametersVector dvector = new DiametersVector(Tools.readCSV(
                    diametersSet), topographie.getProperties());

        setProgress(50);
        setStatus(Messages.getString("SolverDisk.Building_Design..")); 

        //lance le design
        makeDesign = new MakeDesign(topo, dvector, ovector,
                topographie.getProperties(), loadFactor);

        setProgress(100);
        setStatus(Messages.getString("SolverDisk.Completed.") +
            getElapsedTime(tick) + ")");  
    }

    /** Parse les resultats du design dans l'ancien format et renvoie un design valide pour neatwork*/
    public String getDesignContentMakeDesign() {
        Vector v = makeDesign.getDesignData(hsource);
        Vector design = new Vector();

        //ajout des noeuds et pipes
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Vector line = (Vector) e.nextElement();

            if (line.size() == 11) {
                Node n = getExpandedNode(line.get(0).toString());

                if (n.getType() != Node.TYPE_RESERVOIR) {
                    //ajout d'un noeud
                    Vector node = new Vector();
                    node.add(line.get(0)); //node id
                    node.add(line.get(2)); //height
                    node.add("" + n.getCoordX()); //coordX 
                    node.add("" + n.getCoordY()); //coordY 
                    node.add(line.get(9)); //orifice ideal
                    node.add(line.get(10));
                    node.add("" + n.getType()); //nature 
                    design.add(node);

                    //ajout d'un pipe
                    Vector pipe = new Vector();
                    pipe.add(line.get(1)); //begin
                    pipe.add(line.get(0)); //end
                    pipe.add(line.get(3)); //length
                    pipe.add(line.get(5)); //length1
                    pipe.add(line.get(7)); //diam1
                    pipe.add(line.get(6)); //length2
                    pipe.add(line.get(8)); //diam2
                    pipe.add("N"); 
                    design.add(pipe);
                } else {
                    //ajout de la source
                    Vector node = new Vector();
                    node.add(line.get(0)); //node id
                    node.add(line.get(2)); //height
                    node.add("" + n.getCoordX()); //coordX 
                    node.add("" + n.getCoordY()); //coordY 
                    node.add("0"); //orifice ideal 
                    node.add("0"); //orifice comercial 
                    node.add("" + n.getType()); //nature 
                    design.add(node);
                }
            }

            if (line.size() == 8) {
                Vector diam = new Vector(line);
                diam.add("N"); 
                design.add(diam);
            }

            if (line.size() == 6) {
                Vector diam = new Vector(line);
                design.add(diam);
            }
        }

        String content = Tools.getTxt(design);
        content += topographie.getPropertiesContent();

        return content;
    }

    private Node getExpandedNode(String nodeId) {
        Iterator iter = topographie.getExpandedNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getName().equals(nodeId)) {
                return item;
            }
        }

        return null;
    }

    public Vector getNodePressureMakeDesign() {
        return makeDesign.getResultPressureData(hsource);
    }

    /** execute une simulation*/
    public void simulation(Design design, Properties parameters,
        Hashtable faucetStatus) {
        this.design = design;
        setProgress(0);
        setStatus(Messages.getString("SolverDisk.Initialization...")); 

        long tick = getTick();

        //parametres
        OrificesVector ovector = new OrificesVector(design.getCoreOrifice());
        DiametersVector dvector = new DiametersVector(design.getCoreDiameter(),
                parameters);

        //creation du core Design
        CoreDesign dsg = new CoreDesign(design.getCoreDesign(), dvector, ovector);
        
        setProgress(0);
        setStatus(Messages.getString("SolverDisk.Running_simulations..")); 

        makeSimulation = new MakeSimulation(dsg, dvector, ovector, parameters,
                faucetStatus, this);

        setProgress(100);
        setStatus(Messages.getString("SolverDisk.Completed.") +
            getElapsedTime(tick) + ")");  
    }

    /** renvoie les pressions des noeuds estim�s d'une simu */
    public String getSimulationContent() {
        String content = ""; 

        //ajout du design
        content += design.getContent();

        //ajout de la simu
        content += Tools.getTxt(makeSimulation.getSimpleResultsSimu());
        content += Tools.getTxt(makeSimulation.getQuartileSimu());
        content += Tools.getTxt(makeSimulation.getSpeedSimu());
        content += Tools.getTxt(makeSimulation.getPressureSimu());
        content += makeSimulation.getPropertiesContent();

        return content;
    }
}
