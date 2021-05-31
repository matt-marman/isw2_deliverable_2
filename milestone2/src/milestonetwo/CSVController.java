package milestonetwo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.opencsv.CSVReader;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class CSVController {
	
	/*
	 * - take a CSV file
	 * - read the first row for each instance
	 * - copy the current instance in other file
	 * 
	 */
	
	//*BUG FIND* in the last split, the instance is not complete!
	//it has need to remove 
	public static void splitCSV(String nameCSVProject, ArrayList<String> fileCSVList, String firstRelease, int numberFeature) throws IOException {
		
		CSVReader reader = null;  
		try{  
						
			reader = new CSVReader(new FileReader(nameCSVProject));  
			
			String [] nextLine;  
			int currentRow = 1;
			
			String attributeList = "";

			int version = 1;
			
			File fileCheck = new File(fileCSVList.get(version - 1));
			if(fileCheck.exists()) return;

			FileWriter csv = new FileWriter(fileCSVList.get(version - 1));
			String currentVersion = firstRelease;
						
			//read one line at a time  
			while ((nextLine = reader.readNext()) != null)  {  
								
				for(String token : nextLine) {  
			
					if(currentRow >= numberFeature + 2) {

						if(token.equals(currentVersion)) {
							
							if(currentRow == numberFeature + 2) csv.append(attributeList + "\n");
							currentRow++;
							
						}else {
							
							version++;
							csv.close();

							csv = new FileWriter(fileCSVList.get(version - 1));
							csv.append(attributeList + "\n");

							currentVersion = token;
							
						}
						
						//add the row to a new file		
						addRowToCSV(nextLine, attributeList, csv);
						break;
					
					}else {
						
						//create the first row with the feature
						attributeList += token;
						if(currentRow != numberFeature + 1) attributeList += ",";
						currentRow++;
					}

				}  
			
			}  
			
		}  
		
		catch (Exception e)   
		
		{  
		
			e.printStackTrace();  
			
		}  
	}
	
	public static void addRowToCSV(String [] nextLine, String attributeList, FileWriter csv) throws IOException {
		
		String StringToAppend = "";
		int c = 0;
		for(String token : nextLine) {
			
			if(c != 0) StringToAppend += ",";
			StringToAppend += token;
			c++;

		}
						
		//add to file
		csv.append(StringToAppend + "\n");
					
	}

	/**
	   * takes 2 arguments:
	   * - CSV input file
	   * - ARFF output file
	   */

	public static void csvConverter(String[] args) throws Exception {
	    
		if (args.length != 2) {
		      System.out.println("\nUsage: CSV2Arff <input.csv> <output.arff>\n");
		      System.exit(1);
	    }

	    // load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(args[0]));
	    Instances data = loader.getDataSet();
	    
	    //remove name path
	    //index = 1, corresponds to path name
	    int[] indices = {1};
	    
	    /*
	     * This assumes that indices contains 
	     * the indices of attributes that you want to keep. 
	     * If it contains the indices of the attributes you want 
	     * to remove, the delete the call to 
	     * the setInvertSelection method.
	     *  
	     */
	   
	    Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(indices);
	    removeFilter.setInvertSelection(false);
	    removeFilter.setInputFormat(data);
	    
	    data = Filter.useFilter(data, removeFilter);	    
	    
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(args[1]));
	    saver.setDestination(new File(args[1]));
	    saver.writeBatch();
	  }
}
