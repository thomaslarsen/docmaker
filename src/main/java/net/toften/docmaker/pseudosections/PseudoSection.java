package net.toften.docmaker.pseudosections;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.handler.standard.BaseSection;
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
	public String getAsHtml(TOC t, LogWrapper lw) {
		return getSectionHandler().getSectionAsHtml(t, lw);
	}
}
