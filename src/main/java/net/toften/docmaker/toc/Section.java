package net.toften.docmaker.toc;


public interface Section {
	SectionType getSectionType();

	String getDivOpenTag(TOC t);

	String getDivCloseTag();
	
	String getSectionName();
	
	String getIdAttr(TOC t);
}
