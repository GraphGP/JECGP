package savingAndInformation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import helperClasses.Digit;
import helperClasses.InputWithClassification;

/**
 *  For reading the MNIST-Data in CSV-format
 *  
 *  the MNIST-Data in CSV format was downloaded at: 
 *  https://pjreddie.com/projects/mnist-in-csv/
 *  
 * @author Björn Piepenbrink
 *
 */
public class CSVReader {  
	/**
     * Reads the MNIST-Learn-Set
     * @param path Where the Set is saved
     * @return A List of Pictures from the Set
     */
	static public ArrayList<InputWithClassification> getLearnSet(String path){
    	return readCVS(path,false);
    }
    
    /**
     * Reads the MNIST-Test-Set
     * @param path Where the Set is saved
     * @return A List of Pictures from the Set
     */
    static public ArrayList<InputWithClassification> getTestSet(String path){
    	return readCVS(path,true);
    }
	
    
    /**
     * Reads the given MNIST-Set
     * @param path where the set is saved
     * @param test if the Test-Set should be read
     * @return A List of Pictures from the Set
     */
    static public ArrayList<InputWithClassification> readCVS(String path, boolean test){
    	String csvFile = path+"mnist_train.csv";
    	if(test){
    		csvFile = path+"mnist_test.csv";
    	}
        String line = "";
        // use comma as separator
        String cvsSplitBy = ",";
        String[] seperatedString;
        ArrayList<InputWithClassification> digits = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	//per Line one Digit
            while ((line = br.readLine()) != null) {
                seperatedString = line.split(cvsSplitBy);
                int column=0;
                int row=0;
                int[][] pic = new int[28][28];
                for(int i = 1; i<seperatedString.length; i++){
                	pic[row][column] = Integer.parseInt(seperatedString[i]);
                		
                	column++;
                	if(column==28){
                		column=0;
                		row++;
                	}
                }
                int classification = Integer.parseInt(seperatedString[0]);
                Digit digit = new Digit(classification,pic);
                digits.add(digit);
                //System.out.println("Ein Digit hinzugefügt."+digitnr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //digits enthält jetzt alle Lernbeispiele
        return digits;
    }

}