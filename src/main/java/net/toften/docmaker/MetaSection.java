package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class MetaSection {
	private List<String[]> elements = new LinkedList<String[]>();
	private String sectionName;

	public MetaSection(String sectionName) {
		this.sectionName = sectionName;
	}

	public void addElement(String key, String value) {
		elements.add(new String[] { key, value });
	}
	
	public List<String[]> getElements() {
		return elements;
	}
	
	public String getSectionName() {
		return sectionName;
	}
}
