package milestonetwo;

import weka.core.Attribute;
import weka.core.Instance;

/*
 *  How to use WEKA API in Java 
 *  Copyright (C) 2014 
 *  @author Dr Noureddin M. Sadawi (noureddin.sadawi@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it as you wish ... 
 *  I ask you only, as a professional courtesy, to cite my name, web page 
 *  and my YouTube Channel!
 *  
 */

//import required classes
import weka.core.Instances;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;
import weka.classifiers.lazy.IBk;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;  
/*
 * Obiettivo:
 * Comparare l’accuratezza (Precision/Recall/AUC/Kappa),
 * Di tre classificatori (RandomForest / NaiveBayes / Ibk),
 * sui progetti selezionati precedentemente
 * (Deliverable 2 - Milestone 1),
 * utilizzando la tecnica di validazione «WalkForward»
 * 
 * Per rispondere alla domanda sopra si consiglia di
	creare, e poi analizzare, un file avente le seguenti
	colonne: dataset, #TrainingRelease, Classifier,
	Precision, Recall, AUC, Kappa.
 * 
 */

public class Main{
	
	//false = Bookkeeper project
	//true = Syncope project
	public static boolean projectSelection = false;
	
	public static String pathFile = "/home/mattia/Desktop/ingegneria_software_2/Falessi/isw2_deliverable_2/milestone2/";
	public static FileWriter CSVResult;
	public static FileWriter csv;
	
	public static String extensionArff = ".arff";
	public static String extensionCSV = ".csv";
	public static String fileCSV = "";
	public static String fileARFF = "";
	public static String baseFilePath = "";
	public static String firstRelease = "";
	public static String projectName = "";
	
	public static int numberFeature;
	public static int numberRelease;
	public static ArrayList<String> fileArffList;
	public static ArrayList<String> fileCSVList;


	public static void main(String args[]) throws Exception{
		
		//set attributes for Syncope project
		if(projectSelection) {
			
			fileCSV = pathFile + "syncopeFile/SYNCOPE.csv";
			fileARFF = pathFile + "syncopeFile/SYNCOPE.arff";
			baseFilePath = pathFile + "syncopeFile/SYNCOPE";
			firstRelease = "1.0.0-incubating";
			projectName = "SYNCOPE";
			
		}else {
			
			//else set bookkeeper attributes
			fileCSV = pathFile + "bookkeeperFile/BOOKKEEPER.csv";
			fileARFF = pathFile + "bookkeeperFile/BOOKKEEPER.arff";
			baseFilePath = pathFile + "bookkeeperFile/BOOKKEEPER";
			firstRelease = "4.0.0";
			projectName = "BOOKKEEPER";
		}
					
		fileArffList = new ArrayList<String>();
		fileCSVList = new ArrayList<String>();
			
		File fileCheck = new File(fileARFF);
				
			if(!fileCheck.exists()) {
					
				String[] params = {fileCSV, fileARFF};
				csvConverter(params);
					
			}
			
		DataSource source = new DataSource(fileARFF);
		numberFeature = source.getDataSet().numAttributes();
		Attribute versions = source.getDataSet().attribute(0);
		numberRelease = versions.numValues();
		
		int version = 1;
		//convert each .csv file generated		
		for(int k = 0; k < numberRelease; k++) {
			
			String currentCSVFile = baseFilePath;
			currentCSVFile += String.valueOf(version);
			currentCSVFile += extensionCSV;
			fileCSVList.add(currentCSVFile);
			
			String currentArffFile = baseFilePath;
			currentArffFile += String.valueOf(version);
			currentArffFile += extensionArff;
			fileArffList.add(currentArffFile);
			
			version++;
		}
		
		splitCSV(fileCSV);	

		
		for(int k = 0; k < numberRelease; k++) {
			
			String[] params = {fileCSVList.get(k), fileArffList.get(k)};
			csvConverter(params);
			
		}
		
		//use walk forward
		walkForward(fileArffList, versions, firstRelease);
			
	}
	
	/*
	 * - take a CSV file
	 * - read the first row for each instance
	 * - copy the current instance in other file
	 * 
	 */
	
	//*BUG FIND* in the last split, the instance is not complete!
	//it has need to remove 
	public static void splitCSV(String nameCSVProject) throws IOException {
		
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
	
	public static void walkForward(ArrayList<String> releaseFileArffList, Attribute versions, String firstRelease) throws Exception {
		
		//create array with all release
		ArrayList<String> releaseList = new ArrayList<String>();

		for(int j = 0; j < numberRelease; j++) {
			
			String version = versions.value(j);			
			releaseList.add(version);
		}
		
		CSVResult = new FileWriter(baseFilePath + "_RESULT" + extensionCSV);
		CSVResult.append("Dataset,#TrainingRelease,%Training,%DefectiveTraining,%DefectiveTesting,Classifier,Precision,Recall,AUC,Kappa\n");
		
		Instances training = null;
		DataSource sourceTraining = null;
		
		for(int j = 0; j < numberRelease - 1; j++) {
			
			//merge instances
			if(j != 0) {
				
				DataSource newSourceTraining = new DataSource(releaseFileArffList.get(j));
				Instances newTraining = newSourceTraining.getDataSet();	
				
				String releaseNewTraining = newSourceTraining.getDataSet().attribute(0).toString().subSequence(29, 34).toString();
				
				//change the version number
				//for merge the two set
				newTraining.renameAttributeValue(
						newTraining.attribute("Version Number"),
						releaseNewTraining,
					    firstRelease);
						
				training = mergeInstances(training, newTraining);
				
			}else {
				
				//first iteration, only one set for training and another one for testing
				sourceTraining = new DataSource(releaseFileArffList.get(j));
				training = sourceTraining.getDataSet();	
			}
				
			//create testing set
			DataSource sourceTesting = new DataSource(releaseFileArffList.get(j + 1));
			Instances testing = sourceTesting.getDataSet();
			
			int numAttr = training.numAttributes();
			
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
			
			//calculate %Defective in training and testing set
			int [] compositionDefectiveTraining = calculateNumDefective(training);
			int [] compositionDefectiveTesting= calculateNumDefective(testing);	

			//use RandomForest, NaiveBayes, Ibk as classifiers
			NaiveBayes naiveBayes = new NaiveBayes();
			IBk ibk = new IBk();
			RandomForest randomForest = new RandomForest();

			naiveBayes.buildClassifier(training);
			Evaluation evalNaiveBayes = new Evaluation(testing);	
			evalNaiveBayes.evaluateModel(naiveBayes, testing); 

			ibk.buildClassifier(training);
			Evaluation evalIbk = new Evaluation(testing);	
			evalIbk.evaluateModel(ibk, testing); 
					
			randomForest.buildClassifier(training);
			Evaluation evalRandomForest = new Evaluation(testing);	
			evalRandomForest.evaluateModel(randomForest, testing); 
			
			//%Training = training / total data
			float trainingInstance = training.numInstances();
			float totalInstance = trainingInstance + testing.numInstances();
			float percentageTraining = (trainingInstance / totalInstance) * 100;
			
			//let's calculate the metrics for each classifier
			calculateMetric(evalNaiveBayes, j + 1, "NaiveBayes", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting);
			calculateMetric(evalIbk, j + 1, "IBk", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting);
			calculateMetric(evalRandomForest, j + 1, "RandomForest", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting);
			
			CSVResult.flush();
							
		}		
		
	}
	
	public static int[] calculateNumDefective(Instances set) {
		
		int numberDefective = 0;
		int numberNotDefective = 0;
		int[] total = {numberDefective, numberNotDefective};
		int numberInstances = 0;
		numberInstances = set.numInstances();
		for (int k = 1; k < numberInstances; k++) {
			
			Instance currentInstances = set.instance(k);
			
			if(currentInstances.stringValue(10).equals("Yes")) numberDefective++;
			else numberNotDefective++;
			
		}
		
		total[0] = numberDefective;
		total[1] = numberNotDefective;
	
		return total;
	}
	
	/*
	 * Precision, Recall, AUC, Kappa. 
	 */
	public static void calculateMetric(Evaluation eval, int numberRelease, String classifierName, 
										float percentageTraining, int [] compositionDefectiveTraining, int [] compositionDefectiveTesting) throws IOException {
		
		float totalInstancesTraining = compositionDefectiveTraining[0] + compositionDefectiveTraining[1];
		float percentageDefectiveTraining = compositionDefectiveTraining[0] / totalInstancesTraining * 100;
		
		float totalInstancesTesting = compositionDefectiveTesting[0] + compositionDefectiveTesting[1];
		float percentageDefectiveTesting = compositionDefectiveTesting[0] / totalInstancesTesting * 100;
		
		System.out.println("\n" + classifierName);
		System.out.println("Precision = " + eval.precision(0));
		System.out.println("Recall = " + eval.recall(0));
		System.out.println("AUC = " + eval.areaUnderROC(1));
		System.out.println("kappa = " + eval.kappa());
		System.out.println("%Training = " + percentageTraining);
		System.out.println("%DefectiveTraining = " + percentageDefectiveTraining);
		System.out.println("%DefectiveTesting = " + percentageDefectiveTesting);
		
		//write the result .csv file
		CSVResult.append(projectName + "," + numberRelease + "," + percentageTraining + "," +
						percentageDefectiveTraining + "," + percentageDefectiveTesting + "," +
						classifierName + "," + eval.precision(0) + "," + eval.recall(0) +  "," + 
						eval.areaUnderROC(1) + "," + eval.kappa() + "\n");
	}
	
	/*
	 	Please note that the following conditions should hold (there are not checked in the function):

		- Datasets must have the same attributes structure (number of attributes, type of attributes)
		- Class index has to be the same
		- Nominal values have to exactly correspond

	 */
	public static Instances mergeInstances(Instances data1, Instances data2)
		    throws Exception
		{
		    // Check where are the string attributes
		    int asize = data1.numAttributes();
		    boolean strings_pos[] = new boolean[asize];
		    for(int i=0; i<asize; i++)
		    {
		        Attribute att = data1.attribute(i);
		        strings_pos[i] = ((att.type() == Attribute.STRING) ||
		                          (att.type() == Attribute.NOMINAL));
		    }

		    // Create a new dataset
		    Instances dest = new Instances(data1);
		    dest.setRelationName(data1.relationName() + "+" + data2.relationName());

		    DataSource source = new DataSource(data2);
		    Instances instances = source.getStructure();
		    Instance instance = null;
		    while (source.hasMoreElements(instances)) {
		        instance = source.nextElement(instances);
		        dest.add(instance);

		        // Copy string attributes
		        for(int i=0; i<asize; i++) {
		            if(strings_pos[i]) {
		                dest.instance(dest.numInstances()-1)
		                    .setValue(i,instance.stringValue(i));
		            }
		        }
		    }

		    return dest;
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
