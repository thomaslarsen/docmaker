package net.toften.docmaker;

import org.xml.sax.Attributes;

public class HeaderSection extends PseudoSection {
	
	public HeaderSection(String pSectionHandlerClassname, Attributes attributes) throws Exception {
		super(null, pSectionHandlerClassname, attributes, false);
	}

	@Override
	public String getDivOpenTag(AssemblyHandler handler) {
		return null;
	}
	
	@Override
	protected String getDivClassName() {
		return "header-section";
	}
}
