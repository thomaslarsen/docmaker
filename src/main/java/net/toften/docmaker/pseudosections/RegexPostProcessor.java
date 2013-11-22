package net.toften.docmaker.pseudosections;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.Chapter;
import net.toften.docmaker.PostProcessor;

public abstract class RegexPostProcessor implements PostProcessor {

	private Pattern p;
	private Chapter currentChapter;
	private AssemblyHandler currentHandler;
	
	public RegexPostProcessor() {
		p = Pattern.compile(getRegex());
	}
	
	protected abstract String getRegex();
	protected abstract String getReplacement(Matcher m);

	@Override
	public void init(Attributes attributes) {
		// Empty
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, AssemblyHandler handler) {
		Matcher m = p.matcher(fragmentAsHtml);
		currentChapter = chapter;
		currentHandler = handler;
		
		while (m.find()) {
			m.appendReplacement(out, getReplacement(m));
		}
		
		m.appendTail(out);
	}
	
	protected Chapter getCurrentChapter() {
		return currentChapter;
	}
	
	protected AssemblyHandler getCurrentHandler() {
		return currentHandler;
	}
}
