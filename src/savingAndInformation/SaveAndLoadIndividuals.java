package savingAndInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import cartesianNetwork.Individual;

/**
 * for saving and loading Indiciduals of the ECGP-net
 * 
 * @author Björn Piepenbrink
 *
 */
public class SaveAndLoadIndividuals {

	/**
	 * Saves Individual as txt-File
	 * @param indiv The Individual to save
	 * @param path The path where to save
	 * @param fileName The Name of the file
	 */
	public static void saveIndividual(Individual indiv, String path, String fileName) {
		try {
			FileOutputStream f = new FileOutputStream(new File(path + fileName + ".txt"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			o.writeObject(indiv);

			o.close();
			f.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			e.printStackTrace();
		}
	}

	/**
	 * Saves a List of Individuals as txt-Files	
	 * @param indivs The Individuals to save
	 * @param path The path where to save
	 * @param fileName The Name of the file
	 */
	public static void saveIndividual(ArrayList<Individual> indivs, String path, String fileName) {
		for (int i = 0; i < indivs.size(); i++) {
			saveIndividual(indivs.get(i), path, fileName + "_" + i);
		}
	}

	/**
	 * Saves a String as txt-File
	 * @param text The String to save
	 * @param path The path where to save
	 * @param fileName The Name of the file
	 */
	public static void saveStatistics(String text, String path, String fileName) {
		try {
			FileOutputStream f = new FileOutputStream(new File(path + fileName + ".txt"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			o.writeObject(text);

			o.close();
			f.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			e.printStackTrace();
		}
	}

	/**
	 * read a file that contains an individual
	 * 
	 * @param filePath
	 *            the Path of the file + the file name + file ending
	 * @return the Individual read from the file
	 */
	public static Individual readIndividual(String filePath) {
		Individual indiv = null;
		try {
			FileInputStream fi = new FileInputStream(new File(filePath));
			ObjectInputStream oi = new ObjectInputStream(fi);

			indiv = (Individual) oi.readObject();

			oi.close();
			fi.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return indiv;
	}

	/**
	 * saves new Individual only if Fitness is smaller or equal than the fitness
	 * of the already saved one
	 * 
	 * @param indiv
	 *            The new Individual
	 * @param path The path where it should check
	 * @param fileName
	 *            the name of the file it should check and overwrite if
	 *            necessary
	 * @throws Exception if an error occurs during saving/reading
	 */
	public static void saveBestIndividual(Individual indiv, String path, String fileName) throws Exception {
		try {

			Individual best = readIndividual(path + "" + fileName + ".txt");
			if (best == null) {
				saveIndividual(indiv, path, fileName);
				System.out.println("saved Individual with Fitness" + indiv.getFitness());
				return;
			}
			if (indiv.getFitness() <= best.getFitness()) {
				saveIndividual(indiv, path, fileName);
				System.out.println("saved Individual with Fitness" + indiv.getFitness());
			} else {
				System.out.println("saved Individual has Fitness: " + best.getFitness());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			saveIndividual(indiv, path, fileName);
			System.out.println("saved Individual with Fitness" + indiv.getFitness());
		}
	}
}
