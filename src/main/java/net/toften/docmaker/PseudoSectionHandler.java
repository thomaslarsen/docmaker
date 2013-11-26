package net.toften.docmaker;

import java.util.List;

import net.toften.docmaker.postprocessors.PostProcessor;

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
public interface PseudoSectionHandler extends PostProcessor {
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
}
