package net.toften.docmaker;

public class Chapter {
	private Section section;
	private String fragmentName;
	private String repo;
	private int chapterLevelOffset;
	private String fragmentAsHtml;
	private boolean isRotated;

	public Chapter(Section section, String fragmentName, String repo, int chapterLevelOffset, String fragmentAsHtml, boolean isRotated) {
		this.section = section;
		this.fragmentName = fragmentName;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		this.fragmentAsHtml = fragmentAsHtml;
		this.isRotated = isRotated;
	}
	
	public Section getSection() {
		return section;
	}
	
	public int getChapterLevelOffset() {
		return chapterLevelOffset;
	}
	
	public String getFragmentAsHtml() {
		return fragmentAsHtml;
	}
	
	public String getFragmentName() {
		return fragmentName;
	}
	
	public String getRepoName() {
		return repo;
	}
	
	public boolean isRotated() {
		return isRotated;
	}
	
	public int calcEffectiveLevel() {
		return DefaultAssemblyHandler.calcEffectiveLevel(getSection().getSectionLevel(), getChapterLevelOffset());
	}
}