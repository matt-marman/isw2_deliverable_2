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
												MetricEntity metricEntity) {
		
		this.metricEntity = metricEntity;
		this.classifier = classifier;
		this.testingSet = testingSet;
		this.trainingSet = trainingSet;
		
		if(sensitiveSelectionIndex == 0) return applyNoSensitive();
		
		if(sensitiveSelectionIndex == 1) return applySensitiveThreshold();
		
		if(sensitiveSelectionIndex == 2) return applySensitiveLearning();
		
		return null;
		
	}
	
	private Evaluation applyNoSensitive() {
		
		this.metricEntity.setSensitivity("No Cost Sensitive");

		try {
			classifier.buildClassifier(trainingSet);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		Evaluation evaluation = null;
		try {
			evaluation = new Evaluation(testingSet);
		} catch (Exception e1) {
			e1.printStackTrace();
		}	
		try {
			evaluation.evaluateModel(classifier, testingSet);
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		
		return evaluation;
	}
	
	
	private Evaluation applySensitiveThreshold() {
		
		metricEntity.setSensitivity("Sensitive Threshold");

		CostMatrix costMatrix = this.calculateMatrix();
		
		CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(costMatrix);

		costSensitiveClassifier.setMinimizeExpectedCost(true);
		
		return applyEvaluation(costSensitiveClassifier, costMatrix);
		
	}
	
	
	private Evaluation applySensitiveLearning() {
				
		metricEntity.setSensitivity("Sensitive Learning");
		
		CostMatrix costMatrix = this.calculateMatrix();
				
		CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(costMatrix);
		
		costSensitiveClassifier.setMinimizeExpectedCost(false);
		
		return applyEvaluation(costSensitiveClassifier, costMatrix);
	}
	
	private Evaluation applyEvaluation(CostSensitiveClassifier costSensitiveClassifier, CostMatrix costMatrix) {
		
		try {
			costSensitiveClassifier.buildClassifier(trainingSet);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		Evaluation evaluation = null;
		try {
			evaluation = new Evaluation(testingSet, costMatrix);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
		try {
			evaluation.evaluateModel(costSensitiveClassifier, testingSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return evaluation;
		
		
	}
	
	
	private CostMatrix calculateMatrix() {
		
		CostMatrix costMatrix = new CostMatrix(2);
		
		double cfp = 1;
		double cfn = 10 * cfp;
		
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, cfn);
		costMatrix.setCell(1, 0, cfp);
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
