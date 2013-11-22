package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class MetaSection extends BaseSection {
	private List<String[]> elements = new LinkedList<String[]>();

	public MetaSection(String sectionName, boolean isRotated) {
		super(sectionName, isRotated);
	}

	public void addElement(String key, String value) {
		elements.add(new String[] { key, value });
	}
	
	public List<String[]> getElements() {
		return elements;
	}
	
	@Override
	protected String getDivClassName() {
		return "meta-section";
	}
}
