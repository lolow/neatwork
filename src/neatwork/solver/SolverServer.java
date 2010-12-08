package neatwork.solver;

import neatwork.core.*;

import neatwork.project.*;

import java.util.*;


/**
 * Version serveur du solver
 * methodes : makeDesign simulation
 * @author L. DROUET
 * @version 1.0
 */
public class SolverServer {
    //private static Hashtable data;
    private static SolverServer server;
    private MakeDesign makeDesign;
    private Topographie topographie;

    public SolverServer() {
    }

    public Vector makeDesign(String user, String topographie,
        String orificesSet, String diametersSet, Hashtable loadFactor,
        Vector constraints) {
        SolverDisk solver = new SolverDisk();
        Topographie t = new Topographie("temp", topographie, new Properties()); //$NON-NLS-1$
        
        t.makeExpandedTopo();
        solver.makeDesign(t, orificesSet, diametersSet, loadFactor, constraints,0);

        Vector res = new Vector();
        res.add(solver.getNodePressureMakeDesign());
        res.add(solver.getDesignContentMakeDesign());
        

        return res;
    }

    public String simulation(String user, String design, Hashtable parameters,
        Hashtable faucetRef) {
        SolverDisk solver = new SolverDisk();
        Properties p = new Properties();
        Design d = new Design("temp", design, p); //$NON-NLS-1$
        p = new Properties();
        p.putAll(parameters);
        solver.simulation(d, p, faucetRef);

        return solver.getSimulationContent();
    }
}
