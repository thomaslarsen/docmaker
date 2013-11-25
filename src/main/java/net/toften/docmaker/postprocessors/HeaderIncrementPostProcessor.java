package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

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
