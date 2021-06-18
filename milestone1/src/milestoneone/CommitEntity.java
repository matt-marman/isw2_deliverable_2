package milestoneone;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;

public class CommitEntity {
	
	private LocalDate date;
	private int appartainVersion;
	private List<TicketEntity> ticketEntityList;
	private String message;
	private List<DiffEntry> filesChanged;
	
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public int getAppartainVersion() {
		return appartainVersion;
	}
	public void setAppartainVersion(int appartainVersion) {
		this.appartainVersion = appartainVersion;
	}
	public List<TicketEntity> getTicketEntityList() {
		return ticketEntityList;
	}
	public void setTicketEntityList(List<TicketEntity> ticketEntityList) {
		this.ticketEntityList = ticketEntityList;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<DiffEntry> getFilesChanged() {
		return filesChanged;
	}
	public void setFilesChanged(List<DiffEntry> filesChanged) {
		this.filesChanged = filesChanged;
	}


}
