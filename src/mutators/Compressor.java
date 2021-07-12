package mutators;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.ModuleList;
import cartesianNetwork.NodeECGP;

/**
 * Calss for creating a Module out of the Genotype
 * @author Björn Piepenbrink
 *
 */
public class Compressor {

	/**
	 * Generates 2 random Points and generates a Module between them (not necessary)
	 * @param indiv The Individual that should be mutated
	 * @param moduleList The ModuleList ofthe Individual
	 * @param maxModuleSize The max Size a Module can take
	 * @param maxAllowedModules The max Size of the Modulelist
	 * @throws Exception if something unexpected happens
	 */
	public static void compress(Individual indiv,ModuleList moduleList, int maxModuleSize, int maxAllowedModules) throws Exception{
		//selects 2 random Points and adds all Nodes contained between those Points into a Module
		//if there are some Type 1 or Type 2 nodes between these Points this operator does nothing
		
		//changes Nodes of Individual
		//changes Modules in moduleList
		
		if(moduleList.getModuleList().size()==maxAllowedModules&&maxAllowedModules>=0){
			return;
		}
		
		//select 2 random Points in the genotype (area cannot be longer than maxModuleSize)
		
		int randP1 = ThreadLocalRandom.current().nextInt(0,indiv.getNodes().size());
		
		if(randP1==indiv.getNodes().size()-1){
			//one node cannot be made into module
			return;
		}
		
		int endOfGenotype = randP1+maxModuleSize;
		if(endOfGenotype>=indiv.getNodes().size())endOfGenotype=indiv.getNodes().size();
		int randP2 = ThreadLocalRandom.current().nextInt(randP1+1,endOfGenotype);
		
		//check if allNodes inbetween those two Points have Functions of type 1 or type 2
		int currentFunctionType = 0;
		for(int i = randP1; i<=randP2 ; i++){
			currentFunctionType = indiv.getNodes().get(i).getNodeType();
			if(currentFunctionType==1||currentFunctionType==2){
				//do nothing
				return;
			}
		}
		
		//create Module and alter inputs from nodes after Pos1
		//delete Nodes from Individual an add One
		moduleList.addModule(createModule(indiv,moduleList, randP1, randP2));
	}
	
	/**
	 * creates a Module between 2 points
	 * (doesn't validate)
	 * @param indiv The Individual that should be mutated
	 * @param moduleList The Modulelist of the Individual
	 * @param p1 The first Point in the Genotype
	 * @param p2 The second Point in the Genotyps
	 * @return the generated Module (not added in the Modulelist)
	 * @throws Exception if something unexpected happens
	 */
	private static Module createModule(Individual indiv,ModuleList moduleList, int p1, int p2) throws Exception{
		//p1 and p2 are the node-positions that have to be encapsulated into a module 
		//(inclusive with all nodes inbetween)
		
		//copy Module Nodes
		ArrayList<NodeECGP> moduleNodes = new ArrayList<>();
		NodeECGP nodeToAdd = null;
		for(int i = p1; i<= p2 ; i++){
			nodeToAdd = indiv.getNodes().get(i).copy();
			moduleNodes.add(nodeToAdd);
		}
		
		//get Nr of Inputs
		int nrInputs=0;
		int adressOfP1 = p1+indiv.getInputAmount();
		int adressOfP2 = p2+indiv.getInputAmount();
		
		//calculate Number of Inputs for Module
		for(NodeECGP node1 : moduleNodes){//for each Node
			for(int i = 0; i<node1.getInput().size();i++){//for each Input
				if(node1.getInput().get(i)[0]<adressOfP1){
					//Node uses node outside the Module as Input
					nrInputs++;
				}
			}
		}
		
		//change Inputs of Nodes in the Module
		//and save old Inputs for later 
		//(just for inputs that reference adresses outside the module)
		ArrayList<int[]> oldInputs = new ArrayList<>();
		int inputsSet=0;
		
		for(NodeECGP node : moduleNodes){//for each Node
			for(int i = 0; i<node.getInput().size();i++){//for each Input
				if(node.getInput().get(i)[0]>=adressOfP1){
					//Node uses other node in the Module as Input
					//since p1+inputAmount ist the firstNode in the Module
					
					//set Input for the new Node to relative Node in Module
					//add nrInputs because the Inputs have the first Adresses in the module
					node.getInput().get(i)[0]=(node.getInput().get(i)[0]-adressOfP1)+nrInputs;
				}
				else{//Node uses node outside of the Module as Input
					//set each Input to a new value
					//set Node input to the given Input
					oldInputs.add(copyArray(node.getInput().get(i)));
					node.getInput().get(i)[0]=inputsSet;
					inputsSet++;
				}
				//all nodes in the Module take the first Output
				//since nodes in modules can only be of type 0
				//and type 0 modules only have 1 output
				node.getInput().get(i)[1]=0;
			}
		}

		
		//update Nodes of Individual from point p2 onwards (exclusive of p2)
		//nodes before the module dont have to be updated because they never reference later nodes
		//outputs to save where each output gets its data from inside the module
		ArrayList<int[]> outputs = new ArrayList<>();
		ArrayList<NodeECGP> nodesOfIndiv = indiv.getNodes();
		//run through remaining nodes of individual
		//if input of one node is connected to a node inside the module:
		//add new output to module
		//update the node so that it references the new output
		int sizeOfModule= moduleNodes.size() -1;
		//sizeOfModule is 1 smaller than actual module size
		for(int i = p2+1; i<nodesOfIndiv.size();i++){
			NodeECGP currentNode = nodesOfIndiv.get(i);
			for(int j=0;j<currentNode.getInput().size();j++){
				//check all inputs of the node
				int addressOfReferencedNode = currentNode.getInput().get(j)[0];
				if(addressOfReferencedNode>=adressOfP1
					&&addressOfReferencedNode<=adressOfP2){
					//node refenrences node in module
					//output for output in module
					//output[j][0] = node adress that is referenced (substract p1 and inputAmount to get the relative Position)
					//	add inputAmount since the first adresses in module are taken by the input
					//output[j][1] = node output that is referenced (can remain (isn't changed by repositioning))
					// has to be 0 since module doesn't contain nodes with more outputs
					int[] output = {(addressOfReferencedNode-indiv.getInputAmount()-p1)+nrInputs
							,0};
					outputs.add(output);
					
					//adressOfP1 is position of node that has module as function
					currentNode.getInput().get(j)[0]= adressOfP1;
					//Number of the referenced Output
					currentNode.getInput().get(j)[1]=outputs.size()-1;
				}
				if(addressOfReferencedNode>adressOfP2){
					//if Node references Node after Module:
					//subtract moduleSize since:
					//nodes from p1+1 to p2 will be deleted from the nodeList
					currentNode.getInput().get(j)[0] -= sizeOfModule;
				}
			}
		}
		//also check if outputs of the Individual reference the module
		for(int i=0; i<indiv.getOutputAmount(); i++){
			int[] output = indiv.getOutput()[i];
			int addressOfReferencedNode = output[0];
			if(addressOfReferencedNode>=adressOfP1
					&&addressOfReferencedNode<=adressOfP2){
					int[] outputOfRefNode = 
						{(addressOfReferencedNode-indiv.getInputAmount()-p1)+nrInputs,0};
					outputs.add(outputOfRefNode);
					
					output[0]= adressOfP1;
					output[1]=outputs.size()-1;
				}
				if(addressOfReferencedNode>adressOfP2){
					output[0] -= sizeOfModule;
				}
		}
		
		//no node references created module
		if(outputs.size()==0){
			//add one random ouput to module
			//output[j][0] = node index that is referenced
			//output[j][1] = node output that is referenced (only 0)
			int randPosinModule = ThreadLocalRandom.current().nextInt(0,moduleNodes.size());
			//add nrINputs since module-output cannot adress module-input
			int[] output = {randPosinModule+nrInputs,0};
			outputs.add(output);
		}
		
		
		int[] header = {moduleList.getFirstUnusedIdentifier(), nrInputs};
		//moduleNodes contains nodes for modules
		//header and output Array complete
		
		Module module = new Module(header, outputs, moduleNodes);

		//delete nodes from Pos. p1+1 to p2
		for(int i=p1+1; i<=p2;i++){
			//only remove on pos p1+1
			//because all subsequent nodes are shifted to the left
			indiv.getNodes().remove(p1+1);
		}
		//and change node on Pos. p1 to call the Module
		NodeECGP node = indiv.getNodes().get(p1);
		//Function to call module and type = 1
		int[] function = {module.getIdentifier(),1};
		node.setFunction(function);
		//inputs now contain all oldInputs
		ArrayList<int[]> input = new ArrayList<>();
		for(int i=0; i<nrInputs;i++){
			int[] array = new int[2]; 
			array[0] = oldInputs.get(i)[0];
			array[1] = oldInputs.get(i)[1];
			input.add(array);
		}
		node.setInput(input);
		
		return module;
	}
	
	public static int[] copyArray(int[] old){
		int[] newArray = new int[old.length];
		for(int i=0;i<old.length;i++){
			newArray[i]=old[i];
		}
		return newArray;
	}
}
