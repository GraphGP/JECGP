package evaluation;

import java.util.ArrayList;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.NodeECGP;
import helperClasses.Functions;

/**
 * Class for calculating Output of the Individual
 * @author Björn Piepenbrink
 *
 */
public class OutputCalculator {

	/**
	 * calculates Ouput for the given Individual
	 * 
	 * @param indiv
	 *            The Individual
	 * @param input
	 *            The Input
	 * @return The Output for the given Input
	 * @throws Exception if input-length is not expected or something else
	 */
	public static double[] output(Individual indiv, int[] input) throws Exception {
		if (indiv.getInputAmount() != input.length) {
			throw new Exception("gegebene InputLänge entspricht nicht der angegebenen InputLänge des Individuums");
		}
		if (!indiv.usedNodesHasBeenCalculated()) {
			calculateUsedNodes(indiv);
		}

		// go through all nodes
		// calculate output for all usedNodes
		boolean[] usedNodes = indiv.getUsedNodes();

		// save all calculated Outputs
		double[][] outputs = new double[indiv.getNodes().size()][];

		// go through all nodes
		for (int i = 0; i < usedNodes.length; i++) {
			if (usedNodes[i]) {
				// save every input of the Node
				NodeECGP node = indiv.getNodes().get(i);
				ArrayList<Double> inputsOfNode = new ArrayList<>();

				for (int[] inputOfNode : node.getInput()) {
					//for every input of the node
					if (inputOfNode[0] < indiv.getInputAmount()) {
						// Node uses input as input
						inputsOfNode.add((double)input[inputOfNode[0]]);
					} else {
						// node uses another Node as input
						int addressOfInput = inputOfNode[0] - indiv.getInputAmount();
						if(outputs[addressOfInput] == null){
							throw new IllegalStateException("No Output for a Node has been calculated "
									+ "but one node uses antoher as input:"
									+ "Node "+i+""
									+ " uses the node:"+(inputOfNode[0] -indiv.getInputAmount()));
						}
						// get Output of the referenced Node
						double[] outputOfReferencedNode = outputs[addressOfInput];
						// get Output nr input[1] an add it to the list
						try{
						inputsOfNode.add(outputOfReferencedNode[inputOfNode[1]]);
						}
						catch(ArrayIndexOutOfBoundsException e){
							String er = "";
							er += ("Node "+i);
							er += ("RefNode "+addressOfInput);
							er += ("Input "+inputOfNode[1]);
							er += ("NodeType" + node.getNodeType());
							er += ("RefNodeType" + indiv.getNodes().get(addressOfInput).getNodeType());
							er += ("\n Function/Module:" + indiv.getNodes().get(addressOfInput).getFunctionNr());
							if(indiv.getNodes().get(addressOfInput).getNodeType()!=0){
								er += ("\n ModuleOutputs:" + indiv.getModuleList().getModuleWithIdentifier(indiv.getNodes().get(addressOfInput).getFunctionNr()).getNrOfOutputs());
							}
							e.printStackTrace();
							throw new Exception(er);
						}
					}
				}
				// every input has been catched
				// calculate Output
				double[] outputsOfNode;
				if (node.getNodeType() == 0) {
					// primitive function
					outputsOfNode = new double[1];
					outputsOfNode[0] = Functions.getResultForFunction(indiv.getFunctionSet(), inputsOfNode.get(0),
							inputsOfNode.get(1), node.getFunctionNr());
				} else {
					// node uses a module as function
					Module refModule = indiv.getModuleList().getModuleWithIdentifier(node.getFunctionNr());
					outputsOfNode = ModuleOutputCalculator.getOutputsForModule(refModule, inputsOfNode, indiv);
				}

				// save it in outputs
				outputs[i] = outputsOfNode;
			}
		}

		// Output for all Nodes has been calculated
		// Now get all Output-Values
		double[] outputsOfIndividual = new double[indiv.getOutputAmount()];
		for (int i = 0; i < indiv.getOutputAmount(); i++) {
			int indexOfRefNode = indiv.getOutput()[i][0];
			if (indexOfRefNode < indiv.getInputAmount()) {
				outputsOfIndividual[i] = input[indexOfRefNode];
			} else {
				// Output Node references another Node
				double[] output = outputs[indexOfRefNode - indiv.getInputAmount()];
				outputsOfIndividual[i] = output[indiv.getOutput()[i][1]]; //IndexOutOfBoundsException mit 1 oder mehr 
				//weil output denkt er referenziert noch einen Knoten mit mehr Outputs
			}
		}
		return outputsOfIndividual;
	}

	/**
	 * Calculates tha used Nodes of an Individual recursively
	 * @param indiv The Individual that the nodes should be calculated for
	 * @throws Exception if somethiing unexpected happens
	 */
	public static void calculateUsedNodes(Individual indiv) throws Exception {
		if (indiv.usedNodesHasBeenCalculated())
			return;

		boolean[] usedNodes = new boolean[indiv.getNodes().size()];

		// find nodes that are referenced by the output
		for (int i = 0; i < indiv.getOutputAmount(); i++) {
			int inputOfOutput = indiv.getOutput()[i][0];
			if (inputOfOutput >= indiv.getInputAmount()) {
				// else its an input node
				usedNodes[inputOfOutput - indiv.getInputAmount()] = true;
			}
		}

		// check all other Nodes
		for (int i = usedNodes.length - 1; i >= 0; i--) {
			if (usedNodes[i] == true) {
				NodeECGP node = indiv.getNodes().get(i);
				// mark all nodes this node uses as used
				for (int[] inputOfNode : node.getInput()) {
					if (inputOfNode[0] >= indiv.getInputAmount()) {
						// else: it is an input Node
						usedNodes[inputOfNode[0] - indiv.getInputAmount()] = true;
					}
				}
			}
		}

		// set UsedNodes in the Individual

		indiv.setUsedNodes(usedNodes);
	}
}
