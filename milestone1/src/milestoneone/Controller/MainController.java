package milestoneone.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.json.JSONException;

import milestoneone.Entity.CommitEntity;
import milestoneone.Entity.ProjectEntity;
import milestoneone.Entity.TicketEntity;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

public class MainController {

	private static TicketController ticketController;
	private static ProjectEntity projectEntity;

	public static void main(String[] args) throws IOException, JSONException, GitAPIException {

		//project = true equals bookkeeper
		//else syncope
		boolean project = false;
		projectEntity = new ProjectEntity();
		
		if(project) projectEntity.setName("BOOKKEEPER");
		else projectEntity.setName("SYNCOPE");
			
		JSONController jsonController = new JSONController();
		
		// Get the list of version with release date
		projectEntity = jsonController.getVersionWithReleaseDate(projectEntity);
		
		int numberRelease = projectEntity.getVersionEntityList().size();
		projectEntity.setHalfVersion(numberRelease / 2);
		
		ticketController = new TicketController();
								
		projectEntity = jsonController.getBuggyVersionAVTicket(projectEntity);
		
		// Find the IV and FV index for tickets without Jira affected version (proportion method needed)
		ticketController.getBuggyVersionProportionTicket(projectEntity);

		// Build the dataset
		buildDataset(projectEntity.getName());		

		new CSVController(projectEntity);	

	}

	
	public static void buildDataset(String projectName) throws IOException, GitAPIException {

		FileRepositoryBuilder builder = new FileRepositoryBuilder();

		// Setting the project's folder
		String repoFolder = System.getProperty("user.dir") + "/" + projectName + "/.git";
		Repository repository = builder.setGitDir(new File(repoFolder)).readEnvironment().findGitDir().build();
		
		// Try to open the Git repository
		try (Git git = new Git(repository)) {

			Iterable<RevCommit> commits = null;

			// Get all the commits
			commits = git.log().all().call();

			// Iterate over the single issues
			for (RevCommit commit : commits) {

				// Check if commit has parent commit
				if (commit.getParentCount() == 0) continue;

				List<DiffEntry> filesChanged;

				// Get the date of the commit
				LocalDate commitLocalDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				// Get the appartain version of the commit
				int appartainVersion = ticketController.getCommitAppartainVersion(commitLocalDate, projectEntity);

				// Check if the version index is in the first half of the releases
				if (appartainVersion >= projectEntity.getHalfVersion() + 1) continue;

				CommitEntity commitEntity = new CommitEntity();
				commitEntity.setMessage(commit.getFullMessage());
				commitEntity.setDate(commitLocalDate);
				commitEntity.setAppartainVersion(appartainVersion);
				
				// Get the list of the ticket (could be empty) associated to the commit
				List<TicketEntity> ticketInformationBugginess = ticketController.getTicketAssociatedCommitBuggy(commit.getFullMessage(), projectName);
				
				commitEntity.setTicketEntityList(ticketInformationBugginess);

				// Create a new DiffFormatter, needed to get the change between the commit and his parent
				try (DiffFormatter differenceBetweenCommits = new DiffFormatter(NullOutputStream.INSTANCE)) {
					
					differenceBetweenCommits.setRepository(repository);
					
					// Get the difference between the two commit
					filesChanged = differenceBetweenCommits.scan(commit.getParent(0), commit);
					commitEntity.setFilesChanged(filesChanged);
					
					// For each file changed in the commit
					for (DiffEntry singleFileChanged : filesChanged) {
					
						if (singleFileChanged.getNewPath().endsWith(".java")) {
							
							ticketController.getMetrics(commitEntity, singleFileChanged, differenceBetweenCommits);
							
							// Set this and other class contained in [IV, FV) buggy (if ther'are ticket(s) associated to the commit)
							projectEntity = ticketController.setClassBuggy(commitEntity, singleFileChanged);
							
						}
					}
				}
			}
		}
	}
}
