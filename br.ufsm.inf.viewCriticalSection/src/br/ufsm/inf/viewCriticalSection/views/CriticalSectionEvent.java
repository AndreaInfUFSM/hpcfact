package br.ufsm.inf.viewCriticalSection.views;

import org.eclipse.core.resources.IMarker;


/**
 * @author Dionatan Kitzmann Tietzmann
 */

public class CriticalSectionEvent {
	
	private IMarker marker;
	private String image;
	private String type;
	private String fileName;
	private String details;
	private String projectName;
	
	public IMarker getMarker() {
		if (marker == null)
			return null;
		return marker;
	}
	public void setMaker(IMarker marker) {
		this.marker = marker;
	}
	
	public String getImage() {
		if (image == null)
			return "";
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getDetails() {
		if (details == null)
			return "";
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public String getFileName() {
		if (fileName == null)
			return "";
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getType() {
		if (type == null)
			return "";
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getProjectName() {
		if (projectName == null)
			return "";
		return projectName;
	}
	public String toString(){
		return getProjectName()+"\t"+getFileName()+"\t"+getDetails()+"\t"+getType()+"\t"+getMarker();
		
	}
}