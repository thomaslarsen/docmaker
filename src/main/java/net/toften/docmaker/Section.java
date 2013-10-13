package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class Section extends MetaSection {
	private List<Chapter> chapters = new LinkedList<Chapter>();
	private Integer sectionLevel;

	public Section(String sectionName, Integer sectionLevel) {
		super(sectionName);
		this.sectionLevel = sectionLevel;
	}

	public void addChapter(String fragmentName, int chapterLevelOffset, String fragmentAsHtml) {
		chapters.add(new Chapter(fragmentName, chapterLevelOffset, fragmentAsHtml));
	}
	
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	public Integer getSectionLevel() {
		return sectionLevel;
	}
}