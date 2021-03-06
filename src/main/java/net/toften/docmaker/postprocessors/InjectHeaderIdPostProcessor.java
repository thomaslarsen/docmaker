package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.standard.FragmentChapter;

/**
 * This {@link PostProcessor} will inject an id attribute into all HTML
 * header ({@code <h>}) elements.
 * <p>
 * The value of the injected id attribute is the value of the {@link FragmentChapter#getIdAttr(AssemblyHandler) chapter id}
 * attribute with the text in the header appended.
 * 
 * @author thomaslarsen
 *
 */
public class InjectHeaderIdPostProcessor extends RegexPostProcessor {
	public static final String HEADER_SEARCH_REGEX = "<h(\\d)(.*?)>(.*?)</h\\d>";
	
	@Override
	protected String getRegex() {
		return HEADER_SEARCH_REGEX;
	}

	/**
	 * This method will read the header text and use it to generate the id, and then rebuild the complete header tag.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	protected String getReplacement(Matcher m) {
		return "<h$1 id=\"" + calcElementId(m.group(3)) + "\">$3</h$1>";
	}
}
