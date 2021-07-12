package mutators;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.ModuleList;
import cartesianNetwork.NodeECGP;

/**
 * Class for expanding a module back into the genotype of the Individual
 * @author Björn Piepenbrink
 */
public class Expander {
	
	/**
	 * randomly selects a Type 1-Node in the Genotype and expands it
	 * @param indiv The Individual that should be mutated
	 * @param moduleList The ModuleList of the Individual
	 */
	public static void expand(Individual indiv,ModuleList moduleList){
		//randomly select one type 1 node
		ArrayList<NodeECGP> type1Nodes = new ArrayList<>();
		ArrayList<Integer> positions = new ArrayList<>();
		for(int i=0;i<indiv.getNodes().size();i++){
			if(indiv.getNodes().get(i).getNodeType()==1){
				type1Nodes.add(indiv.getNodes().get(i));
				positions.add(i);
			}
		}     
		if(type1Nodes.size()==0){
			//no type1 nodes
			return;
		}
		//select random type 1 node
		int randomNode = ThreadLocalRandom.current().nextInt(0,type1Nodes.size());
		//the actual node that should be changed
		NodeECGP nodeToChange = type1Nodes.get(randomNode);
		//the position of the node in the genotype of the individual
		int posOfNode = positions.get(randomNode);
		//the referenced module
		Module module = moduleList.getModuleWithIdentifier(nodeToChange.getFunctionNr());
		int moduleSize = module.getNrOfNodes();
		
		//copy nodes to original module stays untouched
		ArrayList<NodeECGP> nodesToExpand = new ArrayList<>();
		for(NodeECGP nodeInModule : module.getNodes()){
			nodesToExpand.add(nodeInModule.copy());
		}
		
		//update all the Inputs of the Nodes in the Module
		for(NodeECGP nodeToExpand : nodesToExpand){
			//for every node in the module
			for(int i=0;i<nodeToExpand.getInput().size();i++){
				//for every input of this node
				if(nodeToExpand.getInput().get(i)[0]< module.getNrOfInputs()){
					//Node gets its Input from outside the module
					
					//update outputs for the given index
					nodeToExpand.getInput().get(i)[1] =
							nodeToChange.getInput().get(nodeToExpand.getInput().get(i)[0])[1];

					//update index where the node gets its data
					nodeToExpand.getInput().get(i)[0] =
							nodeToChange.getInput().get(nodeToExpand.getInput().get(i)[0])[0];
					
				}
				else{
					//node gets its Input from inside the module
					
					//update adress where the node gets its data
					int newInputAdress = nodeToExpand.getInput().get(i)[0];
					//subtract inputAmount of module so the positon of the node is adressed
					newInputAdress -= module.getNrOfInputs();
					//add posOfNode+indiv.getInputAmount to get the position of the node when its reentered in the genotype
					newInputAdress += posOfNode+indiv.getInputAmount();
					
					nodeToExpand.getInput().get(i)[0] = newInputAdress;
					//outputs for the given index remains the same (0)
					//nodeInModule.getInput().get(i)[1] = 0;
				}
			}
		}
		//delete Node that references the Modules
		indiv.getNodes().remove(posOfNode);
		//add all Nodes of the Module in right Order into the Gentoype
		for(int i=0;i<nodesToExpand.size();i++){
			indiv.getNodes().add(posOfNode+i, nodesToExpand.get(i));
		}
		//update all inputs of later Nodes
		//and for nodes that referenced the Module change the input
		for(int i=posOfNode+nodesToExpand.size();i<indiv.getNodes().size();i++){
			ArrayList<int[]> inputOfCurrentNode = indiv.getNodes().get(i).getInput();
			//for every Node
			for(int j=0;j<inputOfCurrentNode.size();j++){
				//for every Input
				if(inputOfCurrentNode.get(j)[0]==posOfNode+indiv.getInputAmount()){
					//node referenced the module
					int refOutput = inputOfCurrentNode.get(j)[1];
					int[] output = module.getModuleOutputs().get(refOutput);
					inputOfCurrentNode.get(j)[0] = output[0]-module.getNrOfInputs()+posOfNode+indiv.getInputAmount();
					inputOfCurrentNode.get(j)[1] = output[1];
				}
				else if(inputOfCurrentNode.get(j)[0]>(posOfNode+indiv.getInputAmount())){
					//node references node after the module
					inputOfCurrentNode.get(j)[0] += moduleSize-1;
				}
				else{
					//node references node before the module
				}
			}
		}
		//update outputs of Individual
		for(int i=0; i<indiv.getOutputAmount();i++){
			int[] inputOfOutput = indiv.getOutput()[i];
			if(inputOfOutput[0]==posOfNode+indiv.getInputAmount()){
				//referenced the module
				int refOutput = inputOfOutput[1];
				int[] output = module.getModuleOutputs().get(refOutput);
				inputOfOutput[0] = output[0]-module.getNrOfInputs()+posOfNode+indiv.getInputAmount();
				inputOfOutput[1] = output[1];
			}
			else if(inputOfOutput[0]>(posOfNode+indiv.getInputAmount())){
				//node references node after the module
				inputOfOutput[0] += moduleSize-1;
			}
			else{
				//node references node before the module
			}
		}
	}
}
