package evaluation;

import java.util.ArrayList;

import cartesianNetwork.Individual;
import cartesianNetwork.NodeECGP;
import helperClasses.InputWithClassification;
/**
 * Class for calculating The Fitness of an Individual
 * @author Piepe
 *
 */
public class FitnessCalculator {
	/**
	 * calculates Fitness of an Individual
	 * smaller fitness is better
	 * Compares UsedNodes of Parents and Individal
	 * if they are the same - assign same fitness
	 * @param parents The Parents of the Individual
	 * @param indiv The Individual
	 * @param inputs The Inputs wich determine Fitness
	 * @return The Fitness Value of the Individual (no set in the Individual)
	 * @throws Exception if something unexpected happens
	 */
	public static int calculateFitness(ArrayList<Individual> parents, Individual indiv,
			ArrayList<InputWithClassification> inputs) throws Exception {
		
		if(parents==null){
			return calculateFitness(indiv, inputs);
		}
		OutputCalculator.calculateUsedNodes(indiv);
		for (Individual parent : parents) {
			if (sameUsedNodesAsParent(parent, indiv)) {
				return parent.getFitness();
			}
		}
		return calculateFitness(indiv, inputs);
	}

	/**
	 * compares the Nodes of an Individual and one Parent
	 * @param parent Individual with already assigned Fitness-Value
	 * @param indiv Individual with no assigned Fitness-Value
	 * @return if both Individuals contain the same Nodes
	 * @throws Exception if something unexpected happens
	 */
	private static boolean sameUsedNodesAsParent(Individual parent, Individual indiv) throws Exception {
		boolean[] parentNodes = parent.getUsedNodes();
		boolean[] indivNodes = indiv.getUsedNodes();
		try {
			for (int i = 0; i < parentNodes.length; i++) {
				if(parentNodes[i]!=indivNodes[i]){
					return false;
				}
				if(!sameNode(parent.getNodes().get(i),indiv.getNodes().get(i))){
					return false;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * compares two Nodes
	 * @param node1 the First Node
	 * @param node2 the Second Node
	 * @return true if the Nodes contain the same Genes
	 */
	private static boolean sameNode(NodeECGP node1, NodeECGP node2){
		//function Gene
		if(!sameArray(node1.getFunction(),node2.getFunction())){
			return false;
		}
		//inputGenes
		try{
			ArrayList<int[]> input1 = node1.getInput();
			ArrayList<int[]> input2 = node2.getInput();
			for(int i=0; i<input1.size();i++){
				if(!sameArray(input1.get(i),input2.get(i))){
					return false;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
		catch(IndexOutOfBoundsException e){
			return false;
		}
		return true;
	}
	
	/**
	 * compares two array
	 * @param a1 the fist array
	 * @param a2 the second array
	 * @return true if the arrays contain the same values
	 */
	private static boolean sameArray(int[] a1, int[] a2){
		if(a1.length != a2.length){
			return false;
		}
		try{
			for(int i=0; i< a1.length; i++){
				if(a1[i] != a2[i]){
					return false;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
		return true;
	}

	/**
	 * calculates the Fitness of an Individual 
	 * smaller fitness is better
	 * Without Comparing Used Nodes
	 * @param indiv The Individual that should be processed
	 * @param inputs The Inputs that determine fitness
	 * @return The Fitness Value of the Individual (no set in the Individual)
	 * @throws Exception if something unexpected happens
	 */
	public static int calculateFitness(Individual indiv, ArrayList<InputWithClassification> inputs) throws Exception {
		int sum = 0;
		// increment sum for each mistake in identification
		for (InputWithClassification input : inputs) {
			double[] output = OutputCalculator.output(indiv, input.getInput());
			// check if output == classification
			sum += ComputeOutput.computeOutputAndClassification(output, input.getClassification());
		}
		return sum;
	}
}
