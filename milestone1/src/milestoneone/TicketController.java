package milestoneone;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class TicketController {
	
		//Multimap<ReleaseDate, VersionName, VersionIndex>
		private  Multimap<LocalDate, String> versionListWithDateAndIndex;

		// Map<ticketID, (OV, FV)>
		private  Multimap<Integer, Integer> ticketWithProportion = MultimapBuilder.treeKeys().linkedListValues().build();

		// Map<ticketID, (OV, FV)>
		private  Multimap<Integer, Integer> ticketWithoutAffectedVersionList = MultimapBuilder.treeKeys().linkedListValues().build();

		// MultiKeyMap<FileVersion, FilePath, MetricsList>
		@SuppressWarnings("rawtypes")
		private  MultiKeyMap fileMapDataset;
		
		// Map<ticketID, (IV, FV)>
		private  Map<Integer, List<Integer>> ticketWithBuggyIndex;

		private int metrics = 10;
		
		@SuppressWarnings("rawtypes")
		public TicketController(Multimap<LocalDate, String> versionListWithDate, MultiKeyMap fileMapDataset) {
			
			this.versionListWithDateAndIndex = versionListWithDate;
			this.fileMapDataset = fileMapDataset;			
			ticketWithBuggyIndex = new HashMap<>();

		}
		
		//it puts entries like [4.0.0, file.java, 0, 0, ... ,0] in fileMapDataset
		@SuppressWarnings("unchecked")
		public void insertEntry(String appartainVersion, String nameFile) { 
			
			ArrayList<Integer> initialArrayMetrics;

			//create an array with all metrics equal to 0
			initialArrayMetrics = new ArrayList<>();
			for (int k = 0; k < metrics; k++) initialArrayMetrics.add(0);
			fileMapDataset.put(appartainVersion, nameFile, initialArrayMetrics); 
			
		}
		
		//return the date of ticket, not from jira, but from commit that contains the ID in the comment
		public String getResolutionDateForTicketFromCommit(String projectName, int id) {
			
			List<String> listDate = new ArrayList<>();
			
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			String repoFolder = projectName + "/.git";
			Repository repository = null;
			
			try {
				repository = builder.setGitDir(new File(repoFolder)).readEnvironment().findGitDir().build();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Pattern pattern = null;
			Matcher matcher = null;
			
			try (Git git = new Git(repository)) {

				Iterable<RevCommit> commits = null;

				// Get all the commits
				try {
					commits = git.log().all().call();
				} catch (NoHeadException e) {
					e.printStackTrace();
				} catch (GitAPIException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for (RevCommit commit : commits) {
					
					String message = commit.getFullMessage();
					// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
					pattern = Pattern.compile("\\b"+ projectName + "-" + id + "\\b", Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(message);

					// Check if commit message contains the issues ID and the issues is labeled like "not checked"
					if (matcher.find()) {
						
						/* add this date to the list
						 * because it could happens that more commits
						 * correspond to the same buggy ticket. I'm taking the last one.
						 */ 
						LocalDate commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

						listDate.add(commitDate.toString());

					}
				}
				
				//return the last commit added to the list
				if(!listDate.isEmpty()) {
					
					return listDate.get(0);
					
				}else return "";
				
			}
		}
		
		public void getBuggyVersionListAV(List<String> affectedVersionList, String projectName, String creationDate, int ticketID) {

			int fixedVersionIndex = 0;
			int injectedVersionIndex = 0;
			int openingVersionIndex = 0;

			double numerator = 0;
			double denominator = 0;
			int proportion = 0;
			
			// Get the three version index
			openingVersionIndex = getOpeningVersionIndex(creationDate);
			
			String resolutionDate = "";
			resolutionDate = getResolutionDateForTicketFromCommit(projectName, ticketID);
			
			//if commit does not has a date, return
			if(resolutionDate.equals("")) return;
			
			fixedVersionIndex = getFixedVersionIndex(resolutionDate);
			injectedVersionIndex = getAffectedVersionByList(affectedVersionList);
			
			//remove all tickets with invalid indexes
			//use the proportion method with these tickets
			if(injectedVersionIndex > openingVersionIndex || injectedVersionIndex > fixedVersionIndex || openingVersionIndex >= fixedVersionIndex) {
				
				ticketWithoutAffectedVersionList.put(ticketID, openingVersionIndex);
				ticketWithoutAffectedVersionList.put(ticketID, fixedVersionIndex);
				return;
			}
			
			// Check if the index of IV is different from 0, then the ticket has associated valid AV
			if (injectedVersionIndex != 0) {

				//calculate P = (FV - IV)/(FV - OV)
				numerator = fixedVersionIndex - injectedVersionIndex;
				denominator = fixedVersionIndex - openingVersionIndex;
				proportion = (int)(numerator/denominator);
				
				ticketWithProportion.put(ticketID, 1);
				ticketWithProportion.put(ticketID, proportion);
				
				// Get the IV and FV index of the ticket
				List<Integer> affectedVersionBoundaryList = new ArrayList<>();
				
				// Then add the ticket to the list of the tickets with not empty affected version list
				affectedVersionBoundaryList.add(injectedVersionIndex);
				affectedVersionBoundaryList.add(fixedVersionIndex);
				ticketWithBuggyIndex.put(ticketID, affectedVersionBoundaryList);
				
			}else {

				// If AV is not present, then put the ticket in the list of the tickets that needs proportion for estimate IV
				ticketWithoutAffectedVersionList.put(ticketID, openingVersionIndex);
				ticketWithoutAffectedVersionList.put(ticketID, fixedVersionIndex);
			}
		}
		
		public int getOpeningVersionIndex(String ticketCreationDate) {

			int openingVersionIndex = 0;
			LocalDate creationDate = LocalDate.parse(ticketCreationDate);

			// Iterate over all version with release date
			for (LocalDate k : versionListWithDateAndIndex.keySet()) {
							
				if(creationDate.isAfter(k)) {
										
					openingVersionIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));
					
				}else break;

			}

			return openingVersionIndex;
		}
		
		public int getFixedVersionIndex(String ticketResolutionDate) {

			int fixedVersionIndex = 0;
			LocalDate resolutionDate = LocalDate.parse(ticketResolutionDate);
			
			// Iterate over all version with release date
			for (LocalDate k : versionListWithDateAndIndex.keySet()) {
				
				if(resolutionDate.isAfter(k)) {
					
					fixedVersionIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));
					
				}else break;
	
			}

			return fixedVersionIndex;
		}
		
		public int getAffectedVersionByList(List<String> versionList) {

			int injectedVersionIndex = 0;
			//if the ticket has not, at least, an affected version return 0
			if(versionList.isEmpty()) return 0;
			
			String firstIV = versionList.get(0);

			for (LocalDate k : versionListWithDateAndIndex.keySet()) {
				// Iterate over all version with release date
				String releaseToCompare = String.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 0));
				//it founds the index of injected version
				if(releaseToCompare.equals(firstIV)) {
					
					injectedVersionIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));
					break;					
					
				}
			}
				
			return injectedVersionIndex;
		}
		
		public void getBuggyVersionList(int ticketID, int fixedVersionIndex, int injectedVersionIndex) {

			List<Integer> affectedVersionBoundaryList = new ArrayList<>();

			// Check if the index of the versions are ok
			if (fixedVersionIndex != injectedVersionIndex && injectedVersionIndex < fixedVersionIndex) {

				// Then add the ticket to the list of the tickets with not empty affected version list
				affectedVersionBoundaryList.add(injectedVersionIndex);
				affectedVersionBoundaryList.add(fixedVersionIndex);
				ticketWithBuggyIndex.put(ticketID, affectedVersionBoundaryList);
			}
		}
		
		public void getBuggyVersionProportionTicket(int proportion) {

			int injectedVersionIndex = 1;
			
			// Iterate over all the commit without affected version list
			// Map<ticketID, (OV, FV)>
			for (int ticketID : ticketWithoutAffectedVersionList.keySet()) {
				
				int fixedVersionIndex = Iterables.get(ticketWithoutAffectedVersionList.get(ticketID), 1);
				int openingVersionIndex = Iterables.get(ticketWithoutAffectedVersionList.get(ticketID), 0);

				injectedVersionIndex = fixedVersionIndex - (fixedVersionIndex - openingVersionIndex)*proportion;
				if (injectedVersionIndex < 1) injectedVersionIndex = 1;
				
				//if injectedVersionIndex > fixedVersionIndex, ingnore the ticket
				if(injectedVersionIndex > fixedVersionIndex) continue;

				// Get the IV and FV index of the ticket
				List<Integer> affectedVersionBoundaryList = new ArrayList<>();
				
				//add the ticket to the list
				affectedVersionBoundaryList.add(injectedVersionIndex);
				affectedVersionBoundaryList.add(fixedVersionIndex);
				ticketWithBuggyIndex.put(ticketID, affectedVersionBoundaryList);
				
				} 
		}
		
		public int getEstimateProportion() {
			
			int currentProportion = 0;
			double sumProportion = 0;
			
			for (int k : ticketWithProportion.keySet()) {			
				
				currentProportion = Iterables.get(ticketWithProportion.get(k), 1);
				sumProportion += currentProportion;
			}
				
			return (int) (sumProportion/(double)ticketWithProportion.keySet().size());
			
			
		}
		
		public int getCommitAppartainVersionIndex(LocalDate fileCommitDate) {

			int lastIndex = 0;

			// Iterate over all version with release date
			for (LocalDate k : versionListWithDateAndIndex.keySet()) {

				lastIndex = Integer.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 1));

				// Break if we found the appartaining version of the file before the end of the iteration
				if (k.isAfter(fileCommitDate)) break;
	
			}
			return lastIndex;
		}
		
		public List<Integer> getTicketAssociatedCommitBuggy(String commitMessage, String projectName) {
			
			List<Integer> ticketListBuggy = new ArrayList<>();
			Pattern pattern = null;
			Matcher matcher = null;

			// Map<ticketID, (IV, FV)>
			for (Map.Entry<Integer, List<Integer>> entry : ticketWithBuggyIndex.entrySet()) {
					
				// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
				pattern = Pattern.compile("\\b" + projectName + "-" + entry.getKey() + "\\b", Pattern.CASE_INSENSITIVE);				
				matcher = pattern.matcher(commitMessage);
				
				if (matcher.find()) {
					
					ticketListBuggy.add(ticketWithBuggyIndex.get(entry.getKey()).get(0));
					ticketListBuggy.add(ticketWithBuggyIndex.get(entry.getKey()).get(1));
					ticketListBuggy.add(entry.getKey());
					return ticketListBuggy;
				}
			}
			
			return ticketListBuggy;
		}
		
		public List<Integer> getTicketAssociatedCommitBugFix(String commitMessage, String projectName, List<Integer> ticketList) {
			
			List<Integer> ticketListBugFix = new ArrayList<>();
			Pattern pattern = null;
			Matcher matcher = null;
			
			for (Integer entry : ticketList) {

				// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
				pattern = Pattern.compile("\\b"+ projectName + "-" + entry + "\\b", Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(commitMessage);

				// Check if commit message contains the issues ID and the issues is labeled like "not checked"
				if (matcher.find() && !ticketListBugFix.contains(entry)) ticketListBugFix.add(entry);
			
			}
			return ticketListBugFix;
		}
		
		public List<Integer> calculateMetrics (DiffEntry entry, String version, DiffFormatter diffFormatter, List<DiffEntry> filesChanged, List<Integer> ticketAssociated) throws CorruptObjectException, MissingObjectException, IOException{

			/*	
			 *  Metrics used:
			 *  
			 *  0 - LOC_touched
			 *  1 - NR
			 *  2 - NFix
			 *  3 - LOC_Added
			 *  4 - MAX_LOC_Added
			 *  5 - ChgSetSize
			 *  6 - MAX_ChgSet
			 *  7 - AVG_ChgSet
			 *  8 - AVG_LOC_added
			 * 	9 - Bugginess
			 * 
			 * 
			 */
			
			
			@SuppressWarnings("unchecked")
			ArrayList<Integer> metricsForFile = (ArrayList<Integer>) fileMapDataset.get(version, entry.getNewPath());

			int loctouched = 0;
			int locadded = 0;
			int chgsetsize = filesChanged.size();
			
			try {
				
				for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {

					Type type = edit.getType();

					if (type == Edit.Type.INSERT) {
								
						locadded += edit.getEndB() - edit.getBeginB();
						loctouched += edit.getEndB() - edit.getBeginB();
						
					} 
					
					else if ((type == Edit.Type.DELETE) || (type == Edit.Type.REPLACE)) loctouched += edit.getEndA() - edit.getBeginA();
					
				}
				
			} catch (CorruptObjectException e) {
				e.printStackTrace();
			} catch (MissingObjectException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			metricsForFile.set(0, metricsForFile.get(0) + loctouched);
			metricsForFile.set(1, metricsForFile.get(1) + 1);	
			metricsForFile.set(3, locadded);
				
			// Check if the commit is associated to some bug ticket
			if (ticketAssociated.isEmpty()) metricsForFile.set(2, metricsForFile.get(2) + 0);
			else {
				
				metricsForFile.set(2, metricsForFile.get(2) + ticketAssociated.size());
				metricsForFile.set(9, 1);
			}
			
			metricsForFile.set(3, metricsForFile.get(3) + locadded);
			
			if (locadded > metricsForFile.get(4)) metricsForFile.set(4,  locadded);
				
			metricsForFile.set(5, metricsForFile.get(5) + chgsetsize);
			
			if (chgsetsize > metricsForFile.get(6)) metricsForFile.set(6,  chgsetsize);
			
			return metricsForFile;
			
		}
		
		public void setVersionBuggy(int injectedVersionIndex, int fixedVersionIndex, float numberOfVersions, DiffEntry entry) {
			
			// ... for each version in the affected version range (list) check if the version index is included in the first half of the release ...
			for (int version = injectedVersionIndex; version < fixedVersionIndex && version < numberOfVersions; version++) {

				String versionString = null;
				
				if (!fileMapDataset.containsKey(version, entry.getNewPath())) {
					
					//take the string version
					for (LocalDate k : versionListWithDateAndIndex.keySet()) {
						
						int index = Integer.parseInt(Iterables.get(versionListWithDateAndIndex.get(k), 1));
						if(index == version) versionString = String.valueOf(Iterables.get(versionListWithDateAndIndex.get(k), 0));
						
					}
						
					insertEntry(versionString, entry.getNewPath());

					//set the class "Buggy"
					List<Integer> metricsForFile = (ArrayList<Integer>) fileMapDataset.get(versionString, entry.getNewPath());
					metricsForFile.set(9, 1);
					fileMapDataset.replace(versionString, entry.getNewPath(), metricsForFile);
				}
			}
		}
			
		@SuppressWarnings("unchecked")
		public void setBugginess(List<Integer> ticketAssociatedWithCommit, DiffEntry entry, float numberOfVersions) {

			ChangeType changeType = entry.getChangeType();

			if (!ticketAssociatedWithCommit.isEmpty() && ((changeType == DiffEntry.ChangeType.MODIFY) || (changeType == DiffEntry.ChangeType.DELETE))) {
				
				//ticketAssociatedWithCommit = [IV, FV, ID, ..., IV, FV, ID]
				for (int j = 0; j < ticketAssociatedWithCommit.size(); j= j+3) {
					
					int injectedVersionIndex = ticketAssociatedWithCommit.get(j);
					int fixedVersionIndex = ticketAssociatedWithCommit.get(j + 1);

					setVersionBuggy(injectedVersionIndex, fixedVersionIndex, numberOfVersions, entry);

				}
			
		}
		}

}