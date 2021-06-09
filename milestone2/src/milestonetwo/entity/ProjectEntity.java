package milestonetwo.entity;

public class ProjectEntity {

	private static String fileCSV = "";
	private static String fileARFF = "";
	private static String baseFilePath = "";
	private static String firstRelease = "";
	private static String projectName = "";

	public String getFileCSV() {
		return fileCSV;
	}
	public static void setFileCSV(String fileCSV) {
		ProjectEntity.fileCSV = fileCSV;
	}
	public String getFileARFF() {
		return fileARFF;
	}
	public static void setFileARFF(String fileARFF) {
		ProjectEntity.fileARFF = fileARFF;
	}
	public String getBaseFilePath() {
		return baseFilePath;
	}
	public static void setBaseFilePath(String baseFilePath) {
		ProjectEntity.baseFilePath = baseFilePath;
	}
	public String getFirstRelease() {
		return firstRelease;
	}
	public static void setFirstRelease(String firstRelease) {
		ProjectEntity.firstRelease = firstRelease;
	}
	public String getProjectName() {
		return projectName;
	}
	public static void setProjectName(String projectName) {
		ProjectEntity.projectName = projectName;
	}
	
	
}
