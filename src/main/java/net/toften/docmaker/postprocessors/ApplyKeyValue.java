package net.toften.docmaker.postprocessors;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.LogWrapper;

public class ApplyKeyValue extends RegexPostProcessor {
    private static final String REGEX = "\\$\\{(.*?)\\}";
    private static final Pattern p = Pattern.compile(REGEX);
    private Properties keyValue;

    public ApplyKeyValue() {
    	keyValue = null;
    }
    
    public ApplyKeyValue(Properties keyValue) {
		this.keyValue = keyValue;
	}

    public static String resolve(final Properties props, final String value, final LogWrapper lw) {
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
    			lw.debug(foundKey + " -> " + replaceValue);
    		}

    		m.appendTail(out);
			resolvedValue = out.toString();
    	}
    	
    	lw.debug("Processed fragment \"" + value + "\" with properties " + props.toString() + ", returning " + resolvedValue);
    	
    	return resolvedValue;
    }

	@Override
	protected String getRegex() {
		return REGEX;
	}

	@Override
	protected String getReplacement(Matcher m) {
		Properties props = keyValue == null ? getTOC().getMetaData() : keyValue;
		String value = m.group(1);
		
		lw.debug("Found key: " + value + " in " + getCurrentChapter().getName());
		
		return resolve(props, "${" + value + "}", lw);
	}
}
