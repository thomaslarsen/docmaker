package net.toften.docmaker.postprocessors;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String processFragment(final Properties keyValue, final String fragmentAsHtml) {
    	boolean matchFound = true;

		String value = fragmentAsHtml;

    	while (matchFound) {
            StringBuffer out = new StringBuffer();
    		matchFound = false;
    		Matcher m = p.matcher(value);

    		while (m.find()) {
    			String foundKey = m.group(1);
    			
    			String replaceValue = keyValue.containsKey(foundKey) ? keyValue.getProperty(foundKey) : "KEY: <b>" + foundKey + "</b> NOT FOUND";

    			m.appendReplacement(out, Matcher.quoteReplacement(replaceValue));
    			matchFound = true;
    		}

    		m.appendTail(out);
			value = out.toString();
    	}
    	
    	return value;
    }

	@Override
	protected String getRegex() {
		return REGEX;
	}

	@Override
	protected String getReplacement(Matcher m) {
		return processFragment(keyValue == null ? getTOC().getMetaData() : keyValue, m.group(1));
	}
}
