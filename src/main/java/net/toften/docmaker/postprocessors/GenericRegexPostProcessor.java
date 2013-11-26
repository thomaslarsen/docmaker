package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

import org.xml.sax.Attributes;

/**
 * This {@link PostProcessor} will read a search and replacement regex from
 * the TOC, and then process the HTML fragments using those.
 * <p>
 * The search and replacement regexs are specified in the TOC:
 * 
 * {@code
 * 	...
 * 		<processors>
 * 			<postprocessor 
 * 				classname="net.toften.docmaker.postprocessors.GenericRegexPostProcessor" 
 * 				regex="<div(.*)class=\"(.*?)\">" 
 * 				replacement="<div$1class=\"myclass $2\">"
 * 				/>
 * 		...
 * 	...
 * }
 * 
 * The above example will insert {@code myclass} into any existing {@code class} attribute of all {@code <div>} elements.
 * 
 * @author thomaslarsen
 *
 */
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
