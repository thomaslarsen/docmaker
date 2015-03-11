package net.toften.docmaker.postprocessors;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.toc.TOC;

/**
 * This {@link PostProcessor} applied a list of key/value pairs.
 * <p>
 * It will look for a string with the pattern: "${<key>}" and replace this
 * with a value looked up in the {@link TOC#getMetaData() TOC metadata}.
 * 
 * @author thomaslarsen
 *
 */
public class ApplyKeyValue extends RegexPostProcessor {
	private static Logger lw = Logger.getLogger(ApplyKeyValue.class.getName());	

	private static final String REGEX = "\\$\\{(.*?)\\}";
    private static final Pattern p = Pattern.compile(REGEX);

    public static String resolve(final Properties props, final String value) {
		String resolvedValue = value;

    	boolean matchFound = true;
    	while (matchFound) {
            StringBuffer out = new StringBuffer();
    		matchFound = false;
    		Matcher m = p.matcher(resolvedValue);

    		while (m.find()) {
    			String foundKey = m.group(1);
    			
    			String replaceValue = props.containsKey(foundKey) ? props.getProperty(foundKey) : "KEY: <b>" + foundKey + "</b> NOT FOUND";

    			m.appendReplacement(out, Matcher.quoteReplacement(replaceValue));
    			matchFound = true;
    			lw.fine(foundKey + " -> " + replaceValue);
    		}

    		m.appendTail(out);
			resolvedValue = out.toString();
    	}
    	
    	lw.fine("Processed fragment \"" + value + "\" with properties " + props.toString() + ", returning " + resolvedValue);
    	
    	return resolvedValue;
    }

	@Override
	protected String getRegex() {
		return REGEX;
	}

	@Override
	protected String getReplacement(Matcher m) {
		String value = m.group(1);
		
		lw.fine("Found key: " + value + " in " + getCurrentChapter().getName());
		
		return resolve(getTOC().getMetaData(), "${" + value + "}");
	}
}
