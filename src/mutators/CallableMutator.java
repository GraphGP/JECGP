package mutators;

import java.util.concurrent.Callable;

import cartesianNetwork.Individual;

/**
 * Class for mutating an Individual with Threading
 * 
 * @author Björn Piepenbrink
 *
 */
public class CallableMutator implements Callable<Integer> {
	Individual offspring;
	int num_mutations;
	double compress_probability;
	double modulePointMutation_probability;
	double addOrRemoveInput_probability;
	double addOrRemoveOutput_probability;
	int maxAllowedModules;
	
	/**
	 * Set all the needed Parameters in the Mutator
	 * @param offspring the Individual that should be mutated
	 * @param num_mutations The number of point-Mutations to execute
	 * @param compress_probability The Probability for creating a module
	 * @param modulePointMutation_probability the probability for Moudle-Point-Mutation
 	 * @param addOrRemoveInput_probability Probability for adding an Input
	 * @param addOrRemoveOutput_probability Probability for adding an Output
	 * @param maxAllowedModules The max Moduellist-Size
	 */
	public CallableMutator(Individual offspring, int num_mutations, double compress_probability,
			double modulePointMutation_probability, double addOrRemoveInput_probability,
			double addOrRemoveOutput_probability, int maxAllowedModules) {

		this.offspring = offspring;
		this.num_mutations = num_mutations;
		this.compress_probability = compress_probability;
		this.modulePointMutation_probability = modulePointMutation_probability;
		this.addOrRemoveInput_probability = addOrRemoveInput_probability;
		this.addOrRemoveOutput_probability = addOrRemoveOutput_probability;
		this.maxAllowedModules = maxAllowedModules;
	}

	/**
	 * Mutates the Individual an return a 1
	 */
	public Integer call() throws Exception {
		Mutator.mutate(offspring, num_mutations, compress_probability, modulePointMutation_probability,
				addOrRemoveInput_probability, addOrRemoveOutput_probability, maxAllowedModules);
		return 1;
	}
}