package milestonetwo.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import milestonetwo.entity.MetricEntity;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class CSVController {
		
	/**
	 * @param baseFilePath
	 * @return
	 * @throws IOException
	 */
	
	public FileWriter initializeCSVResult(String baseFilePath) throws IOException {
		
		FileWriter csvResult = new FileWriter(baseFilePath + "_RESULT.csv");	
		
		csvResult.append("Dataset," + 
						"#TrainingRelease," + 
						"%Training," + 
						"%DefectiveTraining," +
						"%DefectiveTesting," +
						"Classifier," + 
						"Balancing," + 
						"Feature Selection," + 
						"Sensitivity," + 
						"TP,FP,TN,FN," + 
						"Precision," + 
						"Recall," + 
						"AUC," + 
						"Kappa\n");
		
		return csvResult;
	}
		
	/**
	 * Step of this function:
	 * 
	 * Take a CSV file
	 * read the first row for each instance
	 * copy the current instance in other file
	 * 
	 * @param nameCSVProject
	 * @param fileCSVList
	 * @param firstRelease
	 * @param numberFeature
	 * @throws IOException
	 */
	
	public void splitCSV(String nameCSVProject, List<String> fileCSVList, 
							String firstRelease, int numberFeature) throws IOException {
		
		CSVReader reader = null;  
		
		try{  
						
			reader = new CSVReader(new FileReader(nameCSVProject));  
			
			String [] nextLine;  
			int currentRow = 1;
			
			StringBuilder attributeList = new StringBuilder();

			int version = 1;
			
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
						addRowToCSV(nextLine, csv);
						break;
					
					}else {
						
						//create the first row with the feature
						attributeList.append(token);
						if(currentRow != numberFeature + 1) attributeList.append(",");
						currentRow++;
					}

				}  
			
			}  
			
			csv.close();		
		}  
		
		catch (Exception e)   
		
		{  
		
			e.printStackTrace();  
			
		}  
	}
	
	/**
	 * @param nextLine
	 * @param csv
	 * @throws IOException
	 */
	
	public static void addRowToCSV(String [] nextLine, FileWriter csv) throws IOException {
		
		StringBuilder stringToAppend = new StringBuilder();

		int c = 0;
		for(String token : nextLine) {
			
			if(c != 0) stringToAppend.append(",");
			stringToAppend.append(token);
			c++;

		}
						
		//add to file
		csv.append(stringToAppend + "\n");
					
	}

	/**
	 * @param args -> CSV input file and ARFF output file
	 * @throws Exception
	 */
	
	public void csvConverter(String[] args){
	    
	    // load CSV
	    CSVLoader loader = new CSVLoader();
	    try {
			loader.setSource(new File(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    Instances data = null;
		try {
			data = loader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    //remove name path
	    //index = 1, corresponds to path name
    	Remove removeFilter = new Remove();
	    	
    	int[] indices = {1};
		removeFilter.setAttributeIndicesArray(indices);
	    removeFilter.setInvertSelection(false);
	    try {
			removeFilter.setInputFormat(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    try {
			data = Filter.useFilter(data, removeFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}	    
	    
	    /*
	     * This assumes that indices contains 
	     * the indices of attributes that you want to keep. 
	     * If it contains the indices of the attributes you want 
	     * to remove, the delete the call to 
	     * the setInvertSelection method.
	     *  
	     */
	   	    
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    try {
			saver.setFile(new File(args[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			saver.setDestination(new File(args[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    
	    List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(args[1]).toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    for (String line : lines) {
	    	if (line.contains("@attribute Bugginess {No,Yes}")) {
	    		lines.set(lines.indexOf(line), "@attribute bugginess {Yes,No}");
	    	}
	    }
	    
	    
	    try {
			Files.write(new File(args[1]).toPath(), lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }
}
