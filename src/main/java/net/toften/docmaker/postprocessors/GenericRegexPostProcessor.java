package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

import org.xml.sax.Attributes;

public class GenericRegexPostProcessor extends RegexPostProcessor {

	private String regex;
	private String replacement;
	
	@Override
	public void init(Attributes attributes) {
		super.init(attributes);
		
		regex = attributes.getValue("regex");
		replacement = attributes.getValue("replacement");
	}
	
	@Override
	protected String getRegex() {
		return regex;
	}

	@Override
	protected String getReplacement(Matcher m) {
		return replacement;
	}
}
