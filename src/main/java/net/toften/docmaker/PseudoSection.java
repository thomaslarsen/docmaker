package net.toften.docmaker;

import org.xml.sax.Attributes;

import net.toften.docmaker.maven.DocMakerMojo;

public class PseudoSection extends BaseSection {

	private PseudoSectionHandler sectionHandler;

	public PseudoSection(String sectionName, String pSectionHandlerClassname, Attributes attributes) throws Exception {
		super(sectionName);
		
		sectionHandler = DocMakerMojo.newInstance(PseudoSectionHandler.class, pSectionHandlerClassname);
		sectionHandler.init(attributes);
	}
	
	public PseudoSectionHandler getSectionHandler() {
		return sectionHandler;
	}
}
