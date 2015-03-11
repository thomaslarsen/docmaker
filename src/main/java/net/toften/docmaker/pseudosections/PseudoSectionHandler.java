package net.toften.docmaker.pseudosections;

import java.util.List;

import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.handler.standard.PseudoSection;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

/**
 * The {@link PseudoSectionHandler} is used to generate the contents of a 
 * section given all the processed sections in the TOC.
 * <p>
 * The input to the pseudo section is a list of all the sections. The HTML contents
 * can be obtained from the {@link Chapter#getAsHtml()} method.
 * 
 * The {@link Chapter}s can be read by iterating over the supplied sections.
 * 
 * @author thomaslarsen
 *
 */
public interface PseudoSectionHandler {
	/**
	 * This method must generate the HTML output of the pseudo section.
	 * <p>
	 * I can use the supplied {@link List} of {@link ChapterSection}s if needed to get to
	 * the {@link Chapter}s or the HTML {@link Chapter#getAsHtml() contents}
	 * of the chapters
	 * 
	 * @param sections the processed sections of the TOC
	 * @param t the {@link TOC} data model
	 * @return the HTML contents of the pseudo section
	 */
	String getSectionAsHtml(TOC t, LogWrapper lw);
	
	/**
	 * Method called when the PseudoSectionHandler is initialized by the {@link PseudoSection} class.
	 * 
	 * @param attributes from the TOC element; Can be <code>null</code>
	 */
	void init(Attributes attributes);
}
