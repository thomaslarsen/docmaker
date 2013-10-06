package net.toften.docmaker;

public class Chapter {
	private String fragmentName;
	private int chapterLevelOffset;
	private String fragmentAsHtml;

	public Chapter(String fragmentName, int chapterLevelOffset,
			String fragmentAsHtml) {
		this.fragmentName = fragmentName;
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
}