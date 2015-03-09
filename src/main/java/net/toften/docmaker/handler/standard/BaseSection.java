package net.toften.docmaker.handler.standard;

import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public abstract class BaseSection implements Section {
	private String name;
	private boolean isRotated;

	public BaseSection(String sectionName, boolean isRotated) {
		this.name = sectionName;
		this.isRotated = isRotated;
	}
	
	public String getName() {
		return name;
	}

	public boolean isRotated() {
		return isRotated;
	}
	
	public String getDivOpenTag(TOC t) {
		String classAttr = getDivClassName() + (isRotated() ? " rotate" : "");
		
		return constructDivOpenTag(classAttr, getIdAttr(t), getName());
	}
	
	public String getDivCloseTag() {
		return "</div>" + "\n";
	}
	
	public String getIdAttr(TOC t) {
		return (t.getTocFileName() + "-" + getName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
	
	protected String getDivClassName() {
		return getDocPart().getName();
	}

	protected String constructDivOpenTag(String divClass, String divId, String divTitle) {
		return "<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">" + "\n";
	}
}
