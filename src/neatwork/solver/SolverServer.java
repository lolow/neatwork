package neatwork.solver;

import neatwork.project.*;

import java.util.*;

/**
 * Version serveur du solver methodes : makeDesign simulation
 * 
 * @author L. DROUET
 * @version 1.0
 */
public class SolverServer {

	public SolverServer() {
	}

	public Vector makeDesign(String user, String topographie, String orificesSet, String diametersSet,
			Hashtable loadFactor, Vector constraints) {
		SolverDisk solver = new SolverDisk();
		Topographie t = new Topographie("temp", topographie, new Properties());

		t.makeExpandedTopo();
		solver.makeDesign(t, orificesSet, diametersSet, loadFactor, constraints, 0);

		Vector res = new Vector();
		res.add(solver.getNodePressureMakeDesign());
		res.add(solver.getDesignContentMakeDesign());

		return res;
	}

	public String simulation(String user, String design, Hashtable parameters, Hashtable faucetRef) {
		SolverDisk solver = new SolverDisk();
		Properties p = new Properties();
		Design d = new Design("temp", design, p);
		p = new Properties();
		p.putAll(parameters);
		solver.simulation(d, p, faucetRef);

		return solver.getSimulationContent();
	}
}
