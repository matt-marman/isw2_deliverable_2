package milestonetwo;

import java.util.ArrayList;

import weka.core.Instance;
	
public class PartEntity {
	
	private String version;
	private ArrayList<Instance> instances;
	
	public PartEntity() {
		this.instances = new ArrayList<Instance>();
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public ArrayList<Instance> getInstances() {
		return instances;
	}
	public void setInstances(ArrayList<Instance> instances) {
		this.instances = instances;
	}
	
	public void addIstance(Instance instance) {
		this.instances.add(instance);
	}

}
