package net.toften.docmaker;

public class Chapter {
	private String fragmentName;
	String repo;
	private int chapterLevelOffset;
	private String fragmentAsHtml;

	public Chapter(String fragmentName, String repo, int chapterLevelOffset, String fragmentAsHtml) {
		this.fragmentName = fragmentName;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		this.fragmentAsHtml = fragmentAsHtml;
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
}