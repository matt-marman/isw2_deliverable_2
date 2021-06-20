package milestoneone.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import milestoneone.Entity.CommitEntity;
import milestoneone.Entity.FileEntity;
import milestoneone.Entity.ProjectEntity;
import milestoneone.Entity.TicketEntity;
import milestoneone.Entity.VersionEntity;

public class TicketController {

	private static ProjectEntity projectEntity;
	
	public TicketController() {};
	
	public TicketController(ProjectEntity projectEntity, List <TicketEntity> ticketList) {this.projectEntity = projectEntity;}

	/**
	 * Given a commit message, this function returns a list of all ticket id
	 * contained in the message
	 * 
	 * @param commitMessage
	 * @param projectName
	 * @return
	 */
	public List<TicketEntity> getTicketForCommit(String commitMessage, String projectName) {
		
		List<TicketEntity> resultList = new ArrayList<>();
		Pattern pattern = null;
		Matcher matcher = null;

		for(int k = 0; k < projectEntity.getTicketBuggy().size() - 1; k++) {
			
			TicketEntity currentTicketEntity = projectEntity.getTicketBuggy().get(k);
					
			// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
			pattern = Pattern.compile("\\b"+ projectName + "-" + currentTicketEntity.getId() + "\\b", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(commitMessage);

			// Check if commit message contains the issues ID and the issues is labeled like "not checked"
			if (matcher.find() && !resultList.contains(currentTicketEntity.getId())) {
						
				TicketEntity ticketEntity = new TicketEntity();
				ticketEntity.setIvIndex(currentTicketEntity.getIvIndex());
				ticketEntity.setFvIndex(currentTicketEntity.getFvIndex());
				ticketEntity.setId(currentTicketEntity.getId());
					
				resultList.add(ticketEntity);
			}
			
		}
		
		return resultList;
	}
	
	/**
	 * This function associates the resolution date of ticket, 
	 * not from jira, but to last commit that contains the ID in the message.
	 * 
	 * @param ticketEntity
	 * @throws IOException
	 */
	
	public void getResolutionDateForTicketFromCommit(TicketEntity ticketEntity) throws IOException {
		
		List<String> listDate = new ArrayList<>();
		
		FileRepositoryBuilder builder = new FileRepositoryBuilder();

		// Setting the project's folder
		String repoFolder = System.getProperty("user.dir") + "/" + projectEntity.getName() + "/.git";
		Repository repository = builder.setGitDir(new File(repoFolder)).readEnvironment().findGitDir().build();
				
		Pattern pattern = null;
		Matcher matcher = null;
				
		try (Git git = new Git(repository)) {

			Iterable<RevCommit> commits = getCommits(git);
					
			for (RevCommit commit : commits) {
						
				String message = commit.getFullMessage();
				// Use pattern to check if the commit message contains the word "*ProjectName-IssuesID*"
				pattern = Pattern.compile("\\b" + projectEntity.getName() + "-" + ticketEntity.getId() + "\\b", Pattern.CASE_INSENSITIVE);
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
					
				ticketEntity.setResolutionDate(listDate.get(0));
					
			}
					
		}
	}
	
	public static Iterable<RevCommit> getCommits(Git git) {
		   
		   Iterable<RevCommit> commits = null;

			// Get all commits
			try {
				return git.log().all().call();
			} catch (NoHeadException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return commits;
	   }
	
	/**
	 * 
	 * It calculates all metrics for couple (version, file).
	 * The metrics are:
	 * 
	 *  - LOC_Touched
	 *  - NumberRevisions
	 *  - NumberBugFix
	 *  - LOC_Added
	 *  - MAX_LOC_Added
	 *  - Chg_Set_Size
	 *  - Max_Chg_Set
	 *  - Avg_Chg_Set
	 *  - AVG_LOC_Added
	 * 
	 * @param commitEntity
	 * @param entry
	 * @param diffFormatter
	 * @param limitVersion
	 * @throws IOException
	 */
	
	public void getMetrics (CommitEntity commitEntity, DiffEntry entry, DiffFormatter diffFormatter) throws IOException{

		//first, if version of the file is grater than the half version, ignore this file
		if (commitEntity.getAppartainVersion() >= projectEntity.getHalfVersion() + 1) return;
				
		//it updates values of couple (version, file). 
		FileEntity fileEntity = removeFileEntity(commitEntity.getAppartainVersion(), entry.getNewPath());
		
		//if it is the first time, let's create a new instance of (version, file)
		if(fileEntity == null) {
			
			fileEntity = new FileEntity(); 
			fileEntity.setFileName(entry.getNewPath());
			fileEntity.setIndexVersion(commitEntity.getAppartainVersion());
			
		}
			
		//let's calculate the metrics
		int locTouched = 0;
		int locAdded = 0;
		int chgSetSize = 0;

		//iterate on every editing of file
		for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {

			if (edit.getType() == Edit.Type.INSERT) {
					
				locAdded += edit.getEndB() - edit.getBeginB();
				locTouched += edit.getEndB() - edit.getBeginB();
			
			} else if (edit.getType() == Edit.Type.DELETE || edit.getType() == Edit.Type.REPLACE) {
				
				locTouched += edit.getEndA() - edit.getBeginA();
			}
		}

		//take number of file committed together
		chgSetSize = commitEntity.getFilesChanged().size();
		
		//update all previous metrics
		int prevLocTouched = fileEntity.getLocTouched();
		fileEntity.setLocTouched(prevLocTouched + locTouched);
			
		int prevNumberRevisions = fileEntity.getNumberRevisions();
		fileEntity.setNumberRevisions(prevNumberRevisions + 1);

		//check if commit is associated to some ticket
		if (!commitEntity.getTicketEntityList().isEmpty()) {
				
			//set the couple (version, file) buggy and update NumberBugFix
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
		
		projectEntity.addFileToList(fileEntity);
			
		return;
	}
	
	/**
	 * Given a couple (version, filename), it is removed
	 * from the list of projectEntity.
	 * 
	 * @param version
	 * @param filename
	 * @return
	 */
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

	/**
	 * It calculates all average metrics.
	 * i.e. 
	 * 	- Avg_Chg_Set
	 *  - AVG_LOC_Added
	 * 
	 * @return
	 */
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
				
				projectEntity.addFileToList(currentFileEntity);	
				
			}
			
		}
			
		return projectEntity;
	}
	
	/**
	 * This function set the bugginess of couple (version, filename).
	 * Given a file and the version of commit that contains an id
	 * of ticket buggy.
	 * 
	 * @param commitEntity
	 * @param entry
	 * @return
	 */
	
	public ProjectEntity setCoupleBuggy(CommitEntity commitEntity, DiffEntry entry) {

		if (commitEntity.getTicketEntityList().isEmpty()) return projectEntity;

		for (int j = 0; j < commitEntity.getTicketEntityList().size() - 1; j++) {
				
			int iv = commitEntity.getTicketEntityList().get(j).getIvIndex();
			int fv = commitEntity.getTicketEntityList().get(j).getFvIndex();

			for (int k = iv; k < fv; k++) { 

				FileEntity fileEntity = removeFileEntity(k, entry.getNewPath());
				if(fileEntity == null) {
					
					fileEntity = new FileEntity();
					fileEntity.setIndexVersion(k);
					fileEntity.setFileName(entry.getNewPath());
					
				}
			
				fileEntity.setBuggy(true);
				projectEntity.addFileToList(fileEntity);						

			}
		}
		
		return projectEntity;
	
	}
	

	/**
	 * This method set the iv, ov, fv for ticketEntity, and divides ticket 
	 * with av from ticket without it. In this way it can calculate the proportion value
	 * for each ticket, and the proportion mean used for estimate other tickets.
	 * 
	 * @param ticketEntity
	 * @throws IOException
	 */
	
	public void setVersions(TicketEntity ticketEntity) throws IOException {

		/*
		 * it set the resolution date of ticket getting
		 * the date of last commit with the id in message.
		 */
		getResolutionDateForTicketFromCommit(ticketEntity);
		
		ticketEntity.setOvIndex(getOpeningVersion(ticketEntity.getCreationDate()));
		ticketEntity.setIvIndex(getAffectedVersionByList(ticketEntity.getAv(), ticketEntity.getCreationDate()));
		ticketEntity.setFvIndex(getFixedVersion(ticketEntity.getResolutionDate()));

		if (ticketEntity.getIvIndex() == 0) {
		
			projectEntity.addTicketBuggyNoAV(ticketEntity);
			return;
		}
			
		if (ticketEntity.getFvIndex() > ticketEntity.getOvIndex() && 
			ticketEntity.getFvIndex() > ticketEntity.getIvIndex() &&
			ticketEntity.getOvIndex() >= ticketEntity.getIvIndex()) {
				
			double fvIv = (double)ticketEntity.getFvIndex() - ticketEntity.getIvIndex();
			double fvOv = (double)ticketEntity.getFvIndex() - ticketEntity.getOvIndex();
			double proportion = fvIv / fvOv;
				
			if (proportion > 0) {
					
				ticketEntity.setProportion(proportion);
				projectEntity.addTicketBuggyAV(ticketEntity);
					
			}
		}
	}
	

	/**
	 * It calculates and applies the estimate proportion
	 * to ticket without av.
	 * 
	 * The formula for iv is:
	 * 
	 * 		iv = fv - P*(fv - ov)
	 * 
	 * @param projectEntity
	 */
	
	public void applyEstimateProportion(ProjectEntity projectEntity) {

		int proportion = getEstimateProportion();
		
		for(int k = 0; k < projectEntity.getTicketBuggyNoAV().size() - 1; k++) {
			
			TicketEntity ticketEntity = projectEntity.getTicketBuggyNoAV().get(k);
			
			int fvIndex = ticketEntity.getFvIndex();
			int ovIndex = ticketEntity.getOvIndex();
			int ivIndex = 0;

			if (proportion >= 0) ivIndex = fvIndex - proportion * (fvIndex - ovIndex);
					
			ticketEntity.setIvIndex(ivIndex);
			
			TicketEntity currentTicketEntity = new TicketEntity();
			currentTicketEntity = ticketEntity;
			
			projectEntity.getTicketBuggyNoAV().remove(k);
			projectEntity.getTicketBuggyNoAV().add(k, currentTicketEntity);
			
		}
	}

	/** 
	 * This function calculate the index of the appertaining version of commit.
	 * 
	 * @param fileCommitDate,the date of the commit
	 * @return lastIndex, the index of the appertaining version of the file
	 */ 
	
	public int getVersionOfCommit(LocalDate fileCommitDate, ProjectEntity projectEntity) {

		int index = 0;
		LocalDate currentDate = null;

		for(int k = 0; k < projectEntity.getVersionEntityList().size(); k++) {
			
			VersionEntity versionEntity = projectEntity.getVersionEntityList().get(k);
			index = versionEntity.getIndex();
			currentDate = versionEntity.getReleaseDate();
			
			if (currentDate.isAfter(fileCommitDate)) break;
			
		}
			
		return index;
	}
	
	/**
	 * It calculates the estimate proportion, doing 
	 * the mean between all proportion calculated before.
	 * 
	 * @return
	 */
	
	public int getEstimateProportion() {
		
		double sumProportion = 0;
		int numberTicketBuggyAV = projectEntity.getTicketBuggyAV().size();
		
		for(int k = 0; k < numberTicketBuggyAV - 1; k++) {
			
			TicketEntity currentTicketEntity = projectEntity.getTicketBuggyAV().get(k);
			int currentP = (int) currentTicketEntity.getProportion();
			sumProportion += currentP;
		}
				
		return (int) (sumProportion/(double)numberTicketBuggyAV);

	}
	
	/**
	 * 
	 * Given a resolution date of ticket, the method returns
	 * the relative index of appertained version.
	 * 
	 * @param resolutionDate
	 * @return
	 */
	
	public int getFixedVersion(String resolutionDate) {

		int fvIndex = 0;
		LocalDate resolutionLocalDate = LocalDate.parse(resolutionDate);

		for(int k = 0; k < projectEntity.getVersionEntityList().size() - 1; k++) {
			
			VersionEntity currentVersionEntity = projectEntity.getVersionEntityList().get(k);
			LocalDate localDate = currentVersionEntity.getReleaseDate();
			fvIndex = currentVersionEntity.getIndex();
			
			if (localDate.isEqual(resolutionLocalDate) || localDate.isAfter(resolutionLocalDate)) {

				return fvIndex;
			}
		}
	
		return fvIndex;
	}

	/**
	 * Given a creation date of ticket, the method returns
	 * the relative index of appertained version.
	 * 
	 * @param creationDate
	 * @return
	 */
	public int getOpeningVersion(String creationDate) {

		int ovIndex = 0;
		LocalDate creationLocalDate = LocalDate.parse(creationDate);
		
		for(int k = 0; k < projectEntity.getVersionEntityList().size() - 1; k++) {
			
			VersionEntity currentVersionEntity = projectEntity.getVersionEntityList().get(k);
			LocalDate localDate = currentVersionEntity.getReleaseDate();
			ovIndex = currentVersionEntity.getIndex();
			
			if (localDate.isEqual(creationLocalDate) || localDate.isAfter(creationLocalDate)) {

				return ovIndex;
			}
		}

		return ovIndex;
	}

	/**
	 * Given a creation date of ticket, and av list of ticket
	 * the method returns the relative index of appertained version.
	 * 
	 * @param creationDate
	 * @return
	 */
	public int getAffectedVersionByList(List<String> versionList, String creationDate) {

		int ivVersion = 0;

		for(int k = 0; k < projectEntity.getVersionEntityList().size() - 1; k++) {
			
			VersionEntity currentVersionEntity = projectEntity.getVersionEntityList().get(k);
			LocalDate localDate = currentVersionEntity.getReleaseDate();
			
			for (int j = 0; j < versionList.size() - 1; j++) {
				
				String currentVersion = versionList.get(j);
				
				if (currentVersion.equals(currentVersionEntity.getReleaseName()) && localDate.isBefore(LocalDate.parse(creationDate))) {
	
					// Break if we found the fixed version of the file before the end of the iteration
					ivVersion = currentVersionEntity.getIndex();
					break;
				}
			}
		}
		
		return ivVersion;
	}

}
