package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class MetaSection extends BaseSection {
	private List<String[]> elements = new LinkedList<String[]>();

	public MetaSection(String sectionName) {
		super(sectionName);
	}

	public void addElement(String key, String value) {
		elements.add(new String[] { key, value });
	}
	
	public List<String[]> getElements() {
		return elements;
	}
}
