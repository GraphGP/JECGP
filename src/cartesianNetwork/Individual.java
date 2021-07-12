package cartesianNetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import helperClasses.Functions;

/**
 * An ECGP-Individual
 * consisting of Nodes and Outputs
 * 
 * @author Björn Piepenbrink
 * 
 * Adresses of the Individual as follows:
 * input Addresses: 
 * [0 , (inputAmount-1)] 
 * node Addresses:
 * [inputAmount,(inputAmount+nodes.size()-1) 
 * output Addresses:
 * [inputAmount+nodes.size(), inputAmount+nodes.size()+outputAmount-1]
 */
public class Individual implements Serializable {

	private static final long serialVersionUID = 1L;

	// size can vary!
	private ArrayList<NodeECGP> nodes;

	// number of Inputs
	private int inputAmount;
	// number of outputs
	private int outputAmount;
	// output Adresses of the CGP network
	// one address contains:
	// the adress of the input node [0]
	// & the nr of the output from the input node [1]
	private int[][] output;
	// size of the genotype
	private int genotypeSize;
	// maxAllowedModuleSize
	private int maxModuleSize;
	// used FunctionSet
	private int functionSet;
	// the used ModuleList
	private ModuleList moduleList;
	// connectivity-maximum
	private int levelsBack;
	// states if the fitness for the Individual has already been calculated
	private boolean hasFitness;
	// the fitness of the Individual
	private int fitness;
	// states if the used Nodes of the Individual have already been assigned
	private boolean usedNodesHasBeenCalculated;
	// the used Nodes for Output Calculation of the Individual
	private boolean[] usedNodes;

	/**
	 * This constructor creates a random Individual
	 * with the given Values
	 * 
	 * @param nodeAmount The initial Amount of Nodes
	 * @param inputAmount The Number of Inputs for an Individual
	 * @param outputAmount The Number of Outputs of the Individual
	 * @param functionSet The FunctionSet that shall be used
	 * @param moduleList The ModuleList the Individual should be initialized with
	 * @param maxModuleSize The max Module Size
	 * @param levelsBack The levels-back-Parameter
	 */
	public Individual(int nodeAmount, int inputAmount, int outputAmount, int functionSet, ModuleList moduleList,
			int maxModuleSize, int levelsBack) {
		this.inputAmount = inputAmount;
		this.functionSet = functionSet;
		this.outputAmount = outputAmount;
		this.moduleList = moduleList;
		this.maxModuleSize = maxModuleSize;
		this.levelsBack = levelsBack;

		nodes = new ArrayList<>();

		for (int i = 0; i < nodeAmount; i++) {
			int randomFunction = ThreadLocalRandom.current().nextInt(1, Functions.getNrFunctions(functionSet) + 1);
			// all Nodes are of type 0 since no Modules exist in the beginning
			int[] function = { randomFunction, 0 };
			// all Nodes contain just 2 inputs in the beginning
			ArrayList<int[]> input = new ArrayList<>();
			
			if (levelsBack <= 0) {
				// i = current position of Node
				// inputAmout + i = adress of current Node
				// upper Bound of ThreadLocalRandom.current().nextInt is
				// exclusive!
				int[] input1 = { (ThreadLocalRandom.current().nextInt(0, inputAmount + i)), 0 };
				int[] input2 = { (ThreadLocalRandom.current().nextInt(0, inputAmount + i)), 0 };
				input.add(input1);
				input.add(input2);
			} else {
				int currentlevelsBackMax = levelsBack;
				if (currentlevelsBackMax >= i)
					currentlevelsBackMax = i;
				for(int in = 0;in < 2 ; in++){
					int cInput = ThreadLocalRandom.current().nextInt(0, inputAmount + currentlevelsBackMax);
					if (cInput >= inputAmount) {
						// input now only contains the chosen levelsBack-value
						cInput -= inputAmount;
						// get the node-adress of the referenced node, relative to
						// the current one
						// position of the current node = inputAmount + i
						cInput = inputAmount + i - cInput - 1;
					}
					int[] actualInput = { cInput, 0};
					input.add(actualInput);
				}
			}
			
			
			nodes.add(new NodeECGP(function, input));
		}

		output = new int[outputAmount][2];
		for (int i = 0; i < outputAmount; i++) {
			/* OLD 
			output[i][0] = ThreadLocalRandom.current().nextInt(0, inputAmount + nodeAmount);
			// since in the beginning there are no modules
			output[i][1] = 0;
			*/
			
			if (levelsBack <= 0) {
				output[i][0] = ThreadLocalRandom.current().nextInt(0, inputAmount + nodeAmount);
				// since in the beginning there are no modules
				output[i][1] = 0;
			} else {
				int currentlevelsBackMax = levelsBack;
				if (currentlevelsBackMax >= nodeAmount)
					currentlevelsBackMax = nodeAmount;
				int cInput = ThreadLocalRandom.current().nextInt(0, inputAmount + currentlevelsBackMax);
				if (cInput >= inputAmount) {
					// input now only contains the chosen levelsBack-value
					cInput -= inputAmount;
					// get the node-adress of the referenced node, relative to
					// the current one
					// position of the current node = inputAmount + i
					cInput = inputAmount + nodeAmount - cInput - 1;
				}
				output[i][0] = cInput;
				// since in the beginning there are no modules
				output[i][1] = 0;
				
			}
		}

		calculateGenotypeSize();

		hasFitness = false;
		usedNodesHasBeenCalculated = false;
	}

	/**
	 * Creates an Individual with the given Values (not random)
	 * @param nodes The Nodes of the Individuall
	 * @param inputAmount The Number of Inputs
	 * @param outputAmount The Number of Outputs
	 * @param output The Output-Adresses
	 * @param funcionSet The function-set-identifier
	 * @param moduleList The Module-List
	 * @param genotypeSize The Size of the Genotype of the Individual
	 * @param maxModuleSize The max Module Size
	 * @param levelsBack The levels-back-Parameter
	 */
	public Individual(ArrayList<NodeECGP> nodes, int inputAmount, int outputAmount, int[][] output, int funcionSet,
			ModuleList moduleList, int genotypeSize, int maxModuleSize, int levelsBack) {
		this.nodes = nodes;
		this.inputAmount = inputAmount;
		this.outputAmount = outputAmount;
		this.moduleList = moduleList;
		this.output = output;
		this.functionSet = funcionSet;
		this.genotypeSize = genotypeSize;
		this.maxModuleSize = maxModuleSize;
		this.levelsBack = levelsBack;
		hasFitness = false;
		usedNodesHasBeenCalculated = false;
	}

	/**
	 * @return if Fitness has already been calculated
	 */
	public boolean hasFitness() {
		return hasFitness;
	}

	/**
	 * @param fitness
	 *            the Fitness of the Individual
	 * @throws Exception
	 *             if fitness has already been calculated
	 */
	public void setFitness(int fitness) throws Exception {
		if (!hasFitness) {
			this.fitness = fitness;
			hasFitness = true;
		} else {
			throw new Exception("tried to change already calculated fitness");
		}
	}

	/**
	 * use HasFitness() to see if Individual already has assigned FitnessValue
	 * 
	 * @return the Fitness of the Individual
	 * @throws Exception
	 *             if no fitness has been calculated for the Individual
	 */
	public int getFitness() throws Exception {
		if (hasFitness)
			return fitness;
		else
			throw new Exception("tried to access non-existing fitness");
	}

	/**
	 * @return if used Nodes have already been calculated
	 */
	public boolean usedNodesHasBeenCalculated() {
		return usedNodesHasBeenCalculated;
	}

	/**
	 * use usedNodesHasBeenCalculated() to see if Individual has assignes
	 * UsedNodes Value
	 * 
	 * @return the UsedNodes of the Individual
	 * @throws Exception
	 *             if usedNodes hasn't been calculated
	 */
	public boolean[] getUsedNodes() throws Exception {
		if (usedNodesHasBeenCalculated)
			return usedNodes;
		else
			throw new Exception("tried to access non-existing UsedNodes");
	}

	/**
	 * @param usedNodes
	 *            the usedNodes of the Individual
	 * @throws Exception
	 *             if usedNodes has already been assigned
	 */
	public void setUsedNodes(boolean[] usedNodes) throws Exception {
		if (!usedNodesHasBeenCalculated) {
			this.usedNodes = usedNodes;
			usedNodesHasBeenCalculated = true;
		} else
			throw new Exception("tried to change already calculated used Nodes of an Individual");
	}

	/**
	 * calculates the Genotype-Size for the Individual,
	 * by running through the whole Genotype
	 * (assigns the calculated value in the Individual)
	 */
	public void calculateGenotypeSize() {
		int size = 0;
		for (NodeECGP node : nodes) {
			// for the function gene
			size += 1;
			// for input genes
			size += node.getInput().size();
		}
		size += outputAmount;
		this.genotypeSize = size;
	}

	/**
	 * Creates a identical Copy of the Individual
	 * @return an identical Copy
	 */
	public Individual copy() {
		// copy nodes
		ArrayList<NodeECGP> nOdes = new ArrayList<>();
		for (NodeECGP node : nodes) {
			nOdes.add(node.copy());
		}
		// copy outputs
		int[][] outputChild = new int[outputAmount][2];
		for (int i = 0; i < outputAmount; i++) {
			for (int j = 0; j < output[0].length; j++) {
				outputChild[i][j] = output[i][j];
			}
		}
		ModuleList moduleListChild = moduleList.copy();

		// generate new offspring
		Individual offspring = new Individual(nOdes, inputAmount, outputAmount, outputChild, functionSet, moduleListChild,
				genotypeSize, maxModuleSize, levelsBack);

		return offspring;
	}

	public ArrayList<NodeECGP> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<NodeECGP> nodes) {
		this.nodes = nodes;
	}

	public int getInputAmount() {
		return inputAmount;
	}

	public int getOutputAmount() {
		return outputAmount;
	}

	public int[][] getOutput() {
		return output;
	}

	public void setOutput(int[][] output) {
		this.output = output;
	}

	public int getFunctionSet() {
		return functionSet;
	}

	public int getGenotypeSize() {
		return genotypeSize;
	}

	public void setGenotypeSize(int genotypeSize) {
		this.genotypeSize = genotypeSize;
	}

	public int getMaxModuleSize() {
		return maxModuleSize;
	}

	public ModuleList getModuleList() {
		return moduleList;
	}

	public int getLevelsBack() {
		return levelsBack;
	}

	public String toString() {
		String toReturn = "";
		toReturn += "InputAmount:" + inputAmount + "\n";
		for (NodeECGP node : nodes) {
			toReturn += node.toString();
		}
		for (int i = 0; i < outputAmount; i++) {
			toReturn += "Output" + i + ":" + output[i] + "\n";
		}
		toReturn += "HasFitness " + hasFitness + ": " + fitness + "\n";
		return toReturn;
	}

}