package net.toften.docmaker;

public class BaseSection {
	private String sectionName;
	private boolean isRotated;

	public BaseSection(String sectionName, boolean isRotated) {
		this.sectionName = sectionName;
	}
	
	public String getSectionName() {
		return sectionName;
	}

	public boolean isRotated() {
		return isRotated;
	}
}
