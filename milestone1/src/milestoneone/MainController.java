package milestoneone;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.json.JSONArray;

public class MainController {

	// Get a new istance of JiraUtils object
	private static TicketController jiraUtilsIstance;
	private static ProjectEntity projectEntity;

	public static final String USER_DIR = "user.dir";
	public static final String RELEASE_DATE = "releaseDate";
	public static final String FILE_EXTENSION = ".java";

	
	 public static String readAll(Reader rd) throws IOException {

	 	 StringBuilder sb = new StringBuilder();
		 int cp;
		 while ((cp = rd.read()) != -1) {
			 sb.append((char) cp);
		 }
		 return sb.toString();
	 }

	 
	 public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	 	
		 InputStream is = new URL(url).openStream();
		 JSONObject json = null;
		 
		 try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			
			 json = new JSONObject(readAll(rd));
		
		 } finally {
		 
			 is.close();
		
		 }
		
		 return json;
	 }
	
	public static Multimap<LocalDate, String> getVersionWithReleaseDate(ProjectEntity projectEntity) throws IOException, JSONException {

		Multimap<LocalDate, String> versionList = MultimapBuilder.treeKeys().linkedListValues().build();
		Integer i;

		// Url for the GET request to get information associated to Jira project
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectEntity.getName();

		JSONObject json = readJsonFromUrl(url);

		// Get the JSONArray associated to project version
		JSONArray versions = json.getJSONArray("versions");	

		// For each version...
		for (i = 0; i < versions.length(); i++) {
			// ... check if verion has release date and name, and add it to relative list
			if (versions.getJSONObject(i).has(RELEASE_DATE) && versions.getJSONObject(i).has("name")) {
				
				String releaseName = versions.getJSONObject(i).get("name").toString();
				LocalDate localDate = LocalDate.parse(versions.getJSONObject(i).get(RELEASE_DATE).toString());
				
				if(!(projectEntity.getName().equals("SYNCOPE") && (releaseName.equals("1.1.7") || releaseName.equals("2.0.0-M3") || releaseName.equals("2.0.8")
						|| releaseName.equals("2.1.1") || releaseName.equals("2.1.2") || releaseName.equals("2.1.3")
						|| releaseName.equals("2.1.4") || releaseName.equals("2.1.6") || releaseName.equals("2.1.7")
						|| releaseName.equals("2.1.8") || releaseName.equals("2.1.5")))) {
					
					VersionEntity versionEntity = new VersionEntity(); 
					versionEntity.setReleaseDate(localDate);
					versionEntity.setReleaseName(releaseName);
					
					projectEntity.addVersionEntity(versionEntity);
					projectEntity.addVersion(releaseName);
					//to remove
					versionList.put(LocalDate.parse(versions.getJSONObject(i).get(RELEASE_DATE).toString()), releaseName);
				}
			}
		}

		for(int k = 0; k < projectEntity.getVersionEntityList().size(); k++) {
			
			VersionEntity versionEntity = projectEntity.getVersionEntityList().get(k);
			versionEntity.setIndex(k + 1);
			
		}
		
		// Give an index to each release in the list
		int counterVersion = 1;
		for (LocalDate k : versionList.keySet()) {
			
			versionList.put(k, String.valueOf(counterVersion));
			counterVersion++;
		}
		
		for(i = 0; i < projectEntity.getVersion().size() - 1; i++) {
			
			for (int j = i + 1; j < projectEntity.getVersion().size(); j++) {  
				
				//compares each elements of the array to all the remaining elements  
				if(projectEntity.getVersion().get(i).compareTo(projectEntity.getVersion().get(j)) > 0){
								
					//swapping array elements  
					String temp = projectEntity.getVersion().get(i);  
					projectEntity.getVersion().set(i, projectEntity.getVersion().get(j));  
					projectEntity.getVersion().set(j, temp);  
				}  
			}  
		}  
		
		projectEntity.getVersion().sort(null);

		return versionList;
	}

	public static void getBuggyVersionAVTicket() throws IOException, JSONException {

		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		String key = null;

		// Get JSON API for closed bugs w/ AV in the project
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projectEntity.getName()
					+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,versions,resolutiondate,created,fixVersions&startAt="
					+ i.toString() + "&maxResults=1000";
			JSONObject json = readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");

			// For each closed ticket...
			for (; i < total && i < j; i++) {

				JSONObject singleJsonObject = (JSONObject) issues.getJSONObject(i % 1000).get("fields");

				// ... get the key of the ticket,
				key = issues.getJSONObject(i % 1000).get("key").toString();

				// , get JSONArray associated to the affected versions,
				JSONArray affectedVersionArray = singleJsonObject.getJSONArray("versions");

				//create a ticket entity
				TicketEntity ticketEntity = new TicketEntity();
				ticketEntity.setId(Integer.valueOf(key.split("-")[1]));
				ticketEntity.setCreationDate(singleJsonObject.getString("created").split("T")[0]);
				ticketEntity.setResolutionDate(singleJsonObject.getString("resolutiondate").split("T")[0]);
				ticketEntity.setProportion(0);
				
				projectEntity.addTicketBuggy(ticketEntity);
				
				// Get a Java List from the JSONArray
				jiraUtilsIstance.getJsonAffectedVersionList(affectedVersionArray, ticketEntity);

				// Calculate the AV index of the ticket [IV, FV)
				jiraUtilsIstance.getBuggyVersionListAV(ticketEntity, projectEntity);

			}
		} while (i < total);

	}

	public static void buildDataset(String projectName) throws IOException, GitAPIException {

		FileRepositoryBuilder builder = new FileRepositoryBuilder();

		// Setting the project's folder
		String repoFolder = System.getProperty(USER_DIR) + "/" + projectName + "/.git";
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
				int appartainVersion = jiraUtilsIstance.getCommitAppartainVersion(commitLocalDate, projectEntity);

				// Check if the version index is in the first half of the releases
				if (appartainVersion >= projectEntity.getHalfVersion() + 1) continue;

				CommitEntity commitEntity = new CommitEntity();
				commitEntity.setMessage(commit.getFullMessage());
				commitEntity.setDate(commitLocalDate);
				commitEntity.setAppartainVersion(appartainVersion);

				//List<TicketEntity> ticketBugFix = jiraUtilsIstance.getTicketAssociatedCommitBugFix(commit.getFullMessage(), projectName);
				//commitEntity.setTicketEntityList(ticketBugFix);
				
				// Get the list of the commit (could be empty) associated to the commit
				List<TicketEntity> ticketInformationBugginess = jiraUtilsIstance.getTicketAssociatedCommitBuggy(commit.getFullMessage(), projectName);
				commitEntity.setTicketEntityList(ticketInformationBugginess);

				// Create a new DiffFormatter, needed to get the change between the commit and his parent
				try (DiffFormatter differenceBetweenCommits = new DiffFormatter(NullOutputStream.INSTANCE)) {
					
					differenceBetweenCommits.setRepository(repository);
					
					// Get the difference between the two commit
					filesChanged = differenceBetweenCommits.scan(commit.getParent(0), commit);
					commitEntity.setFilesChanged(filesChanged);
					
					// For each file changed in the commit
					for (DiffEntry singleFileChanged : filesChanged) {
					
						if (singleFileChanged.getNewPath().endsWith(FILE_EXTENSION)) {
							
							jiraUtilsIstance.getMetrics(commitEntity, singleFileChanged, differenceBetweenCommits, projectEntity.getHalfVersion() + 1);
							
							// Set this and other class contained in [IV, FV) buggy (if ther'are ticket(s) associated to the commit)
							projectEntity = jiraUtilsIstance.setClassBuggy(commitEntity, singleFileChanged, projectEntity.getHalfVersion() + 1);
							
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, JSONException, GitAPIException {

		Map<Integer, List<Integer>> ticketWithBuggyIndex;

		//project = true equals bookkeeper
		//else syncope
		boolean project = false;
		projectEntity = new ProjectEntity();
		
		if(project) {
			
			projectEntity.setName("BOOKKEEPER");
			
		}else {
			
			projectEntity.setName("SYNCOPE");
		}

		//Multimap<ReleaseDate, VersionName, VersionIndex>
		Multimap<LocalDate, String> versionListWithReleaseDate = MultimapBuilder.treeKeys().linkedListValues().build();
			
		ticketWithBuggyIndex = new HashMap<>();

		// Get the list of version with release date
		versionListWithReleaseDate = getVersionWithReleaseDate(projectEntity);

		jiraUtilsIstance = new TicketController(projectEntity, versionListWithReleaseDate, ticketWithBuggyIndex, projectEntity.getTicketBuggy());
		projectEntity.setHalfVersion(versionListWithReleaseDate.size() / 4);
						
		//ticketEntity = new TicketEntity();
		getBuggyVersionAVTicket();

		// Find the IV and FV index for tickets without Jira affected version (proportion method needed)
		jiraUtilsIstance.getBuggyVersionProportionTicket(projectEntity);

		// Build the dataset
		buildDataset(projectEntity.getName());		

		// Write the dataset to CSV file
		CSVController csvController = new CSVController();	
		FileWriter csvResult = csvController.initializeCSVResult(projectEntity);
	
		for(int k = 0; k < projectEntity.getFileEntityList().size() - 1; k++) {

			List<String> attribute = new ArrayList();
						
			attribute.add(projectEntity.getVersion().get(projectEntity.getFileEntityList().get(k).getIndexVersion()));
			attribute.add(projectEntity.getFileEntityList().get(k).getFileName());
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getLocTouched()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getNumberRevisions()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getNumberBugFix()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getLocAdded()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getMaxLocAdded()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getChgSetSize()));
			attribute.add(Integer.toString(projectEntity.getFileEntityList().get(k).getMaxChgSet()));
			attribute.add(Float.toString(projectEntity.getFileEntityList().get(k).getAvgChgSet()));
			attribute.add(Float.toString(projectEntity.getFileEntityList().get(k).getAvgLocAdded()));
			attribute.add(Boolean.toString(projectEntity.getFileEntityList().get(k).getBuggy()));
				 			
			csvController.addRowToCSV(attribute, csvResult);
			
		}
		
		csvResult.close();

	}
}
