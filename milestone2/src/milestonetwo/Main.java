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
	
	//0 = Bookkeeper project
	//1 = Syncope project
	public static int projectSelection = 1;
	
	public static int numberRelease = 7;
	public static String firstRelease = "4.0.0";
	public static int numberClassifires = 3;
	public static String pathFile = "/home/mattia/Desktop/ingegneria_software_2/Falessi/isw2_deliverable_2/milestone2/";
	public static FileWriter CSVResult;
	public static int numberVersionSyncope; 
	public static FileWriter csv;
	
	public static void main(String args[]) throws Exception{
					
		ArrayList<String> releaseFileArffList = new ArrayList<String>();

		String[] CSVFile = new String[] {"bookkeeperReleaseOne.csv", "bookkeeperReleaseTwo.csv", "bookkeeperReleaseThree.csv", 
				"bookkeeperReleaseFour.csv", "bookkeeperReleaseFive.csv", "bookkeeperReleaseSix.csv", "bookkeeperReleaseSeven.csv"};
		
		String[] ArffFile = new String[] {"bookkeeperReleaseOne.arff", "bookkeeperReleaseTwo.arff", "bookkeeperReleaseThree.arff", 
				"bookkeeperReleaseFour.arff", "bookkeeperReleaseFive.arff", "bookkeeperReleaseSix.arff", "bookkeeperReleaseSeven.arff"};
		
		String fileCSV = "";
		String fileARFF = "";
		
		//find all .csv file 
		for(int i = 0; i < CSVFile.length; i++) {
			
			fileCSV = pathFile + CSVFile[i];
			fileARFF = pathFile + ArffFile[i];
			releaseFileArffList.add(fileARFF);
			
			File fileCheck = new File(fileARFF);
				
				if(!fileCheck.exists()) {
					
					String[] params = {fileCSV, fileARFF};
					csvConverter(params);
					
				}		
			
		}
				
		fileCSV = pathFile + "BOOKKEEPER.csv";
		fileARFF = pathFile + "BOOKKEEPER.arff";
		
		File fileCheck = new File(fileARFF);
			
			if(!fileCheck.exists()) {
				
				String[] params = {fileCSV, fileARFF};
				csvConverter(params);
				
			}
			
		fileCSV = pathFile + "syncopeFile/SYNCOPE.csv";
		fileARFF = pathFile + "syncopeFile/SYNCOPE.arff";
			
		File fileCheckSyncope = new File(fileARFF);
				
			if(!fileCheckSyncope.exists()) {
					
				String[] params = {fileCSV, fileARFF};
				csvConverter(params);
					
			}
				
		numberVersionSyncope = 7;
		splitCSV(pathFile + "syncopeFile/SYNCOPE.csv");	
		return;
		
		/*
		DataSource source = new DataSource(fileARFF);
		Attribute versions = source.getDataSet().attribute(0);
		
		//use walk forward
		walkForward(releaseFileArffList, versions);
		*/
	}
	
	/*
	 * - take a CSV file
	 * - read the first row for each instance
	 * - copy the current instance in other file
	 * 
	 */
	
	public static void splitCSV(String nameCSVProject) throws IOException {
	
		
		CSVReader reader = null;  
		try{  
			
			reader = new CSVReader(new FileReader(nameCSVProject));    
			String [] nextLine;  
			int currentRow = 1;
			
			String extension = ".csv";
			String currentCSVFile = pathFile + "syncopeFile/SYNCOPE";
			
			String attributeList = "";
			String currentVersion = "1.0.0-incubating";
			
			int version = 1;
			
			currentCSVFile = pathFile + "syncopeFile/SYNCOPE";
			currentCSVFile += String.valueOf(version);
			currentCSVFile += extension;
			FileWriter csv = new FileWriter(currentCSVFile);

			//read one line at a time  
			while ((nextLine = reader.readNext()) != null)  {  
				
				for(String token : nextLine) {  
			
					if(currentRow >= 13) {

						if(token.equals(currentVersion)) {
							
							if(currentRow == 13) csv.append(attributeList + "\n");
							currentRow++;
							
						}else {
							
							version++;
							csv.close();

							currentCSVFile = pathFile + "syncopeFile/SYNCOPE";
							currentCSVFile += String.valueOf(version);
							currentCSVFile += extension;
							
							csv = new FileWriter(currentCSVFile);
							csv.append(attributeList + "\n");

							currentVersion = token;
							
						}
						
						//add the row to a new file		
						addRowToCSV(nextLine, attributeList, csv);
						break;
					
					}else {
						
						//create the first row with the feature
						attributeList += token;
						if(currentRow != 12) attributeList += ",";
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
	
	public static void walkForward(ArrayList<String> releaseFileArffList, Attribute versions) throws Exception {
		
		//create array with all release
		ArrayList<String> releaseList = new ArrayList<String>();

		for(int j = 0; j < numberRelease; j++) {
			
			String version = versions.value(j);			
			releaseList.add(version);
		}
		
		CSVResult = new FileWriter(pathFile + "BOOKKEEPER_RESULT.csv");
		CSVResult.append("Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa\n");
		
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
				
				//first iteration, it does not have the testing set
				//just skip
				sourceTraining = new DataSource(releaseFileArffList.get(j));
				training = sourceTraining.getDataSet();	
				continue;
			}
				
			//create testing set
			DataSource sourceTesting = new DataSource(releaseFileArffList.get(j + 1));
			Instances testing = sourceTesting.getDataSet();
			
			int numAttr = training.numAttributes();
			
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
			
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
			
			//let's calculate the metrics for each classifier
			calculateMetric(evalNaiveBayes, j, "NaiveBayes");
			calculateMetric(evalIbk, j, "IBk");
			calculateMetric(evalRandomForest, j, "RandomForest");
			
			CSVResult.flush();
							
		}		
		
	}
	
	/*
	 * Precision, Recall, AUC, Kappa. 
	 */
	public static void calculateMetric(Evaluation eval, int numberRelease, String classifierName) throws IOException {
		
		System.out.println("\n" + classifierName);
		System.out.println("Precision = " + eval.precision(0));
		System.out.println("Recall = " + eval.recall(0));
		System.out.println("AUC = " + eval.areaUnderROC(1));
		System.out.println("kappa = " + eval.kappa());
		
		//write the result .csv file
		CSVResult.append("BOOKKEEPER" + "," + numberRelease + "," + classifierName + "," + eval.precision(0) + "," + eval.recall(0) +  "," + eval.areaUnderROC(1) + "," + eval.kappa() + "\n");
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
