package milestoneone.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import milestoneone.entity.ProjectEntity;
import milestoneone.entity.TicketEntity;
import milestoneone.entity.VersionEntity;

/**
 * Part of this class has been provided by the professor.
 * Essentially it works with Json file using some Jira's API.
 *
 */

public class JSONController {
	
	JSONController() {}
	
	private static final String RELEASE_DATE = "releaseDate";

	private static String readAll(Reader rd) throws IOException {

	 	 StringBuilder sb = new StringBuilder();
		 int cp;
		 while ((cp = rd.read()) != -1) {
			 sb.append((char) cp);
		 }
		 return sb.toString();
	 }
	
	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	 	
		 InputStream is = new URL(url).openStream();
		 JSONObject json = null;
		 
		 try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			
			 json = new JSONObject(readAll(rd));
		
		 } finally {
		 
			 is.close();
		
		 }
		
		 return json;
	 }

	/**
	 * 
	 * Given a projectEntity, the function populate
	 * an array of version with this fields:
	 * 
	 *  - ReleaseDate
	 *  - ReleaseName
	 *  - Index
	 *  
	 *  The array is associated for the projectEntity
	 * 
	 * @param projectEntity
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static ProjectEntity getVersion(ProjectEntity projectEntity) throws IOException, JSONException {

		Integer i;

		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectEntity.getName();

		JSONObject json = readJsonFromUrl(url);

		JSONArray versions = json.getJSONArray("versions");	

		for (i = 0; i < versions.length(); i++) {

			if (versions.getJSONObject(i).has(RELEASE_DATE)) {
				
				String releaseName = versions.getJSONObject(i).get("name").toString();
				LocalDate localDate = LocalDate.parse(versions.getJSONObject(i).get(RELEASE_DATE).toString());
				
				/*
				 * in SYNCOPE, some tickets do not have the "releaseDate"
				 * (e.g. version 1.0.10) 
				 * in particular v 1.0.9 and 1.1.7 have the same "releaseDate"
				 * I'm ignoring the 1.1.7 and 2.0.0-M3 version
				 * 
				 */
				
				if(!(projectEntity.getName().equals("SYNCOPE") && (releaseName.equals("1.1.7") || releaseName.equals("2.0.0-M3") || releaseName.equals("2.0.8")
						|| releaseName.equals("2.1.1") || releaseName.equals("2.1.2") || releaseName.equals("2.1.3")
						|| releaseName.equals("2.1.4") || releaseName.equals("2.1.6") || releaseName.equals("2.1.7")
						|| releaseName.equals("2.1.8") || releaseName.equals("2.1.5")))) {
					
					VersionEntity versionEntity = new VersionEntity(); 
					versionEntity.setReleaseDate(localDate);
					versionEntity.setReleaseName(releaseName);
					
					projectEntity.addVersionEntity(versionEntity);
					projectEntity.addVersion(releaseName);
					
				}
			}
		}

		//give an index to each version, in this way i'm working more easily
		for(int k = 0; k < projectEntity.getVersionEntityList().size(); k++) {
			
			VersionEntity versionEntity = projectEntity.getVersionEntityList().get(k);
			versionEntity.setIndex(k + 1);
			
		}
			
		return orderVersion(projectEntity);
			
	}
	
	
	private static ProjectEntity orderVersion(ProjectEntity projectEntity) {
		
		for(int i = 0; i < projectEntity.getVersion().size() - 1; i++) {
			
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
				
		return projectEntity;
	}


	public static ProjectEntity createTicket(ProjectEntity projectEntity) throws IOException, JSONException {

		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		String key = null;

		TicketController ticketController = new TicketController();
		
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

			for (; i < total && i < j; i++) {

				JSONObject singleJsonObject = (JSONObject) issues.getJSONObject(i % 1000).get("fields");

				key = issues.getJSONObject(i % 1000).get("key").toString();

				JSONArray affectedVersionArray = singleJsonObject.getJSONArray("versions");

				TicketEntity ticketEntity = new TicketEntity();
				ticketEntity.setId(Integer.valueOf(key.split("-")[1]));
				ticketEntity.setCreationDate(singleJsonObject.getString("created").split("T")[0]);
				ticketEntity.setResolutionDate(singleJsonObject.getString("resolutiondate").split("T")[0]);
								
				projectEntity.addTicketBuggy(ticketEntity);
				
				setAffectedVersion(affectedVersionArray, ticketEntity);

				ticketController.setVersions(ticketEntity, projectEntity);

			}
		} while (i < total);

		return projectEntity;
	}
	
	/**
	 * For each release in "versions" field, it is added to av list
	 * of ticket.
	 * 
	 * @param json
	 * @param ticketEntity
	 * @throws JSONException
	 */
	
	private static void setAffectedVersion(JSONArray json, TicketEntity ticketEntity) throws JSONException{

		if (json.length() > 0) {

			for (int k = 0; k < json.length(); k++) {

				JSONObject singleRelease = json.getJSONObject(k);

				if (singleRelease.has(RELEASE_DATE)) {
					
					ticketEntity.addAv(singleRelease.getString("name"));
				}
			}
		}
	}
}
