package milestonetwo;

import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Evaluation;

public class MetricEntity {
	
	private static String balancing;
	private static String featureSelection;
	private static String sensitivity; 
	private static float percentageMajorityClass;
	private static int TP, FP, TN, FN;
	
	public static String getBalancing() {
		return balancing;
	}
	public static void setBalancing(String balancing) {
		MetricEntity.balancing = balancing;
	}
	public static String getFeatureSelection() {
		return featureSelection;
	}
	public static void setFeatureSelection(String featureSelection) {
		MetricEntity.featureSelection = featureSelection;
	}
	public static String getSensitivity() {
		return sensitivity;
	}
	public static void setSensitivity(String sensitivity) {
		MetricEntity.sensitivity = sensitivity;
	}
	public static float getPercentageMajorityClass() {
		return percentageMajorityClass;
	}
	public static void setPercentageMajorityClass(float percentageMajorityClass) {
		MetricEntity.percentageMajorityClass = percentageMajorityClass;
	}
	
	

}
