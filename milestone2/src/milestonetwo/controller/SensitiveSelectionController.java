package milestonetwo.controller;

import milestonetwo.entity.MetricEntity;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

public class SensitiveSelectionController {

	public Evaluation applySensitiveSelection(int sensitiveSelectionIndex, Classifier classifier, 
												Instances trainingSet, Instances testingSet, MetricEntity metricEntity) throws Exception {
		
		
		if(sensitiveSelectionIndex == 0) return applyNoSensitive(classifier, trainingSet, testingSet, metricEntity);
		
		if(sensitiveSelectionIndex == 2) return null;
		
		if(sensitiveSelectionIndex == 1) return applySensitiveLearning(classifier, trainingSet, testingSet, metricEntity);
		
		return null;
		
	}
	
	//No cost sensitive / Sensitive Threshold / Sensitive Learning (CFN = 10 * CFP)
	public Evaluation applyNoSensitive(Classifier classifier, Instances trainingSet, Instances testingSet, MetricEntity metricEntity) throws Exception {
		
		metricEntity.setSensitivity("No Cost Sensitive");
		
		classifier.buildClassifier(trainingSet);
		Evaluation evaluation = new Evaluation(testingSet);	
		evaluation.evaluateModel(classifier, testingSet); 		
		
		return evaluation;
	}
	
	
	public void applySensitiveThreshold(Classifier classifier, Instances trainingSet, Instances testingSet, MetricEntity metricEntity) {
		
		metricEntity.setSensitivity("Sensitive Threshold");
		
		
		
		return;
	}
	
	
	public Evaluation applySensitiveLearning(Classifier classifier, Instances trainingSet, Instances testingSet, MetricEntity metricEntity) throws Exception {
				
		metricEntity.setSensitivity("Sensitive Learning");
		
		CostMatrix costMatrix = new CostMatrix(2);
		
		double CFP = 1;
		double CFN = 10 * CFP;
		
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, CFN);
		costMatrix.setCell(1, 0, CFP);
		costMatrix.setCell(1, 1, 0.0);
				
		CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
		costSensitiveClassifier.setClassifier(classifier);
		costSensitiveClassifier.setCostMatrix(costMatrix);
		
		costSensitiveClassifier.buildClassifier(trainingSet);
		
		Evaluation evaluation = new Evaluation(testingSet, costMatrix);		
		evaluation.evaluateModel(costSensitiveClassifier, testingSet);
		
		return evaluation;
	}
}
