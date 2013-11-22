package net.toften.docmaker.pseudosections;

import java.util.regex.Matcher;

public class BQProcessor extends RegexPostProcessor {

	@Override
	protected String getRegex() {
		return "<blockquote>(.*)<strong>(.*)</strong>";
	}

	@Override
	protected String getReplacement(Matcher m) {
		return "<blockquote class=\"$2\">$1<strong>$2</strong>";
	}
}
