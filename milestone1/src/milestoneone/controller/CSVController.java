package milestoneone.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import milestoneone.entity.FileEntity;
import milestoneone.entity.ProjectEntity;

public class CSVController {
		
	private ProjectEntity projectEntity;
	private FileWriter csv;
	
	public CSVController(ProjectEntity projectEntity) {
		
		this.projectEntity = projectEntity;
		
		try {
			csv = initializeCSVResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createCSV();
		
		
	}
	
	public FileWriter initializeCSVResult() throws IOException {
		
		FileWriter csvResult = new FileWriter(projectEntity.getName() + "Metrics.csv");	
		
		csvResult.append("Version Number," + 
						"File Name," + 
						"LOC_Touched," + 
						"NumberRevisions," +
						"NumberBugFix," +
						"LOC_Added," + 
						"MAX_LOC_Added," + 
						"Chg_Set_Size," + 
						"Max_Chg_Set," + 
						"AVG_Chg_Set," + 
						"Avg_LOC_Added," + 
						"Bugginess" + 
						"\n");
				
		return csvResult;
	}

	public void createCSV() {
		
		for(int k = projectEntity.getFileEntityList().size() - 1; k >= 0; k--) {

			List<String> metric = new ArrayList<>();
					
			FileEntity currentFileEntity = projectEntity.getFileEntityList().get(k);

			int index = currentFileEntity.getIndexVersion();
			
			if(index < 0 || currentFileEntity.getIndexVersion() > projectEntity.getHalfVersion()) continue;
					
			metric.add(projectEntity.getVersion().get(index));
			metric.add(currentFileEntity.getFileName());
			
			metric.add(Integer.toString(currentFileEntity.getLocTouched()));
			metric.add(Integer.toString(currentFileEntity.getNumberRevisions()));
			metric.add(Integer.toString(currentFileEntity.getNumberBugFix()));
			metric.add(Integer.toString(currentFileEntity.getLocAdded()));
			metric.add(Integer.toString(currentFileEntity.getMaxLocAdded()));
			metric.add(Integer.toString(currentFileEntity.getChgSetSize()));
			metric.add(Integer.toString(currentFileEntity.getMaxChgSet()));
			metric.add(Float.toString(currentFileEntity.getAvgChgSet()));
			metric.add(Float.toString(currentFileEntity.getAvgLocAdded()));
			metric.add(Boolean.toString(currentFileEntity.getBuggy()));
				
			try {
				addRowToCSV(metric, csv);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		try {
			csv.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static void addRowToCSV(List<String> nextLine, FileWriter csv) throws IOException {
		
		StringBuilder stringToAppend = new StringBuilder();

		int c = 0;
		for(String token : nextLine) {
			
			if(c != 0) stringToAppend.append(",");
			stringToAppend.append(token);
			c++;

		}
						
		//add to file
		csv.append(stringToAppend + "\n");
					
	}
	
}
