package milestoneone;

public class FileEntity {

	private String fileName;
	private String version;
	private int indexVersion;
	
	/*	
	 * Metrics Data Structure
	 *  0 - LOC_Touched
	 *  1 - NumberRevisions
	 *  2 - NumberBugFix
	 *  3 - LOC_Added
	 *  4 - MAX_LOC_Added
	 *  5 - Chg_Set_Size
	 *  6 - Max_Chg_Set
	 *  7 - Avg_Chg_Set
	 *  8 - AVG_LOC_Added
	 * 
	 * */
	
	private int locTouched;
	private int numberRevisions;
	private int numberBugFix;
	private int locAdded;
	private int maxLocAdded;
	private int chgSetSize;
	private int maxChgSet;
	private float avgChgSet;
	private float avgLocAdded;
	private boolean buggy;
	private int attributes = 13;
	
	public FileEntity() {
		
		this.locTouched = 0;
		this.numberRevisions = 0;
		this.numberBugFix = 0;
		this.locAdded = 0;
		this.maxLocAdded = 0;
		this.chgSetSize = 0;
		this.maxChgSet = 0;
		this.avgChgSet = 0;
		this.avgLocAdded = 0;
		this.buggy = false;
		
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getLocTouched() {
		return locTouched;
	}
	public void setLocTouched(int locTouched) {
		this.locTouched = locTouched;
	}
	public int getNumberRevisions() {
		return numberRevisions;
	}
	public void setNumberRevisions(int numberRevisions) {
		this.numberRevisions = numberRevisions;
	}
	public int getNumberBugFix() {
		return numberBugFix;
	}
	public void setNumberBugFix(int numberBugFix) {
		this.numberBugFix = numberBugFix;
	}
	public int getLocAdded() {
		return locAdded;
	}
	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}
	public int getMaxLocAdded() {
		return maxLocAdded;
	}
	public void setMaxLocAdded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}
	public int getChgSetSize() {
		return chgSetSize;
	}
	public void setChgSetSize(int chgSetSize) {
		this.chgSetSize = chgSetSize;
	}
	public int getMaxChgSet() {
		return maxChgSet;
	}
	public void setMaxChgSet(int maxChgSet) {
		this.maxChgSet = maxChgSet;
	}
	public float getAvgChgSet() {
		return avgChgSet;
	}
	public void setAvgChgSet(float avgChgSet) {
		this.avgChgSet = avgChgSet;
	}
	public float getAvgLocAdded() {
		return avgLocAdded;
	}
	public void setAvgLocAdded(float avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}
	public int getIndexVersion() {
		return indexVersion;
	}
	public void setIndexVersion(int indexVersion) {
		this.indexVersion = indexVersion;
	}
	public boolean getBuggy() {
		return buggy;
	}
	public void setBuggy(boolean buggy) {
		this.buggy = buggy;
	}
	public int getAttributes() {
		return attributes;
	}
	public void setAttributes(int attributes) {
		this.attributes = attributes;
	} 
}
