package net.toften.docmaker.handler.standard;

import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.SectionType;

public class ContentSection extends MetaSection implements ChapterSection {
	private List<Chapter> chapters = new LinkedList<Chapter>();
	private Integer sectionLevel;

	public ContentSection(String sectionName, Integer sectionLevel, boolean isRotated) {
		super(sectionName, isRotated);
		this.sectionLevel = sectionLevel;
	}
	
	public void addChapter(String fragmentName, String config, AssemblyHandler handler, Repo repo, int chapterLevelOffset, boolean isRotated) throws Exception {
		chapters.add(new TOCChapter(this, fragmentName, config, handler, repo, chapterLevelOffset, isRotated));
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
	
	@Override
	public SectionType getSectionType() {
		return SectionType.CONTENTS_SECTION;
	}
}