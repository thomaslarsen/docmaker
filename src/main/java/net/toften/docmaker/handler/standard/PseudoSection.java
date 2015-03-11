package net.toften.docmaker.handler.standard;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.pseudosections.PseudoSectionHandler;
import net.toften.docmaker.toc.GeneratedSection;
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
	public DocPart getDocPart() {
		return DocPart.PSECTION;
	}
	
	@Override
	public String getAsHtml(TOC t) {
		return getSectionHandler().getSectionAsHtml(t);
	}
}
