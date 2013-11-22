package net.toften.docmaker;

import java.util.LinkedList;
import java.util.List;

public class Section extends MetaSection {
	private List<Chapter> chapters = new LinkedList<Chapter>();
	private Integer sectionLevel;

	public Section(String sectionName, Integer sectionLevel, boolean isRotated) {
		super(sectionName, isRotated);
		this.sectionLevel = sectionLevel;
	}

	public void addChapter(String fragmentName, String repo, int chapterLevelOffset, String fragmentAsHtml, boolean isRotated) {
		chapters.add(new Chapter(this, fragmentName, repo, chapterLevelOffset, fragmentAsHtml, isRotated));
	}
	
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	public Integer getSectionLevel() {
		return sectionLevel;
	}
	
	@Override
	protected String getDivClassName() {
		return "section-header";
	}
}