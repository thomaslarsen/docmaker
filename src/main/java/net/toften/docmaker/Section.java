package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class Section {
	private List<Chapter> chapters = new LinkedList<Chapter>();
	private List<String[]> elements = new LinkedList<String[]>();
	private String sectionName;
	private Integer sectionLevel;

	public Section(String sectionName, Integer sectionLevel) {
		this.sectionName = sectionName;
		this.sectionLevel = sectionLevel;
	}

	public void addChapter(String fragmentName, int chapterLevelOffset, String fragmentAsHtml) {
		chapters.add(new Chapter(fragmentName, chapterLevelOffset, fragmentAsHtml));
	}

	public void addElement(String key, String value) {
		elements.add(new String[] { key, value });
	}
	
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	public List<String[]> getElements() {
		return elements;
	}
	
	public Integer getSectionLevel() {
		return sectionLevel;
	}
	
	public String getSectionName() {
		return sectionName;
	}
}