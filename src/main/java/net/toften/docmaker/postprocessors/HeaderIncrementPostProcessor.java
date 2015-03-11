package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

import net.toften.docmaker.handler.standard.ContentSection;
import net.toften.docmaker.handler.standard.FragmentChapter;

/**
 * This {@link PostProcessor} will normalise the header level, given the base level of
 * the {@link ContentSection#getSectionLevel() section} and the {@link FragmentChapter#getChapterLevelOffset() chapter}.
 * <p>
 * The <i>effective level</i> of the heading is calculated using the {@link FragmentChapter#calcEffectiveLevel()}
 * method.
 * 
 * @author thomaslarsen
 *
 */
public class HeaderIncrementPostProcessor extends RegexPostProcessor {
	@Override
	protected String getRegex() {
		return "(</?h)(\\d)(>)";
	}

	@Override
	protected String getReplacement(Matcher m) {
		int increment = getCurrentChapter().calcEffectiveLevel();

		return "$1" + (increment  + Integer.valueOf(m.group(2))) + "$3";
	}
}
