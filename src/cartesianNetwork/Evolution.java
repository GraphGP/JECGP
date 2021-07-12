package cartesianNetwork;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import evaluation.Evaluation;
import helperClasses.InputWithClassification;
import mutators.CallableMutator;

/**
 * The Class that runs the (mu+lambda)-algorithm
 * @author Björn Piepenbrink
 *
 */
public class Evolution {

	static boolean useMutationThreading = false;

	boolean print;
	boolean differentCalc;
	int nodeAmount;
	int inputAmount;
	int outputAmount;
	int functionSet;
	int maxModuleSize;
	int maxAllowedModules;
	int generationLimit;
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
	 * @param print If Output to the console is allowed
	 * @param maxAllowedSecondsForMutation The maximum Time a Mutation is allowed to last (unrelevant if Mutation doesn't use Threading) - 
	 * 			Mutation-Threading is turned off by default and can be turned on in the Evolution-Class
	 * @param differentCalc If the Nodes of the Individual should be compared during Fitness-Evaluation (recommended to be turned off)
	 * @param inputs The List of Inputs with which the Fitness can be calculated
	 * @param nodeAmount The Amount of Nodes an Individual is initialized with
	 * @param outputAmount The Number of Outputs an Individual possesses
	 * @param functionSet The function-Set that should be used
	 * @param maxModuleSize The max Module Size a Module can reach
	 * @param maxAllowedModules The number of allowed Modules in the List (-1 if unlimited)
	 * @param generationLimit The Generation-Limit where the Calculation stops
	 * @param mutation_rate The Mutationss-Rate of the Individual
	 * @param levelsBack The levels-back-parameter (-1 if no levelsback should be used)
	 * @param compress_probability The Propability to create a Module
	 * @param modulePointMutation_probability The Probability to mutate a Module
	 * @param addInput_probability The Probability to add an Input
	 * @param addOutput_probability The Probability to add an Output
	 */
	public Evolution(boolean print, int maxAllowedSecondsForMutation, boolean differentCalc,
			ArrayList<InputWithClassification> inputs, int nodeAmount, int outputAmount, int functionSet,
			int maxModuleSize, int maxAllowedModules, int generationLimit, double mutation_rate, int levelsBack,
			double compress_probability, double modulePointMutation_probability, double addInput_probability,
			double addOutput_probability) {
		this.print = print;
		this.maxAllowedSecondsForMutation = maxAllowedSecondsForMutation;
		this.differentCalc = differentCalc;
		this.inputs = inputs;

		this.nodeAmount = nodeAmount;
		this.inputAmount = inputs.get(0).getInput().length;
		this.outputAmount = outputAmount;
		this.functionSet = functionSet;
		this.maxModuleSize = maxModuleSize;
		this.maxAllowedModules = maxAllowedModules;
		this.generationLimit = generationLimit;
		this.levelsBack = levelsBack;

		this.mutation_rate = mutation_rate;
		this.compress_probability = compress_probability;
		this.modulePointMutation_probability = modulePointMutation_probability;
		this.addInput_probability = addInput_probability;
		this.addOutput_probability = addOutput_probability;

		this.solutionFound = false;
	}
	
	/**
	 * runs the EvolutionAlgorithm of ECGP for the constructed Net
	 * @param mu Mu for the (mu+lambda)-Strategy
	 * @param lambda Lambda for the (mu+lambda)-Strategy
	 * @return The Best Mu Individuals of the run
	 * @throws Exception if something unexpected happens
	 */
	public ArrayList<Individual> EvolutionAlgorithm(int mu, int lambda) throws Exception {
		if (print)
			System.out.println("GENERATION: 0");
		ArrayList<Individual> individuals = getXRandomIndiv(mu + lambda);
		ArrayList<Individual> parents = getXFittestIndivs(mu, individuals, null);
		int gens = 1;
		while (!solutionFound && gens <= generationLimit) {
			if (print)
				System.out.println("GENERATION: " + gens);
			// ArrayList for Population
			individuals = new ArrayList<>();
			// Generate Lambda offsprings
			individuals = mutation(lambda, parents);
			// Population now contains parents + offsprings
			parents = getXFittestIndivs(mu, individuals, parents);
			// Only the fittest survive
			gens++;
		}
		return parents;
	}

	/**
	 * Generates x random Individuals
	 * @param x the number of Individuals to generate
	 * @return The generated Individuals
	 * @throws Exception if something unexpected happens
	 */
	private ArrayList<Individual> getXRandomIndiv(int x) throws Exception {
		ArrayList<Individual> individuals = new ArrayList<>();
		for (int i = 0; i < x; i++) {
			individuals.add(new Individual(nodeAmount, inputAmount, outputAmount, functionSet, new ModuleList(),
					maxModuleSize, levelsBack));
		}
		return individuals;
	}

	/**
	 * Calculates Fitness of the given Individuals
	 * To determine the best Individuals
	 * @param mu The number of fittest Individuals
	 * @param indivs The List where the Individuals should be taken from
	 * @param parents The Parents of the Generation
	 * @return The mu best Individuals of the given indiv-List
	 * @throws Exception if something unexpected happens
	 */
	private ArrayList<Individual> getXFittestIndivs(int mu, ArrayList<Individual> indivs, ArrayList<Individual> parents)
			throws Exception {
		return Evaluation.getXFittestIndividuals(this, mu, print, indivs, inputs, parents, differentCalc);
	}

	/**
	 * Use this if a perfect Individual was found
	 * so that the Evolution-Algorithm stops
	 */
	public void solutionFound() {
		this.solutionFound = true;
	}

	/**
	 * Generates Offspring of the given parents by mutation
	 * @param lambda The number of offsprings to generate
	 * @param parents The parents of the generation
	 * @return The new Generation (parents+ offspring)
	 * @throws Exception if something unexpected happens
	 */
	private ArrayList<Individual> mutation(int lambda, ArrayList<Individual> parents) throws Exception {
		// Generation
		ArrayList<Individual> individuals = new ArrayList<>();

		ArrayList<Individual> offsprings = new ArrayList<>();

		for (Individual parent : parents) {
			individuals.add(parent);
		}
		// generate lamba offsprings
		for (int i = 0; i < lambda; i++) {
			// get random parent
			int randomParent = ThreadLocalRandom.current().nextInt(0, parents.size());
			Individual parent = parents.get(randomParent);
			offsprings.add(parent.copy());
		}	
		
		// mutate these offsprings
		if (useMutationThreading) {
			mutate(offsprings);
		} else {
			mutateWithoutThreading(offsprings);
		}
		
		//add mutated offsprings to Generation
		for (Individual offspring : offsprings) {
			individuals.add(offspring);
		}
		return individuals;
	}

	/**
	 * Mutates the given Individuals
	 * Uses Threading (turned off by default)
	 * @param toMutate The Individuals to mutate
	 * @return The mutated Individuals
	 * @throws Exception if something unexpected happens
	 */
	private ArrayList<Individual> mutate(ArrayList<Individual> toMutate) throws Exception {

		// executor for handling Threads
		ExecutorService executor = Executors.newCachedThreadPool();

		for (Individual m : toMutate) {
			int num_mutations = new Double(mutation_rate * m.getGenotypeSize()).intValue();
			CallableMutator mutator = new CallableMutator(m, num_mutations, compress_probability,
					modulePointMutation_probability, addInput_probability, addOutput_probability, maxAllowedModules);

			executor.submit(mutator);
		}

		// close all threads (or else they dont stop)
		executor.shutdown();
		executor.awaitTermination(maxAllowedSecondsForMutation, TimeUnit.SECONDS);
		return toMutate;
	}

	/**
	 * Mutates the given Individuals
	 * Without Threading
	 * @param toMutate The Individuals to mutate
	 * @return The mutated Individuals
	 * @throws Exception if something unexpected happens
	 */
	public ArrayList<Individual> mutateWithoutThreading(ArrayList<Individual> toMutate) throws Exception {
		for (Individual m : toMutate) {
			int num_mutations = new Double(mutation_rate * m.getGenotypeSize()).intValue();
			CallableMutator mutator = new CallableMutator(m, num_mutations, compress_probability,
					modulePointMutation_probability, addInput_probability, addOutput_probability, maxAllowedModules);

			mutator.call();
		}
		return toMutate;
	}
}
