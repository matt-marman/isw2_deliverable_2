package milestoneone;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVController {
		
	/**
	 * @param baseFilePath
	 * @return
	 * @throws IOException
	 */
	
	public FileWriter initializeCSVResult(ProjectEntity projectEntity) throws IOException {
		
		FileWriter csvResult = new FileWriter(projectEntity.getName() + "_RESULT.csv");	
		
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
						"Bugginess," + 
						"\n");
				
		return csvResult;
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
