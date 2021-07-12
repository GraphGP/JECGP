package cartesianNetwork;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Class that represents an ECGP Module
 * @author Björn Piepenbrink
 *
 */
public class Module  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	//moduleHeader
	//	header[0] = module identifier
	//	
	//	header[1] = number of module inputs
	//	number of module inputs in range [ 2 , 2n ]
	//	n = number of node in the Module	
	private int[] header;
	
	//Ouputs of the Module
	//each Output consists of 2 Integers
	//max n outputs
	//int.get(x)[0] = node address that the output x is taken from
	//int.get(x)[1] = node output that the output x is taken from
	private ArrayList<int[]> moduleOutputs;
	
	//module Body consisting of nodes
	//minimum of 2 nodes
	//maximum set by User
	//node can only contatin primitive functions (Node Type 0)
	private ArrayList<NodeECGP> nodes;
	
	private boolean[] usedNodes;
	private boolean usedNodesCalculated;
	
	/**
	 * creates a Module with the given Values
	 * for explicit Values see inside the Module-Class
	 * @param header The Header consisting of Module Identifier and Number of Inputs
	 * @param moduleOutputs The Output-Adresses consisting of Adress and Outputnumber
	 * @param nodes The nodes of the Module (all should have type 0)
	 */
	public Module(int[] header, ArrayList<int[]> moduleOutputs, ArrayList<NodeECGP> nodes){
		this.header = header;
		this.moduleOutputs = moduleOutputs;
		if(moduleOutputs.size()==0){
			throw new IllegalArgumentException("Outputs of Module cannot be 0");
		}
		this.nodes = nodes;
		usedNodesCalculated=false;
	}

	/**
	 * copies the Module
	 * @return an identical Copy of the Module
	 */
	public Module copy(){
		int[] newHeader = new int[header.length];
		ArrayList<int[]> newOutputs = new ArrayList<>();
		ArrayList<NodeECGP> newNodes = new ArrayList<>();
		for(int i=0;i<header.length;i++){
			newHeader[i] = header[i];
		}
		for(int[] out : moduleOutputs){
			int[] outNew = new int[2];
			outNew[0] = out[0];
			outNew[1] = out[1];
			newOutputs.add(outNew);
		}
		for(NodeECGP node : nodes){
			newNodes.add(node.copy());
		}
		return new Module(newHeader,newOutputs,newNodes);
	}
	
	public int[] getHeader() {
		return header;
	}
	
	public int getNrOfNodes(){
		return nodes.size();
	}
	
	public int getNrOfInputs(){
		return header[1];
	}
	
	public int getNrOfOutputs(){
		return moduleOutputs.size();
	}
	
	public int getIdentifier(){
		return header[0];
	}

	public void setHeader(int[] header) {
		this.header = header;
	}

	public ArrayList<int[]> getModuleOutputs() {
		return moduleOutputs;
	}

	public void setModuleOutputs(ArrayList<int[]> moduleOutputs) {
		this.moduleOutputs = moduleOutputs;
	}

	public ArrayList<NodeECGP> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<NodeECGP> nodes) {
		this.nodes = nodes;
	}

	public boolean[] getUsedNodes() {
		if(usedNodesCalculated){
			return usedNodes;
		}
		throw new IllegalStateException("usedNodes of the module have not been calculated");
	}

	public void setUsedNodes(boolean[] usedNodes) {
		if(!usedNodesCalculated){
			this.usedNodes=usedNodes;
			this.usedNodesCalculated=true;
		}
		else{
			throw new IllegalStateException("usedNodes of the module have already been calculated");
		}
	}

	public boolean usedNodesHasBeenCalculated() {
		return usedNodesCalculated;
	}
	
	public void resetUsedNodesHasBeenCalculated() {
		this.usedNodesCalculated=false;
	}
}
