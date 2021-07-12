package mutators;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.ModuleList;
import cartesianNetwork.NodeECGP;
import helperClasses.Functions;

/**
 * Class for Mutating an Individual
 * @author Björn Piepenbrink
 *
 */
public class Mutator {

	/**
	 * Mutates the given Individual
	 * @param offspring The Individual that should be mutated
	 * @param num_mutations The Number of point-Mutations that should be performed
	 * @param compress_probability The Probability for creating a Module
	 * @param modulePointMutation_probability The Probability for Module-Point-Mutation
	 * @param addOrRemoveInput_probability The Probability for Module-Input-Mutation
	 * @param addOrRemoveOutput_probability The Probability for Module-Output-Mutation
	 * @param maxAllowedModules The max number of allowed Modules in the ModuleList (-1 if infinite)
	 * @throws Exception if something unexpected happens
	 */
	public static void mutate(Individual offspring, int num_mutations, double compress_probability,
			double modulePointMutation_probability, double addOrRemoveInput_probability,
			double addOrRemoveOutput_probability, int maxAllowedModules) throws Exception {
		// Point-Mutation
		mutateOffspring(offspring, num_mutations);

		// should some nodes be compressed
		double randomPercent = ThreadLocalRandom.current().nextDouble(1);
		if (compress_probability > randomPercent) {
			Compressor.compress(offspring, offspring.getModuleList(), offspring.getMaxModuleSize(), maxAllowedModules);
		}

		// should some Modules be expanded
		randomPercent = ThreadLocalRandom.current().nextDouble(1);
		double expand_probability = compress_probability * 2;
		if (expand_probability > randomPercent) {
			Expander.expand(offspring, offspring.getModuleList());
		}

		// ModuleMutation
		ModuleMutator.moduleMutation(offspring, modulePointMutation_probability, addOrRemoveInput_probability,
				addOrRemoveOutput_probability);

		deleteUnusedModules(offspring);

		offspring.calculateGenotypeSize();
	}

	/**
	 * Mutates the Indivdual with Point-Mutation
	 * @param offspring The Individual that should be mutated
	 * @param num_mutations The number of Mutations to perform
	 * @throws Exception if something unexpected happens
	 */
	private static void mutateOffspring(Individual offspring, int num_mutations) throws Exception {

		// Point Mutation
		for (int i = 0; i < num_mutations; i++) {
			int geneToChange = ThreadLocalRandom.current().nextInt(0, offspring.getGenotypeSize());

			if (offspring.getGenotypeSize() - geneToChange < offspring.getOutputAmount()) {
				// OutputGeneHas To Change
				int outputToChange = offspring.getGenotypeSize() - geneToChange;
				changeOutputGeneOfNode(offspring, outputToChange);
			} else {
				// check if Node should be changed
				for (int n = 0; n < offspring.getNodes().size() && geneToChange >= 0; n++) {
					NodeECGP node = offspring.getNodes().get(n);

					int sizeOfNode = 1 + node.getInput().size();

					geneToChange -= sizeOfNode;

					if (geneToChange < 0) {
						// change Node
						int nodeGeneToChange = sizeOfNode + geneToChange;
						// functionGene
						if (nodeGeneToChange == 0) {
							// nodeGeneToChange
							changeFunctionGeneOfNode(offspring, node, n);
						} else {
							// input Genes
							// decrement nodeGeneToChange
							// so that range: 0-inputAmount of Node
							nodeGeneToChange--;
							changeInputGeneOfNode(offspring, n, nodeGeneToChange);
						}

					}
				}
			}
		}
	}

	/**
	 * mutates the given Funtion-Gene
	 * @param indiv The Individual the Gene belongs to
	 * @param node The Node the function-Gene belongs to
	 * @param positionOfNode The position of the Node in the genotype
	 * @throws Exception if something unexpected happens
	 */
	private static void changeFunctionGeneOfNode(Individual indiv, NodeECGP node, int positionOfNode) throws Exception {
		int functionSet = indiv.getFunctionSet();
		ModuleList moduleList = indiv.getModuleList();
		ArrayList<NodeECGP> nOdes = indiv.getNodes();
		int inputAmount = indiv.getInputAmount();
		if (node.getNodeType() == 1) {
			// Function Genes of Nodes with type 1 cannot be mutated
			return;
		}

		int nrOfOldOutputs = 1;
		int nrOfNewOutputs = 0;
		if (node.getNodeType() == 2) {
			Module refModule = indiv.getModuleList().getModuleWithIdentifier(node.getFunctionNr());
			nrOfOldOutputs = refModule.getNrOfOutputs();
		}

		// change function Gene
		int randomFunction = ThreadLocalRandom.current().nextInt(1,
				Functions.getNrFunctions(functionSet) + moduleList.getNrOfModules() + 1);
		if (randomFunction <= Functions.getNrFunctions(functionSet)) {
			// change Function to another type 0 Function
			if (node.getNodeType() == 0) {
				node.getFunction()[0] = randomFunction;
			} else {// node referenced a Module before
				node.getFunction()[0] = randomFunction;
				node.getFunction()[1] = 0;
				// shorten old Input to only 2 Inputs
				ArrayList<int[]> newInput = new ArrayList<>();
				newInput.add(node.getInput().get(0));
				newInput.add(node.getInput().get(1));
				node.setInput(newInput);
			}
			nrOfNewOutputs = 1;
		} else {
			// change Function to a type 2 Module Function
			Module randomModule = moduleList.getModuleList()
					.get(randomFunction - Functions.getNrFunctions(functionSet) - 1);
			node.getFunction()[0] = randomModule.getIdentifier();
			node.getFunction()[1] = 2;
			// change input of node
			ArrayList<int[]> newInput = new ArrayList<>();
			// copy old Input from Node
			for (int j = 0; j < node.getInput().size() && j < randomModule.getNrOfInputs(); j++) {
				int[] array = new int[2];
				array[0] = node.getInput().get(j)[0];
				array[1] = node.getInput().get(j)[1];
				newInput.add(array);
			}
			node.setInput(newInput);
			if (newInput.size() < randomModule.getNrOfInputs()) {
				// assign remaining input random
				for (int j = node.getInput().size(); j < randomModule.getNrOfInputs(); j++) {
					// generate random Input
					int randomNodeToGetInputFrom;
					if(indiv.getLevelsBack()<0){
						randomNodeToGetInputFrom = ThreadLocalRandom.current().nextInt(0, inputAmount + positionOfNode);
					}
					else{
						int currentlevelsBackMax = indiv.getLevelsBack();
						if (currentlevelsBackMax >= positionOfNode){
							currentlevelsBackMax = positionOfNode;
						}
						randomNodeToGetInputFrom = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + currentlevelsBackMax);
						if (randomNodeToGetInputFrom >= indiv.getInputAmount()) {
							randomNodeToGetInputFrom -= indiv.getInputAmount();
							randomNodeToGetInputFrom = indiv.getInputAmount() + positionOfNode - randomNodeToGetInputFrom - 1;
						}
					}
					int[] array = new int[2];
					array[0] = randomNodeToGetInputFrom;
					if (randomNodeToGetInputFrom < inputAmount) {
						array[1] = 0;
					} else {
						NodeECGP inputNode = nOdes.get(randomNodeToGetInputFrom - inputAmount);
						if (inputNode.getNodeType() == 0) {
							// input is simple
							array[1] = 0;
						} else {
							// input is a module
							Module refModule = moduleList.getModuleWithIdentifier(inputNode.getFunctionNr());
							array[1] = ThreadLocalRandom.current().nextInt(0, refModule.getNrOfOutputs());
						}
					}
					newInput.add(array);
				}
			}
			if (node.getInput().size() != randomModule.getNrOfInputs()) {
				throw new Exception("Darf net!");
			}
			node.setInput(newInput);
			nrOfNewOutputs = randomModule.getNrOfOutputs();
		}
		if (nrOfNewOutputs < nrOfOldOutputs) {
			// later nodes may reference Outputs
			// that are not present anymore!
			for (int i = positionOfNode + 1; i < indiv.getNodes().size(); i++) {
				// Check if node references current Node
				for (int[] input : indiv.getNodes().get(i).getInput()) {
					if (input[0] == positionOfNode + indiv.getInputAmount()) {
						// Node references current node!
						// check if it references output that doesn't exist
						// anymore
						if (input[1] >= nrOfNewOutputs) {
							// assign random other output of same node
							input[1] = ThreadLocalRandom.current().nextInt(0, nrOfNewOutputs);
						}
					}
				}
			}
			//check also if output referenced node
			for(int i=0; i<indiv.getOutputAmount();i++){
				int[][] output = indiv.getOutput();
				if (output[i][0] == positionOfNode + indiv.getInputAmount()) {
					// references current node!
					// check if it references output that doesn't exist
					// anymore
					if (output[i][1] >= nrOfNewOutputs) {
						// assign random other output of same node
						output[i][1] = ThreadLocalRandom.current().nextInt(0, nrOfNewOutputs);
					}
				}
			}
		}
	}

	/**
	 * mutates the given InputGene
	 * @param indiv The Individual the Gene belongs to
	 * @param positionOfNode The Position of the Node the Input-Gene belongs to
	 * @param inputToChange The Number of the input in the Node that should be mutated
	 */
	private static void changeInputGeneOfNode(Individual indiv, int positionOfNode, int inputToChange) {
		NodeECGP node = indiv.getNodes().get(positionOfNode);

		// generate random Input
		int randomAddress;
		if (indiv.getLevelsBack() < 0) {
			randomAddress = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + positionOfNode);
		} else {
			int currentlevelsBackMax = indiv.getLevelsBack();
			if (currentlevelsBackMax >= positionOfNode) {
				currentlevelsBackMax = positionOfNode;
			}
			randomAddress = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + currentlevelsBackMax);
			if (randomAddress >= indiv.getInputAmount()) {
				randomAddress -= indiv.getInputAmount();
				randomAddress = indiv.getInputAmount() + positionOfNode - randomAddress - 1;
			}
		}
		ArrayList<int[]> inputOfNode = node.getInput();

		if (randomAddress < indiv.getInputAmount()) {
			// referenced address is an input
			inputOfNode.get(inputToChange)[0] = randomAddress;
			inputOfNode.get(inputToChange)[1] = 0;
		} else if (indiv.getNodes().get(randomAddress - indiv.getInputAmount()).getNodeType() == 0) {
			// referenced node is a node of type 0
			// (only 1 output)
			inputOfNode.get(inputToChange)[0] = randomAddress;
			inputOfNode.get(inputToChange)[1] = 0;
		} else {
			// referenced Node has multiple outputs
			// (is a module)
			inputOfNode.get(inputToChange)[0] = randomAddress;
			// getNrOfOutputs
			NodeECGP refNode = indiv.getNodes().get(randomAddress - indiv.getInputAmount());
			Module referencedModule = indiv.getModuleList().getModuleWithIdentifier(refNode.getFunctionNr());
			int nrOfOuputs = referencedModule.getNrOfOutputs();
			// assign input to random output of module
			inputOfNode.get(inputToChange)[1] = ThreadLocalRandom.current().nextInt(0, nrOfOuputs);
		}
	}

	/**
	 * Mutates the given Output-Gene
	 * @param indiv The Individual that contains the Output-Gene
	 * @param outputGeneToChange The number of the OutputGene that should be mutated
	 */
	private static void changeOutputGeneOfNode(Individual indiv, int outputGeneToChange) {
		int[][] output = indiv.getOutput();
		int randOutput;
		if (indiv.getLevelsBack() < 0) {
			randOutput = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + indiv.getNodes().size());
		} else {
			int currentlevelsBackMax = indiv.getLevelsBack();
			if (currentlevelsBackMax >= indiv.getNodes().size()) {
				currentlevelsBackMax = indiv.getNodes().size();
			}
			randOutput = ThreadLocalRandom.current().nextInt(0, indiv.getInputAmount() + currentlevelsBackMax);
			if (randOutput >= indiv.getInputAmount()) {
				randOutput -= indiv.getInputAmount();
				randOutput = indiv.getInputAmount() + indiv.getNodes().size() - randOutput - 1;
			}
		}
		if (randOutput < indiv.getInputAmount()) {
			output[outputGeneToChange][0] = randOutput;
			output[outputGeneToChange][1] = 0;
		} else if (indiv.getNodes().get(randOutput - indiv.getInputAmount()).getNodeType() == 0) {
			output[outputGeneToChange][0] = randOutput;
			output[outputGeneToChange][1] = 0;
		} else {
			output[outputGeneToChange][0] = randOutput;
			Module refModule = indiv.getModuleList()
					.getModuleWithIdentifier(indiv.getNodes().get(randOutput - indiv.getInputAmount()).getFunctionNr());
			output[outputGeneToChange][1] = ThreadLocalRandom.current().nextInt(0, refModule.getNrOfOutputs());
		}
	}

	/**
	 * Deletes all unused Modules in the Individual
	 * @param indiv The Indiviudal that should be checked
	 */
	private static void deleteUnusedModules(Individual indiv) {
		// delete Modules only if they are used by no node

		// save UsageNr of Modules per Identifier
		ArrayList<int[]> identifiersAndUsage = new ArrayList<>();

		for (Module mod : indiv.getModuleList().getModuleList()) {
			int[] ar = new int[] { mod.getIdentifier(), 0 };
			identifiersAndUsage.add(ar);
		}
		for (int n = 0; n < indiv.getNodes().size(); n++) {
			NodeECGP node = indiv.getNodes().get(n);
			if (node.getNodeType() != 0) {
				// nodes with primtive functions are not important
				int identifier = node.getFunctionNr();
				// increase usage in list
				for (int[] usage : identifiersAndUsage) {
					if (usage[0] == identifier) {
						usage[1]++;
					}
				}

			}
		}
		for (int[] usage : identifiersAndUsage) {
			if (usage[1] == 0) {
				indiv.getModuleList().deleteModuleWithIdentifier(usage[0]);
			}
		}
	}

}
