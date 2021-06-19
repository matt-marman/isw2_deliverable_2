package milestoneone.Entity;

import java.util.ArrayList;
import java.util.List;

public class ProjectEntity {
	
	private String name;
	private Integer halfVersion;
	
	private List<String> version = new ArrayList<>();
	private List<FileEntity> fileEntityList = new ArrayList<>();
	private List<VersionEntity> versionEntityList = new ArrayList<>();
	private List<String> listVersionAV = new ArrayList<>();
	private List<TicketEntity> ticketBuggy = new ArrayList<>();
	private List<Integer> commitBuggy = new ArrayList<>();
	private List<TicketEntity> ticketBuggyAV = new ArrayList<>();
	private List<TicketEntity> ticketBuggyNoAV = new ArrayList<>();
	
	public void addVersionEntity(VersionEntity versionEntity) {
		this.versionEntityList.add(versionEntity);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getListVersionAV() {
		return listVersionAV;
	}
	public void setListVersionAV(List<String> listVersionAV) {
		this.listVersionAV = listVersionAV;
	}
	public List<TicketEntity> getTicketBuggyNoAV() {
		return ticketBuggyNoAV;
	}
	public void setTicketBuggyNoAV(List<TicketEntity> ticketBuggyNoAV) {
		this.ticketBuggyNoAV = ticketBuggyNoAV;
	}
	public List<TicketEntity> getTicketBuggyAV() {
		return ticketBuggyAV;
	}
	public void setTicketBuggyAV(List<TicketEntity> ticketBuggyAV) {
		this.ticketBuggyAV = ticketBuggyAV;
	}
	public List<Integer> getCommitBuggy() {
		return commitBuggy;
	}
	public void setCommitBuggy(List<Integer> commitBuggy) {
		this.commitBuggy = commitBuggy;
	}
	public List<TicketEntity> getTicketBuggy() {
		return ticketBuggy;
	}
	public void setTicketBuggy(List<TicketEntity> ticketBuggy) {
		this.ticketBuggy = ticketBuggy;
	}
	public void addTicketBuggy(TicketEntity ticket) {
		this.ticketBuggy.add(ticket);
		
	}
	public void addCommitBuggy(Integer commit) {
		this.commitBuggy.add(commit);
		
	}
	public void addTicketBuggyAV(TicketEntity ticket) {
		this.ticketBuggyAV.add(ticket);
		
	}
	public void addTicketBuggyNoAV(TicketEntity ticket) {
		this.ticketBuggyNoAV.add(ticket);
		
	}
	public List<VersionEntity> getVersionEntityList() {
		return versionEntityList;
	}
	public void setVersionEntityList(List<VersionEntity> versionEntityList) {
		this.versionEntityList = versionEntityList;
	}
	public Integer getHalfVersion() {
		return halfVersion;
	}
	public void setHalfVersion(Integer halfVersion) {
		this.halfVersion = halfVersion;
	}
	public List<FileEntity> getFileEntityList() {
		return fileEntityList;
	}
	public void setFileEntityList(List<FileEntity> fileEntityList) {
		this.fileEntityList = fileEntityList;
	}
	public void addFileToList(FileEntity fileEntity) {
		this.fileEntityList.add(fileEntity);
		
	}
	public List<String> getVersion() {
		return version;
	}
	public void setVersion(List<String> version) {
		this.version = version;
	}
	public void addVersion(String version) {
		this.version.add(version);
	}
}
