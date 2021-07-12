package evaluation;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import cartesianNetwork.Individual;
import helperClasses.InputWithClassification;

/**
 * Class for Calculating the Fitness of the Individual with Threading
 * 
 * @author Björn Piepenbrink
 *
 */
public class CallableCalculator implements Callable<Integer> {
	Individual indiv;
	ArrayList<Individual> parents;
	ArrayList<InputWithClassification> inputs;

	/**
	 * Smaller fitness is better calculates the fitness for the given Value 0 if
	 * Output is correct higher if it isn't (+1 for each misidentification) of
	 * the data set
	 * 
	 * @param indiv
	 *            the Individual for which the fitness shouls be calculated
	 * @param inputs
	 *            the InputData
	 * @param parents
	 *            The List of Parent of the Individual (null if you don't want
	 *            to assign individual with same UsedNodes- same Fitness)
	 */
	public CallableCalculator(ArrayList<Individual> parents, Individual indiv,
			ArrayList<InputWithClassification> inputs) {
		this.indiv = indiv;
		this.inputs = inputs;
		this.parents = parents;
	}

	/**
	 * calculates Fitness of the Individual should be used in Threads to speed
	 * up Evaluation
	 */
	public Integer call() throws Exception {
		if (parents == null) {
			return FitnessCalculator.calculateFitness(indiv, inputs);
		} else {
			return FitnessCalculator.calculateFitness(parents, indiv, inputs);
		}
	}
}
