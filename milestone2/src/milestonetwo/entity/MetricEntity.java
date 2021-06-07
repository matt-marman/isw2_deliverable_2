package milestonetwo.entity;

public class MetricEntity {
	
	private static String balancing;
	private static String featureSelection;
	private static String sensitivity; 
	private static float percentageMajorityClass;
	
	public String getBalancing() {
		return balancing;
	}
	public void setBalancing(String balancing) {
		MetricEntity.balancing = balancing;
	}
	public String getFeatureSelection() {
		return featureSelection;
	}
	public void setFeatureSelection(String featureSelection) {
		MetricEntity.featureSelection = featureSelection;
	}
	public String getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(String sensitivity) {
		MetricEntity.sensitivity = sensitivity;
	}
	public float getPercentageMajorityClass() {
		return percentageMajorityClass;
	}
	public void setPercentageMajorityClass(float percentageMajorityClass) {
		MetricEntity.percentageMajorityClass = percentageMajorityClass;
	}
	
	

}
