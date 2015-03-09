package net.toften.docmaker.handler.standard;

import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public abstract class BaseSection implements Section {
	private String sectionName;
	private boolean isRotated;

	public BaseSection(String sectionName, boolean isRotated) {
		this.sectionName = sectionName;
	}
	
	public String getSectionName() {
		return sectionName;
	}

	public boolean isRotated() {
		return isRotated;
	}
	
	public String getDivOpenTag(TOC t) {
		String classAttr = getDivClassName() + (isRotated() ? " rotate" : "");
		
		return constructDivOpenTag(classAttr, getIdAttr(t), getSectionName());
	}
	
	public String getDivCloseTag() {
		return "</div>" + "\n";
	}
	
	public String getIdAttr(TOC t) {
		return (t.getTocFileName() + "-" + getSectionName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
	
	protected String getDivClassName() {
		return getDocPart().getName();
	}

	protected String constructDivOpenTag(String divClass, String divId, String divTitle) {
		return "<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">" + "\n";
	}
}
