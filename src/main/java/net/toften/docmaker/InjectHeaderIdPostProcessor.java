package net.toften.docmaker;

import java.util.regex.Matcher;

import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.postprocessors.RegexPostProcessor;

/**
 * This {@link PostProcessor} will inject an id attribute into all HTML
 * header ({@code <h>}) elements.
 * <p>
 * The value of the injected id attribute is the value of the {@link Chapter#getIdAttr(AssemblyHandler) chapter id}
 * attribute with the text in the header appended.
 * 
 * @author thomaslarsen
 *
 */
public class InjectHeaderIdPostProcessor extends RegexPostProcessor {

	@Override
	protected String getRegex() {
		return "<h(\\d)>(.*?)</h\\d>";
	}

	/**
	 * This method will read the header text and use it to generate the id, and then rebuild the complete header tag.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	protected String getReplacement(Matcher m) {
		String headerText = m.group(2);
		
		String headerId = (getCurrentChapter().getIdAttr(getCurrentHandler()) + "-" + headerText).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
		
		return "<h$1 id=\"" + headerId + "\">$2</h$1>";
	}
}
