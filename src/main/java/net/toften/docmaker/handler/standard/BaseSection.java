package net.toften.docmaker.handler.standard;

import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

/**
 * This abstract class is the base for all {@link Section} implementations
 * part of the {@link StandardHandler} implementation.
 * 
 * @author thomaslarsen
 *
 */
public abstract class BaseSection implements Section {
	private final String name;
	private final boolean isRotated;

	public BaseSection(String sectionName, boolean isRotated) {
		this.name = sectionName;
		this.isRotated = isRotated;
	}
	
	public final String getName() {
		return name;
	}

	public final boolean isRotated() {
		return isRotated;
	}
	
	public String getDivOpenTag(TOC t) {
		return constructDivOpenTag(getDivClassName() + (isRotated() ? " rotate" : ""), getIdAttr(t), getName());
	}
	
	public String getDivCloseTag() {
		return "</div>" + "\n";
	}
	
	public String getIdAttr(TOC t) {
		return (t.getTocFileName() + "-" + getName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
	
	/**
	 * Must return the class name(s) to use in the <div> open tag.
	 * <p>
	 * This defaults to the name of the section element in the TOC XML 
	 * 
	 * @return class name to use in the <div> open tag
	 * @see net.toften.docmaker.DocPart#getName()
	 */
	protected String getDivClassName() {
		return getDocPart().getName();
	}

	protected String constructDivOpenTag(String divClass, String divId, String divTitle) {
		return "<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">" + "\n";
	}
}
