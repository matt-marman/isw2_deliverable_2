package milestone1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections4.MapIterator;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

public class CSVController {
	

	@SuppressWarnings("rawtypes")
	public CSVController(String projectName, float lastVersion, MultiKeyMap fileMapDataset) {

		try (FileWriter csvWriter = new FileWriter(projectName + ".csv")) {

			csvWriter.append("Version Number");
			csvWriter.append(",");
			csvWriter.append("File Name");
			csvWriter.append(",");
			csvWriter.append("LOC_Touched");
			csvWriter.append(",");
			csvWriter.append("NumberRevisions");
			csvWriter.append(",");
			csvWriter.append("NumberBugFix");
			csvWriter.append(",");
			csvWriter.append("LOC_Added");
			csvWriter.append(",");
			csvWriter.append("MAX_LOC_Added");
			csvWriter.append(",");
			csvWriter.append("Chg_Set_Size");
			csvWriter.append(",");
			csvWriter.append("Max_Chg_Set");
			csvWriter.append(",");
			csvWriter.append("AVG_Chg_Set");
			csvWriter.append(",");
			csvWriter.append("Avg_LOC_Added");
			csvWriter.append(",");
			csvWriter.append("Bugginess");
			csvWriter.append("\n");

			Map<String, List<Integer>> monthMap = new TreeMap<>();
			String buggy;
			int avgLOCAdded;
			int avgChgSet;
			MapIterator dataSetIterator = fileMapDataset.mapIterator();

			// Iterate over the dataset
			while (dataSetIterator.hasNext()) {
				dataSetIterator.next();
				MultiKey key = (MultiKey) dataSetIterator.getKey();

				// Get the metrics list associated to the multikey
				@SuppressWarnings("unchecked")
				ArrayList<Integer> fileMetrics = (ArrayList<Integer>) fileMapDataset.get(key.getKey(0), key.getKey(1));

				monthMap.put(String.valueOf(key.getKey(0)) + "," + (String)key.getKey(1), fileMetrics);
			}

			for (Map.Entry<String, List<Integer>> entry : monthMap.entrySet()) {

				ArrayList<Integer> fileMetrics = (ArrayList<Integer>) entry.getValue();
				// Check that the version index is contained in the first half of the releases
				if (fileMetrics.get(9).equals(0)) buggy = "No";
				else buggy = "Yes";

				if (fileMetrics.get(1).equals(0)) {
					avgLOCAdded = 0;
					avgChgSet = 0;
				} else {
					avgLOCAdded = fileMetrics.get(5)/fileMetrics.get(1);
					avgChgSet = fileMetrics.get(3)/fileMetrics.get(1);
				}

				// Append the data to CSV file
				csvWriter.append(entry.getKey().split(",")[0] + "," + entry.getKey().split(",")[1] + "," + fileMetrics.get(0) + "," + fileMetrics.get(1) + ","
						+ fileMetrics.get(2) + "," + fileMetrics.get(3) + "," + fileMetrics.get(4) + "," + fileMetrics.get(5) + ","
						+ fileMetrics.get(6) + "," + avgLOCAdded + "," + avgChgSet + "," + buggy);

				csvWriter.append("\n");
			}

			csvWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
