package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

import net.toften.docmaker.Chapter;
import net.toften.docmaker.Section;

/**
 * This {@link PostProcessor} will normalise the header level, given the base level of
 * the {@link Section#getSectionLevel() section} and the {@link Chapter#getChapterLevelOffset() chapter}.
 * <p>
 * The <i>effective level</i> of the heading is calculated using the {@link Chapter#calcEffectiveLevel()}
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
