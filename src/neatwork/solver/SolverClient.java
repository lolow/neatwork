package neatwork.solver;

import neatwork.project.*;

import java.net.*;

import java.util.*;


//import org.apache.soap.*;
//import org.apache.soap.rpc.*;

/**
 * Client pour le service Solver
 * @author L. DROUET
 * @version 1.0
 */
public class SolverClient extends AbstractSolver {
    private Vector nodePressureMakeDesign;
    private String designContentMakeDesign;
    private String simulationContent;
    private String user;
    private String uri;
    private URL url;

    public SolverClient(Properties prop) {
        this.user = prop.getProperty("appli.user", "guest");  
        this.uri = prop.getProperty("appli.urisolver"); 

        try {
            this.url = new URL(prop.getProperty("appli.server")); 
        } catch (MalformedURLException e) {
        }
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

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("topographie", String.class, topographie.getContent(), null));
        //    param.addElement(new Parameter("orificesSet", String.class, orificesSet, null));
        //    param.addElement(new Parameter("diametersSet", String.class, diametersSet, null));
        //    param.addElement(new Parameter("loadFactor", Hashtable.class, loadFactor, null));
        //    param.addElement(new Parameter("constraints", Vector.class, constraints, null));
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

        //    param.addElement(new Parameter("user", String.class, user, null));
        //    param.addElement(new Parameter("design", String.class, design.getContent(), null));
        //    param.addElement(new Parameter("parameters", Hashtable.class, new Hashtable(parameters), null));
        //    param.addElement(new Parameter("faucetRef", Hashtable.class, faucetRef, null));
        simulationContent = sendMsg("simulation", param).toString(); 
    }

    private Object sendMsg(String methode, Vector param) {
        //    Parameter result = null;
        //
        //    Call remoteMethod = new Call();
        //    remoteMethod.setTargetObjectURI(uri);
        //    remoteMethod.setMethodName(methode);
        //    remoteMethod.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
        //    remoteMethod.setParams(param);
        //
        //    try {
        //      setStatus("Invoking server...");
        //      setProgress(0);
        //      long tick = getTick();
        //
        //      Response reponse = remoteMethod.invoke(url, "");
        //      setStatus("Completed. (" + getElapsedTime(tick)+ ")");
        //      setProgress(100);
        //
        //      if (reponse.generatedFault()) {
        //        Fault f = reponse.getFault();
        //        setStatus("CALL FAILED:\nFault Code = " + f.getFaultCode() +
        //                " Fault String = " + f.getFaultString());
        //      }else{
        //        result = reponse.getReturnValue();
        //      }
        //    }
        //    catch (Exception e) {}
        //
        //    return (result==null) ? null : result.getValue();
        return null;
    }
}
