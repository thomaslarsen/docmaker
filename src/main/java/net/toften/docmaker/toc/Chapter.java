package net.toften.docmaker.toc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.postprocessors.PostProcessor;

public interface Chapter extends GeneratedSection {
	/**
	 * Return the value the header tags in the {@link Chapter#getAsHtml() HTML} should be incremented by.
	 * <p>
	 * Examples:
	 * 	SL	CL	EL	+
	 * 	1	0	1	0
	 * 	1	1	2	1
	 * 	1	2	3	2
	 * 	2	0	2	1
	 * 	2	1	3	2
	 *
	 * Where:
	 * SL is the {@link ChapterSection#getSectionLevel() section level}
	 * CL is the {@link Chapter#getChapterLevelOffset() chapter level}
	 * EL is the effective base level the header sections in the chapter should start with
	 * + is the actual adjustment
	 * 
	 * @return the actual adjustment
	 */
	int calcEffectiveLevel();
	
	/**
	 * Return the level of the chapter as specified in the level attribute in the chapter element
	 * 
	 * @return
	 */
	int getChapterLevelOffset();

	/**
	 * Run a List of {@link PostProcessor}s over the {@link Chapter#getAsHtml() HTML} of the chapter.
	 * <p>
	 * Note, the output should only be applied to the {@link Chapter#getAsHtml() HTML} if the apply parameter is true.
	 * <p>
	 * The return value should be the compounded output of running all the {@link PostProcessor}s sequentially in order.
	 * 
	 * @param postProcessors
	 * @param t
	 * @param apply <code>true</code> if the output should be retained
	 * @return
	 */
	String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply);

	/**
	 * Return the {@link Repo} from where the fragment is loaded.
	 * 
	 * @return the Repo from where the fragment file is loaded
	 * @see Repo#getFragmentURI(String)
	 * @see #getFragmentURI()
	 */
	Repo getRepo();
	
	/**
	 * @return the parent section of the chapter
	 */
	ChapterSection getSection();
	
	/**
	 * Return the {@link URI} of the fragment file.
	 * <p>
	 * This is a convenience method that invokes the {@link #getRepo()}.{@link Repo#getFragmentURI(String)} method.
	 * 
	 * @return the URI of the fragment file
	 * @throws URISyntaxException
	 */
	URI getFragmentURI() throws URISyntaxException;
}
