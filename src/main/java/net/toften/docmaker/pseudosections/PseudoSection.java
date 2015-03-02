package net.toften.docmaker.pseudosections;

import net.toften.docmaker.handler.standard.BaseSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.SectionType;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

public class PseudoSection extends BaseSection implements GeneratedSection {

	private PseudoSectionHandler sectionHandler;

	public PseudoSection(String sectionName, String pSectionHandlerClassname, Attributes attributes, boolean isRotated) throws Exception {
		super(sectionName, isRotated);
		
		sectionHandler = (PseudoSectionHandler) Class.forName(pSectionHandlerClassname).newInstance();
		sectionHandler.init(attributes);
	}
	
	public PseudoSectionHandler getSectionHandler() {
		return sectionHandler;
	}
	
	@Override
	protected String getDivClassName() {
		return "pseudo-section";
	}
	
	@Override
	public SectionType getSectionType() {
		return SectionType.PSEUDO_SECTION;
	}
	
	@Override
	public String getAsHtml(TOC t) {
		return getSectionHandler().getSectionAsHtml(t);
	}
}
