package milestonetwo.controller;

import milestonetwo.entity.MetricEntity;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

/**
 * This class applies:
 * 
 * No cost sensitive
 * Sensitive Threshold (CFN = 10 * CFP)
 * Sensitive Learning (CFN = 10 * CFP)
 * 
 * @author Mattia Di Battista
 *
 */
public class SensitiveSelectionController {

	private MetricEntity metricEntity;
	private Classifier classifier;
	private Instances trainingSet;
	private Instances testingSet;
	
	/**
	 * @param sensitiveSelectionIndex
	 * @param classifier
	 * @param trainingSet
	 * @param testingSet
	 * @param metricEntity
	 * @return
	 * @throws Exception
	 */
	
	public Evaluation applySensitiveSelection(int sensitiveSelectionIndex, Classifier classifier, 
												Instances trainingSet, Instances testingSet, 
												MetricEntity metricEntity) throws Exception {
		
		this.metricEntity = metricEntity;
		this.classifier = classifier;
		this.testingSet = testingSet;
		this.trainingSet = trainingSet;
		
		if(sensitiveSelectionIndex == 0) return applyNoSensitive();
		
		if(sensitiveSelectionIndex == 1) return applySensitiveThreshold();
		
		if(sensitiveSelectionIndex == 2) return applySensitiveLearning();
		
		return null;
		
	}
	
	private Evaluation applyNoSensitive() throws Exception {
		
		this.metricEntity.setSensitivity("No Cost Sensitive");

		classifier.buildClassifier(trainingSet);
		Evaluation evaluation = new Evaluation(testingSet);	
		evaluation.evaluateModel(classifier, testingSet); 		
		
		return evaluation;
	}
	
	
	private Evaluation applySensitiveThreshold() throws Exception {
		
		metricEntity.setSensitivity("Sensitive Threshold");

		CostMatrix costMatrix = this.calculateMatrix();
		
		CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(costMatrix);

		costSensitiveClassifier.setMinimizeExpectedCost(true);
		
		costSensitiveClassifier.buildClassifier(trainingSet);
		
		Evaluation evaluation = new Evaluation(testingSet, costMatrix);		
		evaluation.evaluateModel(costSensitiveClassifier, testingSet);
		
		return evaluation;
		
	}
	
	
	private Evaluation applySensitiveLearning() throws Exception {
				
		metricEntity.setSensitivity("Sensitive Learning");
		
		CostMatrix costMatrix = this.calculateMatrix();
				
		CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(costMatrix);
		
		costSensitiveClassifier.setMinimizeExpectedCost(false);
		
		costSensitiveClassifier.buildClassifier(trainingSet);
		
		Evaluation evaluation = new Evaluation(testingSet, costMatrix);		
		evaluation.evaluateModel(costSensitiveClassifier, testingSet);
		
		return evaluation;
	}
	
	private CostMatrix calculateMatrix() {
		
		CostMatrix costMatrix = new CostMatrix(2);
		
		double CFP = 1;
		double CFN = 10 * CFP;
		
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, CFN);
		costMatrix.setCell(1, 0, CFP);
		costMatrix.setCell(1, 1, 0.0);
		
		return costMatrix;
	}
	
	/**
	 * @param costMatrix
	 * @return
	 */
	private CostSensitiveClassifier applyCostSensitive(CostMatrix costMatrix) {
		
		CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
		costSensitiveClassifier.setClassifier(classifier);
		costSensitiveClassifier.setCostMatrix(costMatrix);
		return costSensitiveClassifier;
		
	}
}
