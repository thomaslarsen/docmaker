package net.toften.docmaker;

import java.util.regex.Matcher;

import net.toften.docmaker.pseudosections.RegexPostProcessor;

public class InjectHeaderIdPostProcessor extends RegexPostProcessor {

	@Override
	protected String getRegex() {
		return "<h(\\d)>(.*?)</h\\d>";
	}

	@Override
	protected String getReplacement(Matcher m) {
		String headerText = m.group(2);
		
		String headerId = (getCurrentChapter().getIdAttr(getCurrentHandler()) + "-" + headerText).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
		
		return "<h$1 id=\"" + headerId + "\">$2</h$1>";
	}
}
