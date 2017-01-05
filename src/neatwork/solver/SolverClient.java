package neatwork.solver;

import neatwork.project.*;

import java.util.*;


/**
 * Client pour le service Solver
 * @author L. DROUET
 * @version 1.0
 */
public class SolverClient extends AbstractSolver {
    private Vector nodePressureMakeDesign;
    private String designContentMakeDesign;
    private String simulationContent;

    public SolverClient(Properties prop) {

    }

    public Vector getNodePressureMakeDesign() {
        Vector v = new Vector(nodePressureMakeDesign);
        nodePressureMakeDesign = null;

        return v;
    }

    public String getDesignContentMakeDesign() {
        String s = new String(designContentMakeDesign);
        designContentMakeDesign = ""; 

        return s;
    }

    public void makeDesign(Topographie topographie, String orificesSet,
        String diametersSet, Hashtable loadFactor, Vector constraints, double source) {
        Vector param = new Vector();
        Vector v = (Vector) sendMsg("makeDesign", param); 
        nodePressureMakeDesign = (Vector) v.get(0);
        designContentMakeDesign = v.get(1).toString();
    }

    public String getSimulationContent() {
        String s = new String(simulationContent);
        simulationContent = ""; 

        return s;
    }

    public void simulation(Design design, Properties parameters,
        Hashtable faucetRef) {
        Vector param = new Vector();
        simulationContent = sendMsg("simulation", param).toString(); 
    }

    private Object sendMsg(String methode, Vector param) {
        return null;
    }
}
