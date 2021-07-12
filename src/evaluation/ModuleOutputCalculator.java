package evaluation;

import java.util.ArrayList;

import cartesianNetwork.Individual;
import cartesianNetwork.Module;
import cartesianNetwork.NodeECGP;
import helperClasses.Functions;

/**
 * Class for calculating the Output of a Module
 * @author Björn Piepenbrink
 *
 */
public class ModuleOutputCalculator {
	/**
	 * Calculates Module-Output for given Input
	 * also calculates UsedNodes of Module if needed
	 * @param module The Module
	 * @param inputs The given Inputs for the Module
	 * @param indiv The Individual that contains the Module
	 * @return The Output-Array of the Module
	 */
	public static double[] getOutputsForModule(Module module, ArrayList<Double> inputs, Individual indiv) {
		boolean[] usedNodes;
		if (module.usedNodesHasBeenCalculated()) {
			usedNodes = module.getUsedNodes();
		} else {
			usedNodes = getUsedNodesOfModule(module);
		}

		// save all calculated Outputs
		double[][] outputs = new double[module.getNrOfNodes()][];

		// calculate Output for each used Node
		for (int i = 0; i < module.getNrOfNodes(); i++) {
			if (usedNodes[i]) {
				NodeECGP node = module.getNodes().get(i);
				ArrayList<Double> inputsOfNode = new ArrayList<>();

				for (int[] inputOfNode : node.getInput()) {
					if (inputOfNode[0] < module.getNrOfInputs()) {
						// Node uses input as input
						inputsOfNode.add(inputs.get(inputOfNode[0]));
					} else {
						// node uses another Node as input
						int addressOfInput = inputOfNode[0] - module.getNrOfInputs();
						// get Output of the referenced Node
						double[] outputOfReferencedNode = outputs[addressOfInput];
						// get Output nr input[1] an add it to the list
						inputsOfNode.add(outputOfReferencedNode[inputOfNode[1]]);
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
					throw new IllegalStateException(
							"Node in Module has a node of type != 0 inside" + " >ModuleOutputCalculator<");
				}

				// save it in outputs and set the pointer in outputPointer
				outputs[i] = outputsOfNode;

			}
		}
		// Output for all Nodes has been calculated
		// Now get all Output-Values
		double[] outputsOfModule = new double[module.getNrOfOutputs()];
		for (int i = 0; i < module.getNrOfOutputs(); i++) {
			int indexOfRefNode = module.getModuleOutputs().get(i)[0];
			if (indexOfRefNode < module.getNrOfInputs()) {
				throw new IllegalStateException("Module Outputs are not allowed to use inputs!");
				//outputsOfModule[i] = inputs.get(indexOfRefNode);
			} else {
				// Output Node references another Node
				outputsOfModule[i] = outputs[indexOfRefNode-module.getNrOfInputs()][module.getModuleOutputs().get(i)[1]];
			}
		}
		return outputsOfModule;
	}

	/**
	 * Calculates the Used Nodes of the Module
	 * @param module The Module
	 * @return The usedNodes of the Module (already set in Module)
	 */
	private static boolean[] getUsedNodesOfModule(Module module) {
		// mark nodes that are used as true
		boolean[] usedNodes = new boolean[module.getNrOfNodes()];
		// check for Output Nodes
		for (int i = 0; i < module.getNrOfOutputs(); i++) {
			int outputRefAddress = module.getModuleOutputs().get(i)[0];
			if (outputRefAddress >= module.getNrOfInputs()) {
				// mark only outputs if they dont use an input
				usedNodes[outputRefAddress-module.getNrOfInputs()] = true;
			}
		}
		// check all other nodes
		for (int i = module.getNrOfNodes() - 1; i >= 0; i--) {
			if (usedNodes[i]) {
				NodeECGP node = module.getNodes().get(i);
				// mark all nodes this node uses as used
				for (int[] inputOfNode : node.getInput()) {
					if (inputOfNode[0] >= module.getNrOfInputs()) {
						// else: it is an input Node
						usedNodes[inputOfNode[0] - module.getNrOfInputs()] = true;
					}
				}
			}
		}
		module.setUsedNodes(usedNodes);
		return usedNodes;
	}
}
