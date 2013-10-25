package net.toften.docmaker;

import net.toften.docmaker.maven.DocMakerMojo;

public class PseudoSection extends MetaSection {

	private PseudoSectionHandler sectionHandler;

	public PseudoSection(String sectionName, String pSectionHandlerClassname) throws Exception {
		super(sectionName);
		
		sectionHandler = DocMakerMojo.newInstance(PseudoSectionHandler.class, pSectionHandlerClassname);
	}
	
	public PseudoSectionHandler getSectionHandler() {
		return sectionHandler;
	}
}
