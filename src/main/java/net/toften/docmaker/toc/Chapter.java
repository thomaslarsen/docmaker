package net.toften.docmaker.toc;

import java.util.List;

import net.toften.docmaker.postprocessors.PostProcessor;


public interface Chapter {

	String getAsHtml();

	String getDivOpenTag(TOC t);

	String getDivCloseTag();

	int calcEffectiveLevel();

	String getIdAttr(TOC t);

	String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply);

}
