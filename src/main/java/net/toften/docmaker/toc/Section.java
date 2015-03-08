package net.toften.docmaker.toc;


public interface Section {
	SectionType getSectionType();

	String getDivOpenTag(TOC t);

	String getDivCloseTag();
	
	boolean isRotated();
	
	String getSectionName();
	
	String getIdAttr(TOC t);
}
