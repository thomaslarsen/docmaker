package net.toften.docmaker.postprocessors;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.Chapter;
import net.toften.docmaker.Section;
import net.toften.docmaker.markup.MarkupProcessor;

import org.xml.sax.Attributes;

/**
 * The PostProcessor is used to add the ability to change the HTML generated for
 * each fragment.
 * <p>
 * The PostProcessor will be run after the TOC has been processed and all the fragments
 * has been {@link MarkupProcessor converted} into HTML.
 * The PostProcessor will be invoked for each {@link Chapter} that is part of a 
 * {@link Section contents section}. The PostProcessor must emit the output of
 * the processed HTML fragment to the supplied {@link StringBuffer}.
 * 
 * @author tlarsen
 *
 */
public interface PostProcessor {
	/**
	 * This method will be invoked for each {@link Chapter} specified in the TOC.
	 * The method must process the supplied fragmentAsHtml and emit the output
	 * to the supplied {@link StringBuffer}.
	 * 
	 * Note, the {@link Chapter#getFragmentAsHtml()} method should <b>not</b> be used
	 * to obtain the HTML fragment to process.
	 * 
	 * @param chapter the chapter to process
	 * @param fragmentAsHtml the HTML fragment to process
	 * @param out the {@link StringBuffer} to write the output to
	 * @param handler the {@link AssemblyHandler} processing the TOC
	 */
	void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, AssemblyHandler handler);

	/**
	 * Method called when the PostProcessor is specified in the TOC.
	 * 
	 * If the PostProcessor is included directly from the {@link AssemblyHandler}, this method is not invoked
	 * as no attributes are available. The {@link PostProcessor} should then do all necessary initialization in
	 * the default constructor.
	 * 
	 * @param attributes from the TOC element; Can be <code>null</code>
	 */
	void init(Attributes attributes);
}
