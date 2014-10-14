package net.toften.docmaker;

import java.util.List;

import org.xml.sax.Attributes;

/**
 * The {@link PseudoSectionHandler} is used to generate the contents of a 
 * section given all the processed sections in the TOC.
 * <p>
 * The input to the pseudo section is a list of all the sections. The HTML contents
 * can be obtained from the {@link Chapter#getFragmentAsHtml()} method.
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
	 * I can use the supplied {@link List} of {@link Section}s if needed to get to
	 * the {@link Chapter}s or the HTML {@link Chapter#getFragmentAsHtml() contents}
	 * of the chapters
	 * 
	 * @param sections the processed sections of the TOC
	 * @param handler the {@link AssemblyHandler} processing the TOC
	 * @return the HTML contents of the pseudo section
	 */
	String getSectionAsHtml(final List<BaseSection> sections, final AssemblyHandler handler);
	
	/**
	 * Method called when the PseudoSectionHandler is initialized by the {@link PseudoSection} class.
	 * 
	 * @param attributes from the TOC element; Can be <code>null</code>
	 */
	void init(Attributes attributes);
}
