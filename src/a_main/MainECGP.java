package a_main;

import java.util.ArrayList;

import helperClasses.Digit;
import helperClasses.InputWithClassification;
import savingAndInformation.CSVReader;

public class MainECGP {

	/**
	 * E C G P
	 */
	
	static int functionSet = 6;
	
	static double mutation_rate = 0.06;
	static int nodeAmount = 1000;
	static int maxGenerations = 8000;
	
	static int levelsBack = -1;
	static int outputAmount = 1;
	static int maxModuleSize = 5;
	static int maxAllowedModules = -1;

	static int mu = 1;
	static int lambda = 4;

	static double compress_probability = 0.2;
	static double modulePointMutation_probability = 0.5;
	static double addInput_probability = 0.05;
	static double addOutput_probability = 0.02;

	static int tries = 10;

	static boolean useLearnSet = true;
	static boolean printOutput = true;
	static boolean diffCalc = false;
	static int maxAllowedSecondsForMutation = Integer.MAX_VALUE;

	static String pathMNIST = "../";
	static String path = "./";

	public static void main(String[] args) throws Exception {
		System.out.println("Starting PECGP");
		runECGP();
		System.out.println("Finished");
	}
	
	public static void runECGP() throws Exception{
		System.out.println("Reading from" + pathMNIST);
		ArrayList<InputWithClassification> inputs = CSVReader.getLearnSet(pathMNIST);
		
		int treshold = 50;
		
		for(InputWithClassification input : inputs){
			((Digit)input).makeSmaller();
			((Digit)input).makeBinary(treshold);
		}
		
		double time = System.currentTimeMillis();

		ECGP_NetHandler netHandler = new ECGP_NetHandler(path, printOutput,maxAllowedSecondsForMutation,diffCalc, inputs, nodeAmount, outputAmount,
				functionSet, maxModuleSize,maxAllowedModules, maxGenerations, mutation_rate,levelsBack, mu, lambda, compress_probability,
				modulePointMutation_probability, addInput_probability, addOutput_probability);

		netHandler.getXBestIndividuals(tries);
		
		double timeTaken = (System.currentTimeMillis() - time) / 1000;
		System.out.println("Time taken:" + timeTaken / 60 + "min");
	}
}
