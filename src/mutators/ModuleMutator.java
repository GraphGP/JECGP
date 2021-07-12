package mutators;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.NodeECGP;
import helperClasses.Functions;

/**
 * Class for mutating a module
 * with Module-Point-Mutation, Adding/removing In-/Outputs
 * @author Björn Piepenbrink
 *
 */
public class ModuleMutator {

	/**
	 * Mutates every Module in the ModuleList of the Individual
	 * @param indiv The Individual that contains the ModuleList
	 * @param modulePointMutation_probability The Probability to mutate a Module
	 * @param addOrRemoveInput_probability The Probability to add/remove an Input
	 * @param addOrRemoveOutput_probability The Probability to add/remove an Output
	 */
	public static void moduleMutation(Individual indiv,double modulePointMutation_probability, double addOrRemoveInput_probability, double addOrRemoveOutput_probability){
		for(Module mod : indiv.getModuleList().getModuleList()){
			mutate(mod,indiv, modulePointMutation_probability, addOrRemoveInput_probability, addOrRemoveOutput_probability);
		}
	}
	
	/**
	 * Mutates the given Module
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 * @param modulePointMutation_probability The Probability to mutate a Module
	 * @param addOrRemoveInput_probability The Probability to add/remove an Input
	 * @param addOrRemoveOutput_probability The Probability to add/remove an Output
	 */
	private static void mutate(Module module, Individual indiv,double modulePointMutation_probability, double addOrRemoveInput_probability, double addOrRemoveOutput_probability){
		//remove has twice the probability
		double addInput_probability = addOrRemoveInput_probability;
		double removeInput_probability = addInput_probability*2;
		
		double addOutput_probability = addOrRemoveOutput_probability;
		double removeOutput_probability = addOutput_probability*2;

		
		double randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if(modulePointMutation_probability>randomPercent){
			pointMutation(module,indiv);
			module.resetUsedNodesHasBeenCalculated();
		}
		randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if(addInput_probability>randomPercent){
			addInput(module,indiv);
			module.resetUsedNodesHasBeenCalculated();
		}
		randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if(removeInput_probability>randomPercent){
			removeInput(module,indiv);
			module.resetUsedNodesHasBeenCalculated();
		}
		randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if(addOutput_probability>randomPercent){
			addOutput(module,indiv);
			module.resetUsedNodesHasBeenCalculated();
		}
		randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if(removeOutput_probability>randomPercent){
			removeOutput(module,indiv);
			module.resetUsedNodesHasBeenCalculated();
		}
	}
	
	/**
	 * Mutates the Module with one point Mutation
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 */
	private static void pointMutation(Module module,Individual indiv){
		ArrayList<NodeECGP> nodes = module.getNodes();
		//can mutate:
		//function genes to another primtive function
		//input genes of nodes in the module
		//module outputs to antoher valid node output
		int randPos = ThreadLocalRandom.current().nextInt(0,(module.getNrOfNodes()*3)+module.getNrOfOutputs());
		if(randPos<module.getNrOfNodes()*3){
			//change node
			if(randPos%3==0){
				//change function node
				nodes.get(randPos/3).getFunction()[0] = ThreadLocalRandom.current().nextInt(1,(Functions.getNrFunctions(indiv.getFunctionSet())+1));
			}
			else{
				
				int randomAddress;
				if (indiv.getLevelsBack() < 0) {
					randomAddress = ThreadLocalRandom.current().nextInt(0,(randPos/3)+module.getNrOfInputs());
				} else {
					int currentlevelsBackMax = indiv.getLevelsBack();
					if (currentlevelsBackMax >= randPos/3) {
						currentlevelsBackMax = randPos/3;
					}
					randomAddress = ThreadLocalRandom.current().nextInt(0, module.getNrOfInputs() + currentlevelsBackMax);
					if (randomAddress >= module.getNrOfInputs()) {
						randomAddress -= module.getNrOfInputs();
						randomAddress = module.getNrOfInputs() + randPos/3 - randomAddress - 1;
					}
				}
				
				//change input node
				int input = randPos%3;
				nodes.get(randPos/3).getInput().get(input-1)[0] =  randomAddress;
				//since all nodes in module are of type 0 or just inputs
				nodes.get(randPos/3).getInput().get(input-1)[1] = 0;
			}
		}
		else{
			//change output
			//Output is not allowed to be connected to input!
			int output = randPos-(module.getNrOfNodes()*3);
			int randomRefNode;
			if (indiv.getLevelsBack() < 0) {
				randomRefNode = ThreadLocalRandom.current().nextInt(0,module.getNrOfNodes()) + module.getNrOfInputs();
			} else {
				int currentlevelsBackMax = indiv.getLevelsBack();
				if (currentlevelsBackMax >= module.getNrOfNodes()) {
					currentlevelsBackMax = module.getNrOfNodes();
				}
				randomRefNode =  ThreadLocalRandom.current().nextInt(0,currentlevelsBackMax);
				randomRefNode = module.getNrOfInputs() + module.getNrOfNodes() - randomRefNode - 1;
			}
			module.getModuleOutputs().get(output)[0] = randomRefNode;
			//since all nodes in module are of type 0
			module.getModuleOutputs().get(output)[1] = 0;
		}
	}
	
	/**
	 * Adds an Input to the Module
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 */
	private static void addInput(Module module,Individual indiv){
		
		//add an extra input to module
		//new input takes last input adress +1 in module
		
		if(module.getNrOfInputs()>=module.getNrOfNodes()*2){
			//max number of nodes reached
			//cannot mutate further
			return;
		}
		//increment all adresses of Nodes in the Module
		//that reference nodes after the input
		for(NodeECGP node : module.getNodes()){
			for(int i=0;i<node.getInput().size();i++){
				if(node.getInput().get(i)[0]>=module.getNrOfInputs()){
					node.getInput().get(i)[0] += 1;
				}
			}
		}
		//increment all adresses of Outputs
		//since no output is allowed to adress an input
		for(int[] output : module.getModuleOutputs()){
			output[0]+=1;
		}
		//increment size of Inputs in Header
		module.getHeader()[1] += 1;
		
		//update all nodes in the indivdual that reference the altered node
		//add a random Input to all of them
		for(int n=0; n<indiv.getNodes().size();n++){
			NodeECGP nodeIndiv = indiv.getNodes().get(n);
			if(nodeIndiv.getFunction()[0]==module.getIdentifier()){
				//references the given Module
				//add random Input
				int randomInputPos;
				if (indiv.getLevelsBack() < 0) {
					randomInputPos = ThreadLocalRandom.current().nextInt(0,indiv.getInputAmount()+n);
				} else {
					int currentlevelsBackMax = indiv.getLevelsBack();
					if (currentlevelsBackMax >= n) {
						currentlevelsBackMax = n;
					}
					randomInputPos = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + currentlevelsBackMax);
					if (randomInputPos >= indiv.getInputAmount()) {
						randomInputPos -= indiv.getInputAmount();
						randomInputPos = indiv.getInputAmount() + n - randomInputPos - 1;
					}
				}
				
				int[] newInput = new int[2]; 
				newInput[0] = randomInputPos;
				if(randomInputPos<indiv.getInputAmount()){
					newInput[1] = 0; 
				}
				else{
					NodeECGP refNode = indiv.getNodes().get(randomInputPos-indiv.getInputAmount());
					if(refNode.getNodeType()==0){
						newInput[1] = 0;
					}
					else{
						Module refModule = indiv.getModuleList().getModuleWithIdentifier(refNode.getFunctionNr());
						int randomOutputOfInputPos = ThreadLocalRandom.current().nextInt(0,refModule.getNrOfOutputs());
						newInput[1] = randomOutputOfInputPos; 
					}
				}
				nodeIndiv.getInput().add(newInput);
			}
		}
	}
	
	/**
	 * removes an Input of the Module
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 */
	private static void removeInput(Module module,Individual indiv){
		if(module.getNrOfInputs()<=2){
			return;
		}
		int inputToRemove = ThreadLocalRandom.current().nextInt(0,module.getNrOfInputs());
		//decrement header
		module.getHeader()[1] -= 1;
		//for every node inside the module
		//reduce address of input by 1
		//if it adresses the removed input or later nodes
		for(NodeECGP node : module.getNodes()){
			for(int[] input : node.getInput()){
				if(input[0]>=inputToRemove){
					if(input[0]!=0){
						input[0] -= 1;
					}
				}
			}
		}
		//decrement all adresses of Outputs
		//since no output is allowed to adress an input
		for(int[] output : module.getModuleOutputs()){
			output[0]-=1;
		}
		//for every node in the indiv that refeerences the module:
		//delete the input that should be deleted
		for(NodeECGP nodeI : indiv.getNodes()){
			if(nodeI.getNodeType()!=0){
				if(module.getIdentifier() == nodeI.getFunction()[0]){
					nodeI.getInput().remove(inputToRemove);
				}
			}
		}
	}
	
	/**
	 * Adds an Output to the Module
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 */
	private static void addOutput(Module module,Individual indiv){
		if(module.getNrOfOutputs()>=module.getNrOfNodes()){
			//max number of outputs reached
			return;
		}
		int[] newOutput = new int[2];
		
		int randomRefNode;
		if (indiv.getLevelsBack() < 0) {
			randomRefNode = ThreadLocalRandom.current().nextInt(0,module.getNrOfNodes()) + module.getNrOfInputs();
		} else {
			int currentlevelsBackMax = indiv.getLevelsBack();
			if (currentlevelsBackMax >= module.getNrOfNodes()) {
				currentlevelsBackMax = module.getNrOfNodes();
			}
			randomRefNode =  ThreadLocalRandom.current().nextInt(0,currentlevelsBackMax);
			randomRefNode = module.getNrOfInputs() + module.getNrOfNodes() - randomRefNode - 1;
		}
		
		newOutput[0] = randomRefNode;
		newOutput[1] = 0;
		module.getModuleOutputs().add(newOutput);
	}
	
	/**
	 * removes an Output of the Module
	 * @param module The Module that should be mutated
	 * @param indiv The Individual that uses the Module
	 */
	private static void removeOutput(Module module,Individual indiv){
		if(module.getNrOfOutputs()<=1){
			return;
		}
		int outputToRemove = ThreadLocalRandom.current().nextInt(0,module.getNrOfOutputs());
		module.getModuleOutputs().remove(outputToRemove);
		//for every node int the indiv that referenced the output of the module:
		//decrement the input if it references the choosen output or later outputs
		for(NodeECGP nodeI : indiv.getNodes()){
			for(int[] input : nodeI.getInput()){
				//check if input references a node that uses the given module
				int adressOfInput = input[0]-indiv.getInputAmount();
				if(adressOfInput>=0){
					//node references another node
					NodeECGP refNode = indiv.getNodes().get(adressOfInput);
					if(refNode.getNodeType()!=0){
						//referenced Node uses a module
						if(refNode.getFunctionNr()==module.getIdentifier()){
							//module is referenced by the referenced node
							if(input[1]>=outputToRemove){
								//decrement the input so that the deleted output is not referenced
								if(input[1]>0){
									input[1] -= 1;
								}
							}
						}
					}
				}
			}
		}
		//check also if output referenced node
		for(int i=0; i<indiv.getOutputAmount();i++){
			int[][] output = indiv.getOutput();
			int adressOfInput = output[i][0]-indiv.getInputAmount();
			if(adressOfInput>=0){
				//node references another node
				NodeECGP refNode = indiv.getNodes().get(adressOfInput);
				if(refNode.getNodeType()!=0){
					//referenced Node uses a module
					if(refNode.getFunctionNr()==module.getIdentifier()){
						//module is referenced by the referenced node
						if(output[i][1]>=outputToRemove){
							//decrement the input so that the deleted output is not referenced
							if(output[i][1]>0){
								output[i][1] -= 1;
							}
						}
					}
				}
			}
		}
	}
}
