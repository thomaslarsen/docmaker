package net.toften.docmaker.toc;

import net.toften.docmaker.DocPart;


public interface Section {
	DocPart getDocPart();
	
	String getDivOpenTag(TOC t);

	String getDivCloseTag();
	
	boolean isRotated();
	
	String getSectionName();
	
	String getIdAttr(TOC t);
}
