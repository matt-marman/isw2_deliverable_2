package milestoneone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

public class MainController {

	   static String projectName = "BOOKKEEPER";
	   
	   static Collection<JSONObject> items;
	   static JSONObject mainNode;
	   static String fields = "fields";
	   
	   //string array that contains all the versions
	   private static List<String> versionArrayString;
	   
	   //integer array that contains all the ticket ID
	   private static List<Integer> ticketList;
	   
	   //index of the last version in the first half of release
	   private static float indexHalfVersion;
	   private static String releaseDate = "releaseDate";
	   private static TicketController ticketController;
	   
	   // MultiKeyMap<FileVersion, FilePath, MetricsList> 
	   @SuppressWarnings({ "unchecked", "rawtypes" })
	   private static MultiKeyMap fileMapDataset = MultiKeyMap.multiKeyMap(new LinkedMap());
			   
	   private static String readAll(Reader rd) {
		      StringBuilder sb = new StringBuilder();
		      int cp;
		      try {
				while ((cp = rd.read()) != -1) {
				     sb.append((char) cp);
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
		      return sb.toString();
		   }

	   public static JSONArray jsonArray(String jsonText) {
		   
		   try {
				return new JSONArray(jsonText);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		return null;
	   }
	   
	   public static JSONArray readJsonArrayFromUrl(String url) {
	      
		   InputStream is = null;
		try {
			is = new URL(url).openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	      try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
	         String jsonText = readAll(rd);
	         jsonArray(jsonText);
	         
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
	         try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	       }
		return null;
	      

	   }

	   public static JSONObject jsonObject(String jsonText) {
		   
		   try {
				return new JSONObject(jsonText);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		return mainNode;
	   }
	   
	   public static JSONObject readJsonFromUrl(String url){
	      InputStream is = null;
		try {
			is = new URL(url).openStream();
			
		} catch (IOException e) {
			e.printStackTrace();
		
		}
	      try ( BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
	         String jsonText = readAll(rd);
	         return jsonObject(jsonText);
	         
	         
	       } catch (IOException e) {
			e.printStackTrace();
		} finally {
	         try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	       }
		return mainNode;
	   }

	   public static void iterateOnFilesChanged(List<DiffEntry> filesChanged, List<Integer> ticketInformationBugginess, 
			   int appartainVersionIndex, DiffFormatter differenceBetweenCommits, List<Integer> ticketBugFix) {
		   
			ArrayList<Integer> fileMetrics = null;

			// For each file changed in the commit			
				for (DiffEntry singleFileChanged : filesChanged) {
																						
					if (singleFileChanged.getNewPath().endsWith(".java")) {
						
						// Put (if not present) an empty record in the dataset map for the pair (version, filePath)
						String appartainVersion = versionArrayString.get(appartainVersionIndex - 1);
						ticketController.insertEntry(appartainVersion, singleFileChanged.getNewPath());
		
						try {
							fileMetrics = (ArrayList<Integer>) ticketController.calculateMetrics(singleFileChanged, appartainVersion, differenceBetweenCommits, filesChanged, ticketBugFix);
						} catch (CorruptObjectException e) {
							e.printStackTrace();
						} catch (MissingObjectException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// Replace the updated metrics
						fileMapDataset.replace(appartainVersion, singleFileChanged.getNewPath(), fileMetrics);
		
						// Set this and other class contained in [IV, numberOfVersionsFV) buggy (if ther'are ticket(s) associated to the commit)
						ticketController.setBugginess(ticketInformationBugginess, singleFileChanged, indexHalfVersion +1 );
					}
				}
	   }

	   
	   @SuppressWarnings("unchecked")
	   public static void createData(){

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
						
			String gitDirectory = projectName + "/.git";
			Repository directory = null;
			
			try {
				directory = builder.setGitDir(new File(gitDirectory)).readEnvironment().findGitDir().build();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Try to open the Git directory
			try (Git git = new Git(directory)) {

				Iterable<RevCommit> commits = null;

				// Get all commits
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
					
					if (commit.getParentCount() != 0) {

						List<DiffEntry> filesChanged = null;

						// Get the date of the commit
						LocalDate commitLocalDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

						// Get the appartain version of the commit
						int appartainVersionIndex = ticketController.getCommitAppartainVersionIndex(commitLocalDate);
						
						List<Integer> indexVersionCommitCount = new ArrayList<>();
						
						if (appartainVersionIndex < indexHalfVersion + 1){
							
							indexVersionCommitCount.add(appartainVersionIndex);
							
							List<Integer> ticketBugFix = ticketController.getTicketAssociatedCommitBugFix(commit.getFullMessage(), projectName, ticketList);
														
							// Get the list of the ticket (could be empty) associated to the commit
							List<Integer> ticketInformationBugginess = ticketController.getTicketAssociatedCommitBuggy(commit.getFullMessage(), projectName);
							
							// Create a new DiffFormatter, needed to get the change between the commit and his parent
							try (DiffFormatter differenceBetweenCommits = new DiffFormatter(NullOutputStream.INSTANCE)) {

								differenceBetweenCommits.setRepository(directory);

								// Get the difference between the two commit
								try {
									filesChanged = differenceBetweenCommits.scan(commit.getParent(0), commit);
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								iterateOnFilesChanged(filesChanged, ticketInformationBugginess, appartainVersionIndex, differenceBetweenCommits, ticketBugFix);
								
							}
						}
					}
				}
			}

		}
	   
	   public static List<String> getAffectedVersion(JSONObject singleJsonObject) {
		 
		   //get JSONArray associated to the affected versions,
		   List<String> affectedVersionList = new ArrayList<>();
			
		   try {
				
			   //take the affected versions and t
			   JSONArray affectedVersionArray = singleJsonObject.getJSONArray("versions");
			   if (affectedVersionArray.length() > 0) {
				
				   for (int k = 0; k < affectedVersionArray.length(); k++) {
	
					   JSONObject singleAffectedVesion = affectedVersionArray.getJSONObject(k);
					   if (singleAffectedVesion.has(releaseDate)) {
							
						   String affectedVerion = singleAffectedVesion.getString("name");
						   affectedVersionList.add(affectedVerion);
					   }
				   }
			   }
										
		   } catch (JSONException e) {
			   e.printStackTrace();
		   }
		   
		   return affectedVersionList;
	   }
		
	  
	   public static int iterateOnTicket(Integer i, Integer total, Integer j, JSONArray issues) {
	   		
	   		String key = null;
	   		
	   		for (; i < total && i < j; i++) {
	            
	            	JSONObject singleJsonObject = null;
					try {
						singleJsonObject = (JSONObject) issues.getJSONObject(i % 1000).get(fields);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					try {
						key = issues.getJSONObject(i % 1000).get("key").toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}

					
					//get JSONArray associated to the affected versions,
					List<String> affectedVersionList = getAffectedVersion(singleJsonObject);

					ticketList.add(Integer.valueOf(key.split("-")[1]));

					// Calculate the AV index of the ticket [IV, FV)
					try {
						
						String creationDate = singleJsonObject.getString("created").split("T")[0];
						int ticketID = Integer.parseInt(key.split("-")[1]);
												
						ticketController.getBuggyVersionListAV(affectedVersionList, projectName, creationDate, ticketID);
				
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
	         
	   		}
	   		
	   		return i;
	   		
	   	}
	   	
	   public static Multimap<LocalDate, String> getVersionWithReleaseDate() {

			Multimap<LocalDate, String> versionList = MultimapBuilder.treeKeys().linkedListValues().build();
			String releaseName = null;
			String releaseDate = null;
			Integer i;
			versionArrayString = new ArrayList<>();

			// Url for the GET request to get information associated to Jira project
			String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;

			JSONObject json = null;
			json = readJsonFromUrl(url);

			// Get the JSONArray associated to project version
			JSONArray versions = null;
			try {
				versions = json.getJSONArray("versions");
			} catch (JSONException e) {
				e.printStackTrace();
			}	

			for (i = 0; i < versions.length(); i++) {

				try {
					releaseName = versions.getJSONObject(i).get("name").toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				//in SYNCOPE, some tickets do not have the "releaseDate"
				//(e.g. version 1.0.10) 
				//in particular v 1.0.9 and 1.1.7 have the same "releaseDate"
				//I'm ignoring the 1.1.7 and 2.0.0-M3 version
				try {
					
					if(versions.getJSONObject(i).has(releaseDate)) {
						
						releaseDate = versions.getJSONObject(i).get(releaseDate).toString();
						
						if(!(projectName.equals("SYNCOPE") && (releaseName.equals("1.1.7") || releaseName.equals("2.0.0-M3") || releaseName.equals("2.0.8")
								|| releaseName.equals("2.1.1") || releaseName.equals("2.1.2") || releaseName.equals("2.1.3")
								|| releaseName.equals("2.1.4") || releaseName.equals("2.1.6") || releaseName.equals("2.1.7")
								|| releaseName.equals("2.1.8") || releaseName.equals("2.1.5")))) {
							
							versionArrayString.add(releaseName);
							versionList.put(LocalDate.parse(releaseDate), releaseName);
							
						}

					}
					
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
									
			}
						
			// Give an index to each release in the list
			int counter = 1;
			for (LocalDate k : versionList.keySet()) {
				
				versionList.put(k, String.valueOf(counter));	
				counter++;
			}
			
			return versionList;
		}
	   
	   /*
	    * This function insert in the .csv file
	    * all java class, with the version
	    * 
	    */
	   
	   public static void filterFileName() {
		  
		   try (Stream<File> fileStream = Files.walk(Paths.get(projectName)).filter(Files::isRegularFile).map(Path::toFile)){

			   List<File> files = fileStream.collect(Collectors.toList());

			   for (File i : files) {
				
				   if (i.toString().endsWith(".java")) {

						//insert [4.0.0, test.java]
						for (int j = 0; j < indexHalfVersion; j++) ticketController.insertEntry(versionArrayString.get(j), i.toString());
					}
				}
			   
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	   /*
	     * A Multimap is a general way to associate 
	     * keys with arbitrarily many values.
	     * In this case it stores data like
	     * 2011-12-07 = [4.0.0, 1] 
	     */
	   
	   public static void main(String[] args){
		   
		    Multimap<LocalDate, String> dataVersionIndexList;
			
			ticketList = new ArrayList<>();
						
			dataVersionIndexList = getVersionWithReleaseDate();
			
			ticketController = new TicketController(dataVersionIndexList, fileMapDataset);			
			
			indexHalfVersion = (dataVersionIndexList.size() / 4);
			
			filterFileName();
			
	  		items = new ArrayList<>();
	  		mainNode = new JSONObject();
	  		Integer j = 0;
	  		Integer i = 0;
	  		Integer total = 1;
	  		JSONObject json = null;
	  		String url = null;
	  		JSONArray issues = null;

	  		//Get JSON API for closed bugs w/ AV in the project
	  		do {
		         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
		         j = i + 1000;
		         url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
		                + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
		                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&"
		                + i.toString() + "&maxResults=" + j.toString();
				json = readJsonFromUrl(url);
	
				try {
					issues = json.getJSONArray("issues");
				} catch (JSONException e) {
					e.printStackTrace();
				}
		         try {
		        	 
		        	//total number of issues 
					total = json.getInt("total");
				} catch (JSONException e) {
					e.printStackTrace();
				}
		         
		         i = iterateOnTicket(i, total, j, issues); 
	      
	  		}while (i < total);

	  		//calculate P
	  		int proportion = ticketController.getEstimateProportion();

	  		ticketController.getBuggyVersionProportionTicket(proportion);
	  		
	  		createData();
	  			  		
	  		new CSVController(projectName, fileMapDataset);

			}
	   
}

	 