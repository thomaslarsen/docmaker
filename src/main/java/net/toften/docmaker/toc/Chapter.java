package net.toften.docmaker.toc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.postprocessors.PostProcessor;


public interface Chapter {

	String getFragmentName();
	
	String getAsHtml();

	String getDivOpenTag(TOC t);

	String getDivCloseTag();

	int calcEffectiveLevel();

	String getIdAttr(TOC t);

	String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply);

	Repo getRepo();
	
	ChapterSection getSection();
	
	URI getFragmentURI() throws URISyntaxException;
}
