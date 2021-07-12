package cartesianNetwork;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class for an ECGP-Node
 * @author Björn Piepenbrink
 *
 */
public class NodeECGP implements Serializable {

	private static final long serialVersionUID = 1L;
	// functionGene
	//consists of function: function[0] 
	//and node type: function[1]
	/*PRIMITIVE: (node type 0)
	 * primitive functions
	 */
	/*
	 * ORIGINAL: (node type 1)
	 * modules containing an original section of the genotype
	 */
	/*
	 * REPLICATED: (node type 2)
	 * reused modules containing a replicated section of the genotype
	 */
	private int[] function;
	
	//Input Genes:
	//input.get(x)[0] = node adress that the inputs are taken from for Input x
	//input.get(x)[1] = node output that the inputs are taken from for Input x
	private ArrayList<int[]> input;

	/**
	 * Creates a ECGP-Node
	 * @param function the Function-Gene consisting of
	 * 	function-adress on function[0] and
	 * 	node-type on function[1]
	 * @param input List of Inputs of the Node
	 * 	one Input consists of:
	 * 	the adress on pos[0]
	 *  the OutputNr on pos[1]
	 */
	public NodeECGP(int[] function, ArrayList<int[]> input) {
		this.function = function;
		this.input = input;
	}
	
	/**
	 * Copies the Node
	 * @return an identical Copy of the Node
	 */
	public NodeECGP copy(){
		int[] newFunction = new int[function.length];
		for(int i=0;i<function.length;i++){
			newFunction[i] = function[i];
		}
		ArrayList<int[]> newInput = new ArrayList<>();
		for(int i=0;i<input.size();i++){
			int[] array = new int[2];
			array[0] = input.get(i)[0];
			array[1] = input.get(i)[1];
			newInput.add(array);
		}
		return new NodeECGP(newFunction, newInput);
	}

	public int[] getFunction() {
		return function;
	}
	
	public int getNodeType(){
		return function[1];
	}
	
	public int getFunctionNr(){
		return function[0];
	}

	public void setFunctionNr(int function) {
		this.function[0] = function;
	}
	
	public void setNodeType(int nodeType) {
		this.function[1] = nodeType;
	}

	public void setFunction(int[] function) {
		this.function = function;
	}

	public ArrayList<int[]> getInput() {
		return input;
	}

	public void setInput(ArrayList<int[]> input) {
		this.input = input;
	}

}
