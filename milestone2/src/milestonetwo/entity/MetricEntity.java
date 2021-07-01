package milestonetwo.entity;

public class MetricEntity {
	
	private static String balancing;
	private static String featureSelection;
	private static String sensitivity; 
	private static float percentageMajorityClass;
	private static int [] compositionDefectiveTraining;
	private static int [] compositionDefectiveTesting;
	private static int[] compositionDefectiveTrainingToWrite;

	
	public String getBalancing() {
		return balancing;
	}
	public static void setBalancing(String balancing) {
		MetricEntity.balancing = balancing;
	}
	public String getFeatureSelection() {
		return featureSelection;
	}
	public static void setFeatureSelection(String featureSelection) {
		MetricEntity.featureSelection = featureSelection;
	}
	public String getSensitivity() {
		return sensitivity;
	}
	public static void setSensitivity(String sensitivity) {
		MetricEntity.sensitivity = sensitivity;
	}
	public float getPercentageMajorityClass() {
		return percentageMajorityClass;
	}
	public static void setPercentageMajorityClass(float percentageMajorityClass) {
		MetricEntity.percentageMajorityClass = percentageMajorityClass;
	}
	public static int [] getCompositionDefectiveTraining() {
		return compositionDefectiveTraining;
	}
	public static void setCompositionDefectiveTraining(int [] compositionDefectiveTraining) {
		MetricEntity.compositionDefectiveTraining = compositionDefectiveTraining;
	}
	public static int [] getCompositionDefectiveTesting() {
		return compositionDefectiveTesting;
	}
	public static void setCompositionDefectiveTesting(int [] compositionDefectiveTesting) {
		MetricEntity.compositionDefectiveTesting = compositionDefectiveTesting;
	}
	
	public static int [] getCompositionDefectiveTrainingToWrite() {
		return compositionDefectiveTrainingToWrite;
	}
	public static void setCompositionDefectiveTrainingToWrite(int [] compositionDefectiveTrainingToWrite) {
		MetricEntity.compositionDefectiveTrainingToWrite = compositionDefectiveTrainingToWrite;
	}
	
	
	
	

}
