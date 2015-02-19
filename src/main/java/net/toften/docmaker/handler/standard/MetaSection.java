package net.toften.docmaker.handler.standard;

import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.toc.ElementsSection;
import net.toften.docmaker.toc.SectionType;

public class MetaSection extends BaseSection implements ElementsSection {
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
	
	@Override
	public SectionType getSectionType() {
		return SectionType.META_SECTION;
	}
}
