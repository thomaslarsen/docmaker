package net.toften.docmaker.handler.standard;

import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

public class HeaderSection extends PseudoSection {
	
	public HeaderSection(String pSectionHandlerClassname, Attributes attributes) throws Exception {
		super(null, pSectionHandlerClassname, attributes, false);
	}

	@Override
	public String getDivOpenTag(TOC t) {
		return null;
	}
	
	@Override
	public String getDivCloseTag() {
		return null;
	}
	
	@Override
	protected String getDivClassName() {
		return "header-section";
	}
}
