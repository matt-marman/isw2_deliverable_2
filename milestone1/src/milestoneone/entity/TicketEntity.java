package milestoneone.entity;

import java.util.ArrayList;
import java.util.List;

public class TicketEntity {
	
	private List<String> av = new ArrayList<>();
	private double proportion;
	private int id;
	private String resolutionDate;
	private String creationDate;
	private int ivIndex;
	private int ovIndex;
	private int fvIndex;
	
	public List<String> getAv() {
		return av;
	}
	public void setAv(List<String> av) {
		this.av = av;
	}
	public void addAv(String version) {
		this.av.add(version);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIvIndex() {
		return ivIndex;
	}
	public void setIvIndex(int ivIndex) {
		this.ivIndex = ivIndex;
	}
	public int getOvIndex() {
		return ovIndex;
	}
	public void setOvIndex(int ovIndex) {
		this.ovIndex = ovIndex;
	}
	public int getFvIndex() {
		return fvIndex;
	}
	public void setFvIndex(int fvIndex) {
		this.fvIndex = fvIndex;
	}
	public String getResolutionDate() {
		return resolutionDate;
	}
	public void setResolutionDate(String resolutionDate) {
		this.resolutionDate = resolutionDate;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public double getProportion() {
		return proportion;
	}
	public void setProportion(double proportion) {
		this.proportion = proportion;
	}
	
}
