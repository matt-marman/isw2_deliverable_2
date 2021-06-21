package milestoneone.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.json.JSONException;

import milestoneone.entity.CommitEntity;
import milestoneone.entity.ProjectEntity;
import milestoneone.entity.TicketEntity;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

/**
 * The aim of this project is to provide a csv file 
 * that contains the metrics for each couple (version, file) 
 * of the Apache projects. 
 * I'm analyzing the first half of release for project.
 * 
 * Metrics are:
 * 
 *	- LOC_Touched
 *  - NumberRevisions
 *  - NumberBugFix
 *  - LOC_Added
 *  - MAX_LOC_Added
 *  - Chg_Set_Size
 *  - Max_Chg_Set
 *  - Avg_Chg_Set
 *  - AVG_LOC_Added
 * 
 * Each row also contains a boolean buggy field. 
 * 
 * The projects taken are Bookkeeper and Syncope.
 * 
 * @author Mattia Di Battista
 *
 */

public class MainController {

	private static TicketController ticketController;
	private static ProjectEntity projectEntity;

	public static void main(String[] args) throws IOException, JSONException, GitAPIException {

		/*
		 * project = true  --> Bookkeeper
		 * project = false --> Syncope
		 * 
		 */
	
		boolean project = false;
		projectEntity = new ProjectEntity();
		
		if(project) projectEntity.setName("BOOKKEEPER");
		else projectEntity.setName("SYNCOPE");
			
		JSONController jsonController = new JSONController();
		projectEntity = jsonController.getVersion(projectEntity);
		
		int numberRelease = projectEntity.getVersionEntityList().size();
		projectEntity.setHalfVersion(numberRelease / 2);
	
		projectEntity = jsonController.createTicket(projectEntity);
	
		ticketController = new TicketController();
		ticketController.applyEstimateProportion(projectEntity);

		createData();		

		new CSVController(projectEntity);	

	}

	private static void createData() throws IOException, GitAPIException {

		FileRepositoryBuilder builder = new FileRepositoryBuilder();

		String gitDirectory = System.getProperty("user.dir") + "/" + projectEntity.getName() + "/.git";
		Repository repository = builder.setGitDir(new File(gitDirectory)).readEnvironment().findGitDir().build();
		
		try (Git git = new Git(repository)) {

			Iterable<RevCommit> commits = null;

			commits = git.log().all().call();

			iterateOnCommit(commits, repository);
			
		}
	}
	
	private static void iterateOnCommit(Iterable<RevCommit> commits, Repository repository) throws IOException {
		
		for (RevCommit commit : commits) {

			LocalDate commitLocalDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			int appartainVersion = ticketController.getVersionOfCommit(commitLocalDate, projectEntity);

			//ignore ticket released after the half of release
			if (commit.getParentCount() == 0 || appartainVersion >= projectEntity.getHalfVersion() + 1) continue;

			CommitEntity commitEntity = new CommitEntity();
			commitEntity.setMessage(commit.getFullMessage());
			commitEntity.setDate(commitLocalDate);
			commitEntity.setAppartainVersion(appartainVersion);
			
			
			List<TicketEntity> ticketForCommit = ticketController.getTicketForCommit(commit.getFullMessage(), projectEntity);
			commitEntity.setTicketEntityList(ticketForCommit);

			iterateOnChange(repository, commit, commitEntity);
			
		}
	}
	
	private static void iterateOnChange(Repository repository, RevCommit commit, CommitEntity commitEntity) throws IOException {
		
		List<DiffEntry> filesChanged;
		
		try (DiffFormatter differenceBetweenCommits = new DiffFormatter(NullOutputStream.INSTANCE)) {
			
			differenceBetweenCommits.setRepository(repository);
			
			filesChanged = differenceBetweenCommits.scan(commit.getParent(0), commit);
			commitEntity.setFilesChanged(filesChanged);
			
			for (DiffEntry singleFileChanged : filesChanged) {
			
				if (singleFileChanged.getNewPath().endsWith(".java")) {
					
					ticketController.getMetrics(commitEntity, singleFileChanged, differenceBetweenCommits, projectEntity);
					projectEntity = ticketController.setCoupleBuggy(commitEntity, singleFileChanged, projectEntity);
					
				}
			}
		}
	}
}
