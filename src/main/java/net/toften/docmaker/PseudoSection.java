package net.toften.docmaker;

public class PseudoSection extends MetaSection {

	private String pSectionHandlerClassname;

	public PseudoSection(String sectionName, String pSectionHandlerClassname) {
		super(sectionName);
		
		this.pSectionHandlerClassname = pSectionHandlerClassname;
	}
	
	
}
