package milestonetwo.controller;

import java.io.FileWriter;
import java.io.IOException;

import milestonetwo.entity.MetricEntity;
import milestonetwo.entity.ProjectEntity;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

public class MetricController {
	
	/*
	 * Precision, Recall, AUC, Kappa. 
	 */
	public void calculateMetric(Evaluation eval, ProjectEntity projectEntity, int numberRelease, String classifierName, 
										float percentageTraining, int [] compositionDefectiveTraining, int [] compositionDefectiveTesting,
										MetricEntity metricEntity,
										FileWriter CSVResult) throws IOException {
		
		double TP = eval.numTruePositives(0);
		double TN = eval.numTrueNegatives(0);
		double FP = eval.numFalsePositives(0);
		double FN = eval.numFalseNegatives(0);
		
		float totalInstancesTraining = compositionDefectiveTraining[0] + compositionDefectiveTraining[1];
		float DefectiveTraining = compositionDefectiveTraining[0] / totalInstancesTraining;
		float percentageDefectiveTraining = DefectiveTraining * 100;
		
		float totalInstancesTesting = compositionDefectiveTesting[0] + compositionDefectiveTesting[1];
		float DefectiveTesting = compositionDefectiveTesting[0] / totalInstancesTesting;
		float percentageDefectiveTesting = DefectiveTesting * 100;
		
		if(numberRelease == 6 && metricEntity.getBalancing() == "No Balancing") {
			
			metricEntity.setPercentageMajorityClass(((compositionDefectiveTraining[0] + compositionDefectiveTesting[0])/
											totalInstancesTraining + totalInstancesTesting) * 100);
			
			
		}
		
		
		System.out.println("\n" + classifierName);
		System.out.println("Precision = " + eval.precision(0));
		System.out.println("Recall = " + eval.recall(0));
		System.out.println("AUC = " + eval.areaUnderROC(0));
		System.out.println("kappa = " + eval.kappa());
		System.out.println("%Training = " + percentageTraining);
		System.out.println("%DefectiveTraining = " + percentageDefectiveTraining);
		System.out.println("%DefectiveTesting = " + percentageDefectiveTesting);
		
		System.out.println("Balancing = " + metricEntity.getBalancing());
		System.out.println("Feature Selection = " + metricEntity.getFeatureSelection());
		System.out.println("Sensitivity = " + metricEntity.getSensitivity());
		
		System.out.println("TP = " + TP);
		System.out.println("FP = " + FP);
		System.out.println("TN = " + TN);
		System.out.println("FN = " + FN);
		
		//write the result .csv file
		CSVResult.append(projectEntity.getProjectName() + "," 
						+ numberRelease + "," 
						+ percentageTraining + "," 
						+ percentageDefectiveTraining + "," 
						+ percentageDefectiveTesting + "," 
						+ classifierName + "," 
						+ metricEntity.getBalancing() + "," 
						+ metricEntity.getFeatureSelection() + ","  
						+ metricEntity.getSensitivity() + "," 
						+ TP + "," + FP + "," + TN + ","  + FN + "," 
						+ eval.precision(0) + "," 
						+ eval.recall(0) +  "," 
						+ eval.areaUnderROC(0) + "," 
						+ eval.kappa() + 
						"\n");
	
		CSVResult.flush();
	}
	
	public int[] calculateNumDefective(Instances set, MetricEntity metricEntity) {
		
		int numberDefective = 0;
		int numberNotDefective = 0;
		int[] total = {numberDefective, numberNotDefective};
		int numberInstances = 0;
		numberInstances = set.numInstances();
		
		int indexBugginess = 10;
		//if(metricEntity.getBalancing() != "No Sampling") indexBugginess--;
		
		for (int k = 1; k < numberInstances; k++) {
			
			Instance currentInstances = set.instance(k);

			if(currentInstances.stringValue(indexBugginess).equals("Yes")) {
				
				numberDefective++;
			}
			else numberNotDefective++;
			
		}
		
		total[0] = numberDefective;
		total[1] = numberNotDefective;
	
		return total;
	}
	
	

}
