package a_main;

import java.util.ArrayList;
import cartesianNetwork.Evolution;
import cartesianNetwork.Individual;
import helperClasses.InputWithClassification;
import savingAndInformation.SaveAndLoadIndividuals;
import savingAndInformation.StatisticsCalculator;

/**
 * Class for running the ECGP-Network
 * @author Björn Piepenbrink
 */
public class ECGP_NetHandler {

	String path;
	boolean print;
	boolean diffCalc;
	int nodeAmount;
	int inputAmount;
	int outputAmount;
	int functionSet;
	int maxModuleSize;
	int maxAllowedModules;
	int generationLimit;
	int mu;
	int lambda;
	int levelsBack;

	double mutation_rate;
	double compress_probability;
	double modulePointMutation_probability;
	double addInput_probability;
	double addOutput_probability;

	int maxAllowedSecondsForMutation;
	boolean solutionFound;

	ArrayList<InputWithClassification> inputs;

	/**
	 * The Constructor with all needed Attributes
	 * @param path The Path, where the best Individuals of each run should be saved
	 * @param print If Output to the console is allowed
	 * @param maxAllowedSecondsForMutation The maximum Time a Mutation is allowed to last (unrelevant if Mutation doesn't use Threading) - 
	 * 			Mutation-Threading is turned off by default and can be turned on in the Evolution-Class
	 * @param diffCalc If the Nodes of the Individual should be compared during Fitness-Evaluation (recommended to be turned off)
	 * @param inputs The List of Inputs with which the Fitness can be calculated
	 * @param nodeAmount The Amount of Nodes an Individual is initialized with
	 * @param outputAmount The Number of Outputs an Individual possesses
	 * @param functionSet The function-Set that should be used
	 * @param maxModuleSize The max Module Size a Module can reach
	 * @param maxAllowedModules The number of allowed Modules in the List (-1 if unlimited)
	 * @param generationLimit The Generation-Limit where the Calculation stops
	 * @param mutation_rate The Mutationss-Rate of the Individual
	 * @param levelsBack The levels-back-parameter (-1 if no levelsback should be used)
	 * @param mu Mu for the (mu+lambda)-Strategy
	 * @param lambda Lambda for the (mu+lambda)-Strategy
	 * @param compress_probability The Propability to create a Module
	 * @param modulePointMutation_probability The Probability to mutate a Module
	 * @param addInput_probability The Probability to add an Input
	 * @param addOutput_probability The Probability to add an Output
	 */
	public ECGP_NetHandler(String path, boolean print,int maxAllowedSecondsForMutation,boolean diffCalc, ArrayList<InputWithClassification> inputs, int nodeAmount,
			int outputAmount, int functionSet, int maxModuleSize,int maxAllowedModules, int generationLimit, double mutation_rate,int levelsBack, int mu,
			int lambda, double compress_probability, double modulePointMutation_probability,
			double addInput_probability, double addOutput_probability) {
		this.path = path;
		this.print = print;
		this.maxAllowedSecondsForMutation = maxAllowedSecondsForMutation;
		this.diffCalc = diffCalc;
		this.inputs = inputs;

		this.nodeAmount = nodeAmount;
		this.inputAmount = inputs.get(0).getInput().length;
		this.outputAmount = outputAmount;
		this.functionSet = functionSet;
		this.maxModuleSize = maxModuleSize;
		this.maxAllowedModules = maxAllowedModules;
		this.generationLimit = generationLimit;
		this.levelsBack = levelsBack;
		
		this.mu = mu;
		this.lambda = lambda;

		this.mutation_rate = mutation_rate;
		this.compress_probability = compress_probability;
		this.modulePointMutation_probability = modulePointMutation_probability;
		this.addInput_probability = addInput_probability;
		this.addOutput_probability = addOutput_probability;

		this.solutionFound = false;
	}

	/**
	 * Runs an ECGP-Net with the given values
	 * @param tries How many times the Net should be run
	 * @return The best Individual of all runs
	 * @throws Exception if something unexpected happens
	 */
	public Individual runECGP(int tries) throws Exception {
		ArrayList<Individual> bestIndividuals = getXBestIndividuals(tries);
		int bestFitness = bestIndividuals.get(0).getFitness();
		Individual bestIndividual = bestIndividuals.get(0);
		for (Individual indiv : bestIndividuals) {
			// lower Fitness means better Fitness
			if (bestFitness > indiv.getFitness()) {
				bestFitness = indiv.getFitness();
				bestIndividual = indiv;
			}
		}
		return bestIndividual;
	}
	
	/**
	 * Runs an ECGP-Net with the given values
	 * @param tries How many times the Net should be run
	 * @return The best Individuals of each try
	 * @throws Exception if something unexpected happens
	 */
	public ArrayList<Individual> getXBestIndividuals(int tries) throws Exception {
		ArrayList<Individual> bestIndividuals = new ArrayList<>();
		for (int i = 0; i < tries; i++) {
			ArrayList<Individual> bestIndividualsCurrent = getBestIndividual(i);
			for (Individual e : bestIndividualsCurrent) {
				bestIndividuals.add(e);
			}
		}

		if (print) {
			String toPrint = "Fitness of the best Individuals:\n";
			ArrayList<Integer> fitness = new ArrayList<>();
			for (Individual ind : bestIndividuals) {
				fitness.add(ind.getFitness());

				// sum over used Nodes
				boolean[] uN = ind.getUsedNodes();
				int sum = 0;
				for (int i = 0; i < uN.length; i++) {
					if (uN[i]) {
						sum++;
					}
				}
				toPrint += ind.getFitness() + "     UsedNodes:" + sum + "\n";
			}

			toPrint += StatisticsCalculator.calculateOutput(fitness);

			System.out.println(toPrint);
			String fileName = "fs_" + functionSet + "_mr_" + mutation_rate + "_nodes_" + nodeAmount + "_levelsBack_" + levelsBack
					+ "_Gens_" + generationLimit + "_outputs_" + outputAmount;
			SaveAndLoadIndividuals.saveStatistics(toPrint, path, fileName);
		}

		return bestIndividuals;
	}

	/**
	 * Runs the ECGP-Net
	 * @param i Number of the Run
	 * @return The best Individuals of the Run
	 * @throws Exception if something unexpected happens
	 */
	private ArrayList<Individual> getBestIndividual(int i) throws Exception {
		Evolution evolution = new Evolution(print, maxAllowedSecondsForMutation,diffCalc, inputs, nodeAmount, outputAmount, functionSet, maxModuleSize,maxAllowedModules,
				generationLimit, mutation_rate,levelsBack, compress_probability, modulePointMutation_probability,
				addInput_probability, addOutput_probability);
		ArrayList<Individual> bestIndivs = evolution.EvolutionAlgorithm(mu, lambda);
		SaveAndLoadIndividuals.saveIndividual(bestIndivs, path, "BestIndividual_" + i);
		return bestIndivs;
	}
}
