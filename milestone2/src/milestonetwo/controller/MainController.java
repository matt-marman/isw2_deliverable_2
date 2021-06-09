package milestonetwo.controller;

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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import milestonetwo.entity.MetricEntity;
import milestonetwo.entity.ProjectEntity;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.lazy.IBk;
 
/*
 * Obiettivo:
 * Comparare l’accuratezza (Precision/Recall/AUC/Kappa),
 * Di tre classificatori (RandomForest / NaiveBayes / Ibk),
 * sui progetti selezionati precedentemente
 * (Deliverable 2 - Milestone 1),
 * utilizzando la tecnica di validazione «WalkForward»
 * MetricController
 * Per rispondere alla domanda sopra si consiglia di
	creare, e poi analizzare, un file avente le seguenti
	colonne: dataset, #TrainingRelease, Classifier,
	Precision, Recall, AUC, Kappa.
 * 
 */

public class MainController{
	
	//false = Bookkeeper project
	//true = Syncope project
	public static boolean projectSelection = false;
	
	public static String pathFile = "/home/mattia/Desktop/ingegneria_software_2/Falessi/isw2_deliverable_2/milestone2/";
	public static FileWriter CSVResult;
	public static FileWriter csv;
	
	public static String extensionArff = ".arff";
	public static String extensionCSV = ".csv";
	
	public static int numberFeature;
	public static int numberRelease;
	public static ArrayList<String> fileArffList;
	public static ArrayList<String> fileCSVList;

	public static void main(String args[]) throws Exception{
		
		ProjectEntity projectEntity = new ProjectEntity(); 
		
		//set attributes for project
		if(projectSelection) {
			
			projectEntity.setBaseFilePath(pathFile + "syncopeFile/SYNCOPE");
			projectEntity.setFileARFF(pathFile + "syncopeFile/SYNCOPE.arff");
			projectEntity.setFileCSV(pathFile + "syncopeFile/SYNCOPE.csv");
			projectEntity.setFirstRelease("1.0.0-incubating");
			projectEntity.setProjectName("SYNCOPE");
						
		}else {
			
			//else set bookkeeper attributes
			projectEntity.setBaseFilePath(pathFile + "bookkeeperFile/BOOKKEEPER");
			projectEntity.setFileARFF(pathFile + "bookkeeperFile/BOOKKEEPER.arff");
			projectEntity.setFileCSV(pathFile + "bookkeeperFile/BOOKKEEPER.csv");
			projectEntity.setFirstRelease("4.0.0");
			projectEntity.setProjectName("BOOKKEEPER");
	
		}
		
		MetricEntity metricEntity = new MetricEntity(); 
		metricEntity.setBalancing("No Sampling");
		
		String[] params = null;
		File fileCheck = new File(projectEntity.getFileARFF());			
		if(!fileCheck.exists()) {
					
			//in case it is created from PROJECT.csv 
			params[0] = projectEntity.getFileCSV();
			params[1] = projectEntity.getFileARFF();
			CSVController.csvConverter(params, metricEntity);
					
		}
		
		//create file where it is writing results
		CSVResult = new FileWriter(projectEntity.getBaseFilePath() + "_RESULT" + extensionCSV);	
		CSVResult.append("Dataset,#TrainingRelease,%Training,%DefectiveTraining,%DefectiveTesting,"
				+ "Classifier,Balancing,Feature Selection,Sensitivity,TP,FP,TN,FN,Precision,Recall,AUC,Kappa\n");
		
		fileArffList = new ArrayList<String>();
		fileCSVList = new ArrayList<String>();
		CSVController CSVController = new CSVController();
					
		for (int balancingSelectionIndex = 0; balancingSelectionIndex < 4; balancingSelectionIndex++) {
				
			BalancingController balancingController = new BalancingController();
			//it returns an .arff file and .csv file
			String[] arffFileBalancing = balancingController.applyBalancing(projectEntity.getFileARFF(), balancingSelectionIndex, 
											projectEntity.getBaseFilePath(), metricEntity);
				
				
			projectEntity.setFileARFF(arffFileBalancing[0]);
			projectEntity.setFileCSV(arffFileBalancing[1]);
				
			String[] params2 = {projectEntity.getFileCSV(), projectEntity.getFileARFF()};
			CSVController.csvConverter(params2, metricEntity);
								
			DataSource source = new DataSource(projectEntity.getFileARFF());
			numberFeature = source.getDataSet().numAttributes();
			Attribute versions = source.getDataSet().attribute(0);
			numberRelease = versions.numValues();

			//convert each .csv file generated		
			for(int version = 0; version < numberRelease; version++) {
						
				String currentCSVFile = projectEntity.getBaseFilePath();
				currentCSVFile += String.valueOf(version + 1);
				currentCSVFile += extensionCSV;
				fileCSVList.add(currentCSVFile);
						
				String currentArffFile = projectEntity.getBaseFilePath();;
				currentArffFile += String.valueOf(version + 1);
				currentArffFile += extensionArff;
				fileArffList.add(currentArffFile);
					
			}
						
			CSVController.splitCSV(projectEntity.getFileCSV(), fileCSVList, projectEntity.getFirstRelease(), numberFeature, metricEntity);	
					
			for(int j = 0; j < numberRelease; j++) {
					
				String[] paramsCSVController = {fileCSVList.get(j), fileArffList.get(j)};
				CSVController.csvConverter(paramsCSVController, metricEntity);
					
			}
					
			//use walk forward
			walkForward(fileArffList, versions, projectEntity, metricEntity);
								
		}
				
	}
			
	
		
	public static void walkForward(ArrayList<String> releaseFileArffList, Attribute versions, 
									ProjectEntity projectEntity, MetricEntity metricEntity) throws Exception {
		
		//create array with all release
		ArrayList<String> releaseList = new ArrayList<String>();

		for(int j = 0; j < numberRelease; j++) {
			
			String version = versions.value(j);			
			releaseList.add(version);
		}
		
		Instances training = null;
		DataSource sourceTraining = null;
					
		//No cost sensitive / Sensitive Threshold / Sensitive Learning (CFN = 10 * CFP)
		for (int sensitiveSelectionIndex = 0; sensitiveSelectionIndex < 3; sensitiveSelectionIndex++) {

			SensitiveSelectionController sensitiveSelectionController = new SensitiveSelectionController();
		
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
							projectEntity.getFirstRelease());
							
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
				
				
				MetricController metricController = new MetricController();
				//calculate %Defective in training and testing set
				int [] compositionDefectiveTraining = metricController.calculateNumDefective(training, metricEntity);
				int [] compositionDefectiveTesting= metricController.calculateNumDefective(testing, metricEntity);	
	
				//use RandomForest, NaiveBayes, Ibk as classifiers
				NaiveBayes naiveBayes = new NaiveBayes();
				IBk ibk = new IBk();
				RandomForest randomForest = new RandomForest();
				
				//use feature selection
				for (int featureSelectionIndex = 0; featureSelectionIndex < 2; featureSelectionIndex++) {
					
					FeatureSelectionController featureSelectionController = new FeatureSelectionController();
					Instances [] set = featureSelectionController.applyFeatureSelection(featureSelectionIndex, training, testing, metricEntity);
					
					Instances trainingFeatureSelection = set[0];				
					testing = set[1];

					Evaluation evalNaiveBayes = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, naiveBayes, trainingFeatureSelection, testing, metricEntity);
					Evaluation evalIbk = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, ibk, trainingFeatureSelection, testing, metricEntity);	
					Evaluation evalRandomForest = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, randomForest, trainingFeatureSelection, testing, metricEntity);
						
					//%Training = training / total data
					float trainingInstance = training.numInstances();
					float totalInstance = trainingInstance + testing.numInstances();
					float percentageTraining = (trainingInstance / totalInstance) * 100;
					
					//let's calculate the metrics for each classifier
					metricController.calculateMetric(evalNaiveBayes, projectEntity, j + 1, "NaiveBayes", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting, 
							metricEntity, CSVResult);
					metricController.calculateMetric(evalIbk, projectEntity, j + 1, "IBk", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting, 
							metricEntity, CSVResult);
					metricController.calculateMetric(evalRandomForest, projectEntity, j + 1, "RandomForest", percentageTraining, compositionDefectiveTraining, compositionDefectiveTesting, 
							metricEntity, CSVResult);
				}
									
			}	
			
		}
			
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
	
}
