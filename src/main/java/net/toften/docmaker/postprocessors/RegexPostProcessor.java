package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

/**
 * This implementation of the {@link PostProcessor} is used as a baseclass
 * for any implementation that wants to use a Regex to perform a search and
 * replace of the fragment HTML.
 * <p>
 * The processor will try to match a {@link #getRegex() regex} in the HTML fragment
 * and for each successfull {@link Matcher#find()} it will perform a {@link Matcher#appendReplacement(StringBuffer, String) replacement}
 * using the {@link #getReplacement(Matcher) string supplied}.
 * 
 * @author thomaslarsen
 *
 */
public abstract class RegexPostProcessor implements PostProcessor {
	private final Pattern p;
	private Chapter currentChapter;
	private TOC currentTOC;
	
	public RegexPostProcessor() {
		p = Pattern.compile(getRegex());
	}
	
	/**
	 * @return the Regex to search for
	 */
	protected abstract String getRegex();
	
	/**
	 * The Regex replacement string to use.
	 * <p>
	 * Note, the {@link Matcher#appendReplacement(StringBuffer, String)} method should not be invoked
	 * by this method.
	 * 
	 * @param m the Matcher that found the match
	 * @return the Regex replacement string
	 */
	protected abstract String getReplacement(Matcher m);

	@Override
	public void init(Attributes attributes) {
		// Empty
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, TOC t) {
		Matcher m = p.matcher(fragmentAsHtml);
		currentChapter = chapter;
		this.currentTOC = t;
		
		while (m.find()) {
			m.appendReplacement(out, getReplacement(m));
		}
		
		m.appendTail(out);
	}
	
	/**
	 * @return the {@link Chapter} currently being processed
	 */
	protected Chapter getCurrentChapter() {
		return currentChapter;
	}
	
	/**
	 * @return the {@link TOC} data model
	 */
	protected TOC getTOC() {
		return currentTOC;
	}
	
	protected String calcElementId(String elementText) {
		return getCurrentChapter().getIdAttr(getTOC()) + "-" + elementText.trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
}
