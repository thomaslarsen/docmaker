package net.toften.docmaker;

public class Chapter {
	private String fragmentName;
	private String repo;
	private int chapterLevelOffset;
	private String fragmentAsHtml;
	private boolean isRotated;

	public Chapter(String fragmentName, String repo, int chapterLevelOffset, String fragmentAsHtml, boolean isRotated) {
		this.fragmentName = fragmentName;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		this.fragmentAsHtml = fragmentAsHtml;
		this.isRotated = isRotated;
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
}