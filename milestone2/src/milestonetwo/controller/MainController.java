package milestonetwo.controller;

import weka.core.Attribute;
import weka.core.Instance;

import weka.core.Instances;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import milestonetwo.entity.MetricEntity;
import milestonetwo.entity.ProjectEntity;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.lazy.IBk;

/**
 * The aim of this project is to provide a .csv file 
 * that contains all combinations for:
 * 
 * No selection / best first as feature selection
 * No sampling / oversampling / undersampling / SMOTE as balancing
 * No cost sensitive / Sensitive Threshold / Sensitive Learning (CFN = 10 * CFP)
 * RandomForest / NaiveBayes / Ibk as classifier
 * 
 * The projects taken are Bookkeeper and Syncope.
 * 
 * @author Mattia Di Battista
 * 
 * My acknowledge to @author Dr Noureddin M. Sadawi (noureddin.sadawi@gmail.com)
 *
 */

public class MainController{
	
	/**
	 * false = Bookkeeper project
	 * true = Syncope project
	 */
	
	private static boolean projectSelection = true;
		
	private static FileWriter csvResult;
	
	private static String extensionArff = ".arff";
	private static String extensionCSV = ".csv";
	
	private static int numberRelease;

	public static void main(String[] args) throws Exception{
			    
		String pathFile = System.getProperty("user.dir");		
	    
		ProjectEntity projectEntity = new ProjectEntity(); 
		
		//set attributes for project
		if(projectSelection) {
			
			projectEntity.setBaseFilePath(pathFile + "/syncopeFile/SYNCOPE");
			projectEntity.setFileARFF(pathFile + "/syncopeFile/SYNCOPE.arff");
			projectEntity.setFileCSV(pathFile + "/syncopeFile/SYNCOPE.csv");
			projectEntity.setFirstRelease("1.0.0-incubating");
			projectEntity.setProjectName("SYNCOPE");
						
		}else {
			
			projectEntity.setBaseFilePath(pathFile + "/bookkeeperFile/BOOKKEEPER");
			projectEntity.setFileARFF(pathFile + "/bookkeeperFile/BOOKKEEPER.arff");
			projectEntity.setFileCSV(pathFile + "/bookkeeperFile/BOOKKEEPER.csv");
			projectEntity.setFirstRelease("4.0.0");
			projectEntity.setProjectName("BOOKKEEPER");
	
		}
		
		CSVController csvController = new CSVController();
		
		//create CSVResult.csv. It is populating with all data
		csvResult = csvController.initializeCSVResult(projectEntity.getBaseFilePath());
		
		MetricEntity metricEntity = new MetricEntity(); 
		
		ArrayList<String> fileArffList = new ArrayList<>();
		ArrayList<String> fileCSVList = new ArrayList<>();
		
		String[] paramsCSVController = {projectEntity.getFileCSV(), projectEntity.getFileARFF()};
		csvController.csvConverter(paramsCSVController);
		
		DataSource source = new DataSource(projectEntity.getFileARFF());
		int numberFeature = source.getDataSet().numAttributes();
		Attribute versions = source.getDataSet().attribute(0);
		numberRelease = versions.numValues();

		//convert each .csv file generated		
		for(int version = 0; version < numberRelease; version++) {
						
			String currentCSVFile = projectEntity.getBaseFilePath();
			currentCSVFile += String.valueOf(version + 1);
			currentCSVFile += extensionCSV;
			fileCSVList.add(currentCSVFile);
						
			String currentArffFile = projectEntity.getBaseFilePath();
			currentArffFile += String.valueOf(version + 1);
			currentArffFile += extensionArff;
			fileArffList.add(currentArffFile);
				
		}
			
				
		csvController.splitCSV(projectEntity.getFileCSV(), fileCSVList, projectEntity.getFirstRelease(), numberFeature);	

		for(int j = 0; j < numberRelease; j++) {
					
			paramsCSVController[0] = fileCSVList.get(j);
			paramsCSVController[1] = fileArffList.get(j);
			
			csvController.csvConverter(paramsCSVController);
					
		}
					
		walkForward(fileArffList, projectEntity, metricEntity);
				
	}
		
	
	/**
	 * @param releaseFileArffList
	 * @param projectEntity
	 * @param metricEntity
	 * @throws Exception
	 */
	
	private static void walkForward(ArrayList<String> releaseFileArffList, 
									ProjectEntity projectEntity, MetricEntity metricEntity) {
		
		Instances training = null;
		DataSource sourceTraining = null;
		
		for(int j = 0; j < numberRelease - 1; j++) {
				
			//merge instances
			if(j != 0) {
					
				training = mergeInstancesPrepare(releaseFileArffList.get(j), projectEntity, training);
				
			}else {
					
				//first iteration, only one set for training and another one for testing
				try {
					sourceTraining = new DataSource(releaseFileArffList.get(j));
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					training = sourceTraining.getDataSet();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
					
			//create testing set	
			DataSource sourceTesting = null;
			try {
				sourceTesting = new DataSource(releaseFileArffList.get(j + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Instances testing = null;
			try {
				testing = sourceTesting.getDataSet();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
				
			int numAttr = training.numAttributes();
				
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
				
			applyCombination(testing, training, j, projectEntity, metricEntity);
								
		}		
			
	}
	
	/**
	 * @param fileArff
	 * @param projectEntity
	 * @param training
	 * @return
	 */
	private static Instances mergeInstancesPrepare(String fileArff, ProjectEntity projectEntity, Instances training) {
		
		DataSource newSourceTraining = null;
		try {
			newSourceTraining = new DataSource(fileArff);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Instances newTraining = null;
		try {
			newTraining = newSourceTraining.getDataSet();
		} catch (Exception e) {
			e.printStackTrace();
		}	
			
		String releaseNewTraining = null;
		try {
			
			releaseNewTraining = newSourceTraining.getDataSet().attribute(0).value(0).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		//change the version number
		//for merge the two set
		newTraining.renameAttributeValue(
				newTraining.attribute("Version Number"),
				releaseNewTraining,
				projectEntity.getFirstRelease());
					
		return mergeInstances(training, newTraining);
	}
		
	/**
	 * @param testing
	 * @param training
	 * @param lastRealese
	 * @param projectEntity
	 * @param metricEntity
	 */
	private static void applyCombination(Instances testing, Instances training, int lastRealese, ProjectEntity projectEntity, MetricEntity metricEntity) {
		
		//use RandomForest, NaiveBayes, Ibk as classifiers
		NaiveBayes naiveBayes = new NaiveBayes();
		IBk ibk = new IBk();
		RandomForest randomForest = new RandomForest();
		
		MetricController metricController = new MetricController();	
		int [] compositionDefectiveTraining = compositionDefectiveTraining = metricController.calculateNumDefective(training);
		metricEntity.setCompositionDefectiveTrainingToWrite(compositionDefectiveTraining);

		//use balancing
		for (int balancingSelectionIndex = 0; balancingSelectionIndex < 4; balancingSelectionIndex++) {
				
			BalancingController balancingController = new BalancingController();
			
			/*
			 * metricEntity.setPercentageMajorityClass method is for oversampling 
			 * if there are more defective than not defective 
			 * set the PercentageMajorityClass on defective
			 */	
			
			compositionDefectiveTraining = metricController.calculateNumDefective(training);
			
			float percentageDefectiveTraining = (compositionDefectiveTraining[0] / (float)training.numInstances()) * 100;
			
			if(compositionDefectiveTraining[0] > compositionDefectiveTraining[1]) {
				
				metricEntity.setPercentageMajorityClass(percentageDefectiveTraining);
			}
				
			else metricEntity.setPercentageMajorityClass(100 - percentageDefectiveTraining);

			Instances trainingSetBalancing = balancingController.applyBalancing(training, balancingSelectionIndex, metricEntity);
						
			//calculate %Defective in training and testing set
			compositionDefectiveTraining = metricController.calculateNumDefective(trainingSetBalancing);
			int [] compositionDefectiveTesting = metricController.calculateNumDefective(testing);
					
			metricEntity.setCompositionDefectiveTesting(compositionDefectiveTesting);
			metricEntity.setCompositionDefectiveTraining(compositionDefectiveTraining);
			
			//use feature selection
			for (int featureSelectionIndex = 0; featureSelectionIndex < 2; featureSelectionIndex++) {
					
				FeatureSelectionController featureSelectionController = new FeatureSelectionController();
				List<Instances> set = new ArrayList<>();

				try {
					set = featureSelectionController.applyFeatureSelection(featureSelectionIndex, trainingSetBalancing, testing, metricEntity);
				} catch (Exception e) {
					e.printStackTrace();
				}
					
				Instances trainingFeatureSelection = set.get(0);	
				Instances testingFeatureSelection = set.get(1);

				//use sensitive cost classifier
				for (int sensitiveSelectionIndex = 0; sensitiveSelectionIndex < 3; sensitiveSelectionIndex++) {

					SensitiveSelectionController sensitiveSelectionController = new SensitiveSelectionController();
						
					Evaluation evalNaiveBayes = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, naiveBayes, trainingFeatureSelection, testingFeatureSelection, metricEntity);
					Evaluation evalIbk = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, ibk, trainingFeatureSelection, testingFeatureSelection, metricEntity);	
					Evaluation evalRandomForest = sensitiveSelectionController.applySensitiveSelection(sensitiveSelectionIndex, randomForest, trainingFeatureSelection, testingFeatureSelection, metricEntity);
							
					//let's calculate the metrics for each classifier
					metricController.calculateMetric(evalNaiveBayes, projectEntity, lastRealese + 1, "NaiveBayes", metricEntity, csvResult);
					metricController.calculateMetric(evalIbk, projectEntity, lastRealese + 1, "IBk", metricEntity, csvResult);
					metricController.calculateMetric(evalRandomForest, projectEntity, lastRealese + 1, "RandomForest", metricEntity, csvResult);
				}
			}
		}
	}
	
	/**
	 * 
	 * Please note that the following conditions should hold (there are not checked in the function):
	 * 
	 * Datasets must have the same attributes structure (number of attributes, type of attributes)
	 * Class index has to be the same
	 * Nominal values have to exactly correspond
	 *
	 * @param data1
	 * @param data2
	 * @return
	 * @throws Exception
	 */
	
	private static Instances mergeInstances(Instances data1, Instances data2){
		
		    // Check where are the string attributes
		    int asize = data1.numAttributes();
		    boolean[] stringsPos = new boolean[asize];
		    for(int i=0; i<asize; i++){
		    	
		        Attribute att = data1.attribute(i);
		        stringsPos[i] = ((att.type() == Attribute.STRING) ||
		                          (att.type() == Attribute.NOMINAL));
		    }

		    // Create a new dataset
		    Instances dest = new Instances(data1);
		    dest.setRelationName(data1.relationName() + "+" + data2.relationName());

		    DataSource source = new DataSource(data2);
		    Instances instances = null;
			try {
				instances = source.getStructure();
			} catch (Exception e) {
				e.printStackTrace();
			}
		    Instance instance = null;
		    while (source.hasMoreElements(instances)) {
		        instance = source.nextElement(instances);
		        dest.add(instance);

		        // Copy string attributes
		        for(int i=0; i<asize; i++) {
		            if(stringsPos[i]) {
		                dest.instance(dest.numInstances()-1)
		                    .setValue(i,instance.stringValue(i));
		            }
		        }
		    }

		    return dest;
		}
	
}
