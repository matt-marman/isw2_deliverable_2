package milestonetwo.controller;

import java.io.FileWriter;
import java.io.IOException;

import milestonetwo.entity.MetricEntity;
import milestonetwo.entity.ProjectEntity;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

public class MetricController {
	
	/**
	 * @param eval
	 * @param projectEntity
	 * @param numberRelease
	 * @param classifierName
	 * @param percentageTraining
	 * @param compositionDefectiveTraining
	 * @param compositionDefectiveTesting
	 * @param metricEntity
	 * @param CSVResult
	 * @throws IOException
	 */
	
	public void calculateMetric(Evaluation eval, ProjectEntity projectEntity, 
										int numberRelease, 
										String classifierName, 
										MetricEntity metricEntity,
										FileWriter csvResult) {
		
		double tp = eval.numTruePositives(0);
		double tn = eval.numTrueNegatives(0);
		double fp = eval.numFalsePositives(0);
		double fn = eval.numFalseNegatives(0);
		
		int [] compositionDefectiveTraining = metricEntity.getCompositionDefectiveTraining();
		int [] compositionDefectiveTesting = metricEntity.getCompositionDefectiveTesting();
		
		float totalInstancesTraining = compositionDefectiveTraining[0] + compositionDefectiveTraining[1];
		float defectiveTraining = compositionDefectiveTraining[0] / totalInstancesTraining;
		float percentageDefectiveTraining = defectiveTraining * 100;
		
		float totalInstancesTesting = compositionDefectiveTesting[0] + compositionDefectiveTesting[1];
		float defectiveTesting = compositionDefectiveTesting[0] / totalInstancesTesting;
		float percentageDefectiveTesting = defectiveTesting * 100;
		
		float percentageTraining = totalInstancesTraining/(totalInstancesTraining + totalInstancesTesting) * 100;

		double precision = 0;
		
		if(fp != 0) precision = eval.precision(0);

		//write the result .csv file
		try {
			csvResult.append(projectEntity.getProjectName() + "," 
							+ numberRelease + "," 
							+ percentageTraining + "," 
							+ percentageDefectiveTraining + "," 
							+ percentageDefectiveTesting + "," 
							+ classifierName + "," 
							+ metricEntity.getBalancing() + "," 
							+ metricEntity.getFeatureSelection() + ","  
							+ metricEntity.getSensitivity() + "," 
							+ tp + "," + fp + "," + tn + ","  + fn + "," 
							+ precision + "," 
							+ eval.recall(0) +  "," 
							+ eval.areaUnderROC(0) + "," 
							+ eval.kappa() + 
							"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		try {
			csvResult.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param set
	 * @return
	 */
	
	public int[] calculateNumDefective(Instances set) {
		
		int numberDefective = 0;
		int numberNotDefective = 0;
		int[] total = {numberDefective, numberNotDefective};
		int numberInstances = 0;
		numberInstances = set.numInstances();
		
		int indexBugginess = 10;
		
		for (int k = 1; k < numberInstances; k++) {
			
			Instance currentInstances = set.instance(k);

			if(currentInstances.stringValue(indexBugginess).equals("true")) {
				
				numberDefective++;
			}
			else numberNotDefective++;
			
		}
		
		total[0] = numberDefective;
		total[1] = numberNotDefective;
	
		return total;
	}
	
	

}
