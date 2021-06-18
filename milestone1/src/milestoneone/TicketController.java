package milestoneone;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class TicketController {

	//Multimap<ReleaseDate, VersionName, VersionIndex>
	private  Multimap<LocalDate, String> versionListWithDateAndIndex;

	// Map<ticketID, (OV, FV)>
	private  Multimap<Integer, Double> ticketWithProportion = MultimapBuilder.treeKeys().linkedListValues().build();

	// Map<ticketID, (IV, FV)>
	private  Map<Integer, List<Integer>> ticketWithBuggyIndex;

	private static final String RELEASE_DATE = "releaseDate";
	private static ProjectEntity projectEntity;
	
	public TicketController(ProjectEntity projectEntity, Multimap<LocalDate, String> versionListWithDate, Map<Integer, List<Integer>> ticketWithBuggyIndex, List <TicketEntity> ticketList) {
		
		this.projectEntity = projectEntity;
		this.versionListWithDateAndIndex = versionListWithDate;
		this.ticketWithBuggyIndex = ticketWithBuggyIndex;
		
	}

	public void getJsonAffectedVersionList(JSONArray json, TicketEntity ticketEntity) throws JSONException{

		if (json.length() > 0) {

			// For each release in the AV version...
			for (int k = 0; k < json.length(); k++) {

				JSONObject singleRelease = json.getJSONObject(k);

				// ... check if the single release has been released
				if (singleRelease.has(RELEASE_DATE)) {
					ticketEntity.addAv(singleRelease.getString("name"));
				}
			}
		}
	}

	public List<TicketEntity> getTicketAssociatedCommitBuggy(String commitMessage, String projectName) {
		
		List<TicketEntity> resultList = new ArrayList<>();
		Pattern pattern = null;
		Matcher matcher = null;

		for (Map.Entry<Integer,List<Integer>> entry : ticketWithBuggyIndex.entrySet()) {
				
			// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
			pattern = Pattern.compile("\\b"+ projectName + "-" + entry.getKey() + "\\b", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(commitMessage);

			// Check if commit message contains the issues ID and the issues is labeled like "not checked"
			if (matcher.find() && !resultList.contains(entry.getKey())) {
			
				TicketEntity ticketEntity = new TicketEntity();
				ticketEntity.setIvIndex(ticketWithBuggyIndex.get(entry.getKey()).get(0));
				ticketEntity.setFvIndex(ticketWithBuggyIndex.get(entry.getKey()).get(1));
				ticketEntity.setId(entry.getKey());
		
				resultList.add(ticketEntity);
			}
		}
		
		return resultList;
	}

	public void getMetrics (CommitEntity commitEntity, DiffEntry entry, DiffFormatter diffFormatter, int limitVersion) throws IOException{

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

		FileEntity fileEntity = removeFileEntity(commitEntity.getAppartainVersion(), entry.getNewPath());
		
		if(fileEntity == null) {
			
			fileEntity = new FileEntity(); 
			fileEntity.setFileName(entry.getNewPath());
			fileEntity.setIndexVersion(commitEntity.getAppartainVersion());
			
		}
		
		// Check if the appartaining version of the file is less than the upper bound
		if (commitEntity.getAppartainVersion() < limitVersion) {
			
			int locTouched = 0;
			int locAdded = 0;
			int chgSetSize = 0;

			// Get the total number of file committed
			chgSetSize = commitEntity.getFilesChanged().size();

			// For each edit made to the file...
			for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {

				// Check the type of the edit and increment the corresponding variable
				if (edit.getType() == Edit.Type.INSERT) {
					
					locAdded += edit.getEndB() - edit.getBeginB();
					locTouched += edit.getEndB() - edit.getBeginB();
			
				} else if (edit.getType() == Edit.Type.DELETE) {
				
					locTouched += edit.getEndA() - edit.getBeginA();
				
				} else if (edit.getType() == Edit.Type.REPLACE) {
				
					locTouched += edit.getEndA() - edit.getBeginA();
				
				}
			}
		
			int prevLocTouched = fileEntity.getLocTouched();
			fileEntity.setLocTouched(prevLocTouched + locTouched);
			
			int prevNumberRevisions = fileEntity.getNumberRevisions();
			fileEntity.setNumberRevisions(prevNumberRevisions + 1);


			// Check if the commit is associated to some ticket
			if (!commitEntity.getTicketEntityList().isEmpty()) {
				
				// If yes, set the call buggy and calculate the number of "NumberBugFix"
				int prevNumberBugFix = fileEntity.getNumberBugFix();
				fileEntity.setNumberBugFix(prevNumberBugFix + commitEntity.getTicketEntityList().size());
				fileEntity.setBuggy(true);
			}

			int prevLocAdded = fileEntity.getLocAdded();
			fileEntity.setLocAdded(prevLocAdded + locAdded);
						
			if (locAdded > fileEntity.getMaxLocAdded()) {
				
				fileEntity.setMaxLocAdded(locAdded);
			}

			int prevChgSetSize = fileEntity.getChgSetSize();
			fileEntity.setChgSetSize(prevChgSetSize + chgSetSize);
			
			if (chgSetSize > fileEntity.getMaxChgSet()) {
				fileEntity.setMaxChgSet(chgSetSize);
			} 
				
			fileEntity.setAvgLocAdded((float)(prevLocAdded + locAdded) / (float)(prevNumberRevisions + 1));
			fileEntity.setAvgChgSet((float)(prevChgSetSize + chgSetSize) / (float)(prevNumberRevisions + 1));
			
			putEmptyRecord(fileEntity);
			
		} 

		return;

	}

	private FileEntity removeFileEntity(int version, String filename) {

		for(int k = 0; k < projectEntity.getFileEntityList().size() - 1; k++) {
			
			FileEntity fileEntity = projectEntity.getFileEntityList().get(k);
			
			if(fileEntity.getIndexVersion() == version && fileEntity.getFileName().equals(filename)) {

				projectEntity.getFileEntityList().remove(k);
				return fileEntity;
			}
		}
				
		return null;

	}

	public void putEmptyRecord(FileEntity fileEntity) {
				
		//check if fileEntity already exists
		for(int k = 0; k < projectEntity.getFileEntityList().size() - 1; k++) {
			
			FileEntity currentFileEntity = projectEntity.getFileEntityList().get(k);
			
			if(currentFileEntity.getIndexVersion() == fileEntity.getIndexVersion() && currentFileEntity.getFileName().equals(fileEntity.getFileName())) {
				
				return;
			}
		}
		
		projectEntity.addFileToList(fileEntity);

	
	}

	public ProjectEntity calculateAverageMetric() {
			
		for(int i = 0; i < projectEntity.getFileEntityList().size() - 1; i++) {
			
			FileEntity currentFileEntity = projectEntity.getFileEntityList().get(i);
								
			float numberRevisions = currentFileEntity.getNumberRevisions();
			
			if(numberRevisions != 0) {
				
				float avgChgSet = currentFileEntity.getAvgChgSet() / numberRevisions;
				float avgLocAdded = currentFileEntity.getLocAdded() / numberRevisions;
				
				currentFileEntity = removeFileEntity(currentFileEntity.getIndexVersion(), currentFileEntity.getFileName());
				
				currentFileEntity.setAvgChgSet(avgChgSet);
				currentFileEntity.setAvgLocAdded(avgLocAdded);
				
				putEmptyRecord(currentFileEntity);		
				
			}
			
		}
			
		return projectEntity;
	}
	
	public ProjectEntity setClassBuggy(CommitEntity commitEntity, DiffEntry entry, int numberOfVersions) {

		// Check the ticket list associated to the commit  and the edit type of the file
		if (!commitEntity.getTicketEntityList().isEmpty() && (entry.getChangeType() == DiffEntry.ChangeType.MODIFY
				|| entry.getChangeType() == DiffEntry.ChangeType.DELETE)) {

			// For each ticket (IV, OV, ID, ..., IV, OV, ID)...
			for (int j = 0; j < commitEntity.getTicketEntityList().size() - 1; j++) {
				
				int startVersion = commitEntity.getTicketEntityList().get(j).getIvIndex();
				int endVersion = commitEntity.getTicketEntityList().get(j).getOvIndex();

				// ... for each version in the affected version range (list) check if the version index is included in the first half of the release ...
				for (int version = startVersion; version < endVersion && version < numberOfVersions; version++) {

					//if (!fileMapDataset.containsKey(version, entry.getNewPath())) {
					if (!checkFileEntity(version, entry.getNewPath())) {
								
						// ... set the class "Buggy"
						FileEntity fileEntity = removeFileEntity(version, entry.getNewPath());
						fileEntity.setBuggy(true);
						putEmptyRecord(fileEntity);
					}
				}
			}
		}
		
		return projectEntity;
	}


	private boolean checkFileEntity(int version, String newPath) {
		
		for(int k = 0; k < projectEntity.getFileEntityList().size() - 1; k++) {
			
			FileEntity fileEntity = projectEntity.getFileEntityList().get(k);
			
			if(fileEntity.getIndexVersion() == version && fileEntity.getFileName().equals(newPath)) {
				
				return true;
			}
		}
		
		return false;
	}

	public void getBuggyVersionListAV(TicketEntity ticketEntity, ProjectEntity projectEntity) {

		// Get the three version index
		ticketEntity.setOvIndex(getOpeningVersion(ticketEntity.getCreationDate()));
		ticketEntity.setIvIndex(getAffectedVersionByList(ticketEntity.getAv(), ticketEntity.getCreationDate()));
		ticketEntity.setFvIndex(getFixedVersion(ticketEntity.getResolutionDate()));

		// Check if the index of IV is different from 0, then the ticket has associated valid AV
		if (ticketEntity.getIvIndex() != 0) {

			// Calculate (if check is ok) the proportion of the ticket and add it to the list of all ticket with proportion
			if (!(ticketEntity.getFvIndex() == ticketEntity.getOvIndex() || ticketEntity.getFvIndex() == ticketEntity.getIvIndex() || ticketEntity.getFvIndex() < ticketEntity.getIvIndex())) {
				
				double fvIv = (double)ticketEntity.getFvIndex() - ticketEntity.getIvIndex();
				double fvOv = (double)ticketEntity.getFvIndex() - ticketEntity.getOvIndex();
				double proportion = fvIv / fvOv;
				
				if (proportion > 0) {
					
					ticketEntity.setProportion(proportion);
					projectEntity.addTicketBuggyAV(ticketEntity);
					
					//ticketWithProportion.put(ticketEntity.getId(), 1.0);
					//ticketWithProportion.put(ticketEntity.getId(), proportion);
				}
			}

			// Get the IV and FV index of the ticket
			getBuggyVersionList(ticketEntity);

		} else {

			// If AV is not present, then put the ticket in the list of the tickets that needs proportion for estimate IV
			projectEntity.addTicketBuggyNoAV(ticketEntity);
			
			//ticketWithoutAffectedVersionList.put(ticketEntity.getId(), ticketEntity.getOvIndex());
			//ticketWithoutAffectedVersionList.put(ticketEntity.getId(), ticketEntity.getFvIndex());
		}
	}

	public void getBuggyVersionProportionTicket(ProjectEntity projectEntity) {

		int ivIndex = 1;

		// Iterate over all the commit without affected version list
		for(int k = 0; k < projectEntity.getTicketBuggyNoAV().size() - 1; k++) {
			
			TicketEntity ticketEntity = projectEntity.getTicketBuggyNoAV().get(k);
			
			int fvIndex = ticketEntity.getFvIndex();
			int ovIndex = ticketEntity.getOvIndex();

			// Get the mean proportion of the previous tickets
			int proportion = (int) Math.round(getProportionPreviousTicket(k));

			// Check if the affected version list is not empty
			if (fvIndex != ovIndex) {

				// Use the P formula, if the mean value of previous tickets it's greater than 0
				if (proportion > 0) {
					ivIndex = fvIndex - (fvIndex - ovIndex)*proportion;
					if (ivIndex < 1)
						ivIndex = 1;

				} else {
					// otherwise use the "simple approach"
					ivIndex = ovIndex;
				}

				// Get the IV and FV index of the ticket
				getBuggyVersionList(ticketEntity);
			} 
			
			
						
		}

	}


	/** This function calculate the index of the appertaining version of a file
	 * 
	 * @param fileCommitDate,the date of the commit
	 * @return lastIndex, the index of the appertaining version of the file
	 */ 
	public int getCommitAppartainVersion(LocalDate fileCommitDate, ProjectEntity projectEntity) {

		int lastIndex = 0;
		LocalDate currentDate = null;

		for(int k = 0; k < projectEntity.getVersionEntityList().size(); k++) {
			
			VersionEntity versionEntity = projectEntity.getVersionEntityList().get(k);
			lastIndex = versionEntity.getIndex();
			currentDate = versionEntity.getReleaseDate();
			
			if (currentDate.isAfter(fileCommitDate)) {
				break;
			}
			
		}
			
		return lastIndex;
	}


	/** This function calculate value of P of the previous tickets (if any available)
	 * 
	 * @param ticketID, the ID of the ticket
	 * @return result, the value of P (could be 0 if no previous tickets)
	 */ 
	public double getProportionPreviousTicket(int ticketID) {

		int counter = 0;
		double proportion = 0;
		double result;

		// For each ticket with correct calculated P value...
		for (int k : ticketWithProportion.keySet()) {

			// ... check iv the ticket ID is lower than the current ticket and sum the P value
			if (k < ticketID && Iterables.get(ticketWithProportion.get(k), 0) != -1.0) {
				counter = counter + 1;
				proportion = proportion + Iterables.get(ticketWithProportion.get(k), 1);
			}
		}

		// If number of previous ticket is greater than 0, calculate the mean value P
		if (counter > 0) {
			result = counter / proportion;
		} else {

			// Else, return 0, to signal that we need to use the simple method
			result = 0;
		}

		return result;
	}


	/** This function calculate the index of the fixed version
	 * 
	 * @param ticketCreationDate, the date of the creation of the ticket
	 * @return fvIndex, the index of the fixed version
	 */ 
	public int getFixedVersion(String resolutionDate) {

		int fvIndex = 0;

		// Iterate over all version with release date
		for (LocalDate k : versionListWithDateAndIndex.keySet()) {

			/*  Why this assign before the check? If we have a ticket with resolutionDate date after the last released version
			 * in that way we associate it to the last released version. Is this wrong? Not in our scope, because we just
			 * want to build the dataset for the first half of the release. In this way we assign a "fake" FV to the ticket,
			 * because we don't want to loose the affected version list of the ticket. */
			fvIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));

			if (k.isEqual(LocalDate.parse(resolutionDate)) || k.isAfter(LocalDate.parse(resolutionDate))) {

				// Break if we found the fixed version of the file before the end of the iteration
				break;
			}
		}

		return fvIndex;
	}


	/** This function calculate the index of the opening version
	 * 
	 * @param ticketCreationDate, the date of the creation of the ticket
	 * @return ov, the index of the opening version
	 */ 
	public int getOpeningVersion(String ticketCreationDate) {

		int ovIndex = 0;

		// Iterate over all version with release date
		for (LocalDate k : versionListWithDateAndIndex.keySet()) {

			/*  Why this assign before the check? If we have a commit with commit date after the last released version
			 * in that way we associate it to the last released version. Is this wrong? Not in our scope, because we just
			 * want to build the dataset for the first half of the release. In this way we assign a "fake" FV to the ticket,
			 * because we don't want to loose the affected version list of the ticket. */
			ovIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));
			if (k.isAfter(LocalDate.parse(ticketCreationDate)) || k.isEqual(LocalDate.parse(ticketCreationDate))) {

				// Break if we found the opening version of the file before the end of the iteration
				break;
			}
		}

		return ovIndex;
	}


	/** This function, given the list of the Affected Version taked from Jira, return the index of the oldest IV version
	 * 
	 * @param versionList, the list of all the AV from Jira
	 * @param creationDate, the creation date of the ticket
	 * @return version, the index of the oldest IV version
	 */ 
	public int getAffectedVersionByList(List<String> versionList, String creationDate) {

		int ivVersion = 0;

		// Iterate over all the version with release date and the versionList to found the oldest affected version
		for (LocalDate k : versionListWithDateAndIndex.keySet()) {
			for (String k1 : versionList) {

				/* Check if the versio'ns index is equals to the one contained in the list,
				 * but also check that the release date of the version is before the creation of the ticket
				 * (check needed because of some wrong data on Jira)*/
				if (Iterables.get(versionListWithDateAndIndex.get(k), 0).equals(k1) && k.isBefore(LocalDate.parse(creationDate))) {
					ivVersion = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));
					break;
				}
			}
		}

		return ivVersion;
	}


	/** This function check the IV and FV index and add the ticket to the commit with valid AV list, with [IV,FV) index
	 * 
	 * @param tickedID, the ID of the ticket
	 * @param fvIndex, the index of the FV version
	 * @param ivIndex, the index of the OV version
	 */ 
	public void getBuggyVersionList(TicketEntity ticketEntity) {

		List<Integer> affectedVersionBoundaryList = new ArrayList<>();

		// Check if the index of the versions are ok
		if (ticketEntity.getFvIndex() != ticketEntity.getIvIndex() && ticketEntity.getIvIndex() < ticketEntity.getFvIndex()) {

			// Then add the ticket to the list of the tickets with not empty affected version list
			affectedVersionBoundaryList.add(ticketEntity.getIvIndex());
			affectedVersionBoundaryList.add(ticketEntity.getFvIndex());
			ticketWithBuggyIndex.put(ticketEntity.getId(), affectedVersionBoundaryList);
		}
	}
}
