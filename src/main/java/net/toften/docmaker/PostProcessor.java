package net.toften.docmaker;

import org.xml.sax.Attributes;

/**
 * The PostProcessor is used to add the ability to change the HTML generated for
 * each fragment.
 * <p>
 * 
 * 
 * @author tlarsen
 *
 */
public interface PostProcessor {
	void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, AssemblyHandler handler);

	void init(Attributes attributes);
}
