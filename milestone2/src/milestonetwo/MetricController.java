package milestonetwo;

import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

public class MetricController {
	
	public int[] calculateNumDefective(Instances set) {
		
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
	public void calculateMetric(Evaluation eval, ProjectEntity projectEntity, int numberRelease, String classifierName, 
										float percentageTraining, int [] compositionDefectiveTraining, int [] compositionDefectiveTesting,
										String balancing, String featureSelection, String sensitivity, int TP, int FP, int TN, int FN,
										FileWriter CSVResult) throws IOException {
		
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
		
		System.out.println("Balancing = " + balancing);
		System.out.println("Feature Selection = " + featureSelection);
		System.out.println("Sensitivity = " + sensitivity);
		
		System.out.println("TP = " + TP);
		System.out.println("FP = " + FP);
		System.out.println("TN = " + TN);
		System.out.println("FN = " + FN);
		
		//write the result .csv file
		CSVResult.append(projectEntity.getProjectName() + "," + numberRelease + "," + percentageTraining + "," +
						percentageDefectiveTraining + "," + percentageDefectiveTesting + "," +
						classifierName + "," + balancing + "," + featureSelection + ","  + sensitivity + "," +
						TP + "," + FP + "," + TN + ","  + FN + 
						eval.precision(0) + "," + eval.recall(0) +  "," + 
						eval.areaUnderROC(1) + "," + eval.kappa() + "\n");
	
		CSVResult.flush();
	}
	
	

}
