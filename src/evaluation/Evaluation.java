package evaluation;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import cartesianNetwork.Evolution;
import cartesianNetwork.Individual;
import helperClasses.InputWithClassification;

/**
 * Class for calculating fitness of Individual
 * UsedNodes willa slo be calculated
 * @author Björn Piepenbrink
 *
 */
public class Evaluation {
	
	/**
	 * Calculates the fittest Individuals of the given Population
	 * @param callingObject The EvolutionObject that calls this Method
	 * @param x The number of Individuals that should be returned
	 * @param print if Output to the Console is allowed
	 * @param indivs The Population that should be evalutated (With Parents)
	 * @param inputs The Inputs for Fitness-Calculation
	 * @param parents The Parents of the Generation
	 * @param precalcFitness if you want to calculate the Fitness by comparing Nodes
	 * @return The x Fittests Individuals of the Population
	 * @throws Exception if something unexpected happens
	 */
	public static ArrayList<Individual> getXFittestIndividuals(Evolution callingObject, int x, boolean print, ArrayList<Individual> indivs,
			ArrayList<InputWithClassification> inputs, ArrayList<Individual> parents, boolean precalcFitness) throws Exception {
		if (print)
			System.out.println("calculating Fittest individual out of " + indivs.size());
		//assign every Individual a fitness
		indivs = calculateFitnessThreading(parents, indivs, inputs, precalcFitness);
		//get the x best Individuals out of this list
		ArrayList<Individual> fittestIndivs;
		if(parents == null){
			fittestIndivs = getXBest(0, x, indivs);
		}
		else{
			fittestIndivs = getXBest(parents.size(), x, indivs);
		}
		if (print) {
			System.out.println("FITTEST INDIVIDUALS:");
		}
		for (Individual indiv : fittestIndivs) {
			if (print) {
				System.out.println(indiv.getFitness());
			}
			if(indiv.getFitness()==0){
				//if best Fitness was found - mark in the EvolutionRun!
				callingObject.solutionFound();
			}
		}
		return fittestIndivs;
	}

	/**
	 * Calculates the Fitness by using threading
	 * @param parents The Parents of the Generation
	 * @param indiv The whole Population that should be evaluated
	 * @param inputs the Inputs for Fitness-Calculation
	 * @param precalcFitness if you want to calculate the Fitness by comparing Nodes
	 * @return The Population with calculated Fitness-Values
	 * @throws Exception if something unexpected happens
	 */
	private static ArrayList<Individual> calculateFitnessThreading(ArrayList<Individual> parents,
			ArrayList<Individual> indiv, ArrayList<InputWithClassification> inputs, boolean precalcFitness) throws Exception {
		//executor for handling Threads
		ExecutorService executor = Executors.newCachedThreadPool();
		ArrayList<Future<Integer>> results = new ArrayList<>();
		//add calculated fitness (or null if fitness already exists)
		for (int i = 0; i < indiv.size(); i++) {
			if (!(indiv.get(i).hasFitness())) {
				if(precalcFitness){
					results.add(executor.submit(new CallableCalculator(parents, indiv.get(i), inputs)));
				}
				else{
					//add null as Parents so UsedNodes wont be compared
					results.add(executor.submit(new CallableCalculator(null, indiv.get(i), inputs)));
				}
			} else {
				results.add(null);
			}
		}
		//assign fitnesses of Individuals
		for (int i = 0; i < indiv.size(); i++) {
			if (!(indiv.get(i).hasFitness())) {
				indiv.get(i).setFitness(results.get(i).get());
				System.out.println("Fitness of Individual: " + indiv.get(i).getFitness());
			}
		}
		//close all threads (or else they dont stop)
		executor.shutdown();
		return indiv;
	}
	
	/**
	 * returns random best Individuals
	 * @param parentsNr The number of Parents in the given Individual-List
	 * @param x the number of Individuals to return
	 * @param indivs The List of Individuals (parents have to be on the first positions)
	 * @return The best Individuals of the List
	 * @throws Exception if x is bigger than the list or something unexpected
	 */
	private static ArrayList<Individual> getXBest(int parentsNr, int x, ArrayList<Individual> indivs) throws Exception {
		
		if (x > indivs.size()) {
			throw new IllegalArgumentException(
					"tried to get more individuals out of a list than possible - Evaluation");
		}
		
		if(parentsNr==0){
			ArrayList<Individual> toReturn = new ArrayList<>();
			for(int i=0;i<x;i++){
				toReturn.add(getRandomBestAndDelete(indivs));
			}
			return toReturn;
		}
		
		ArrayList<Individual> toReturn = new ArrayList<>();
		// first parentsNr positions are parents
		Individual bestParent = indivs.get(0);
		int fittestParentFitness = indivs.get(0).getFitness();
		for (int i = 1; i < parentsNr; i++) {
			int currentfitness = indivs.get(i).getFitness();
			// smaller fitness means better
			if (currentfitness <= fittestParentFitness) {
				fittestParentFitness = currentfitness;
				bestParent = indivs.get(i);
			}
		}
		//all Individual that are better than the best Parent
		ArrayList<Individual> listBetterThanParent = new ArrayList<>();
		for (int i = x; i < indivs.size(); i++) {
			int currentfitness = indivs.get(i).getFitness();
			// smaller fitness means better
			if (currentfitness <= bestParent.getFitness()) {
				listBetterThanParent.add(indivs.get(i));
				//remove Individuals from original list
				indivs.remove(i);
			}
		}
		// add all Individual that are better than the best Parent to the list
		int numberOfBetterIndivs = listBetterThanParent.size();
		for (int i = 0; i < numberOfBetterIndivs && toReturn.size() < x; i++) {
			toReturn.add(getRandomBestAndDelete(listBetterThanParent));
		}

		//if toReturn-List contains too few elements:
		//add Parents with highest fitness to the list
		int numberOfBestParents = 0;
		if (toReturn.size() < x) {
			ArrayList<Individual> bestParents = new ArrayList<>();
			for (int i = 0; i < parentsNr; i++) {
				int currentfitness = indivs.get(i).getFitness();
				//if parents has highest fitness
				if (currentfitness == fittestParentFitness) {
					bestParents.add(indivs.get(i));
					//remove added Parents from original list
					indivs.remove(i);
				}
			}
			numberOfBestParents = bestParents.size();
			for (int i = 0; i < numberOfBestParents && toReturn.size() < x; i++) {
				toReturn.add(getRandomBestAndDelete(bestParents));
			}

			// if list-Size still < x do all the same things just:
			// add only Indivs that are not in the list
			// dont count the best parents (already in list)
			// dont count the best Individuals frombefore (already in list)
			if (toReturn.size() < x) {
				toReturn.addAll(getXBest(parentsNr - numberOfBestParents, x - toReturn.size(), indivs));
			}
		}

		return toReturn;
	}

	/**
	 * Returns a random best Individual of the List
	 * @param indivs The List that the Individual should be taken from
	 * @return The best Individual of the lost
	 * @throws Exception is somthing unexpected happens
	 */
	private static Individual getRandomBestAndDelete(ArrayList<Individual> indivs) throws Exception {
		// list with best Individuals
		ArrayList<Individual> fittestInds = new ArrayList<>();
		fittestInds.add(indivs.get(0));
		for (int i = 1; i < indivs.size(); i++) {
			int currentfitness = indivs.get(i).getFitness();
			// smaller fitness means better
			if (currentfitness == fittestInds.get(0).getFitness()) {
				//if same fitness add to list
				//random one is chosen later
				fittestInds.add(indivs.get(i));
			}
			if (currentfitness < fittestInds.get(0).getFitness()) {
				//completely generate new List
				//since new Individual is better than before
				fittestInds = new ArrayList<>();
				fittestInds.add(indivs.get(i));
			}
		}
		//choose random Individual from the best
		int randPos = ThreadLocalRandom.current().nextInt(0, fittestInds.size());
		Individual bestIn = fittestInds.get(randPos);
		indivs.remove(randPos);
		return bestIn;
	}

}
