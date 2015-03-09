package net.toften.docmaker.toc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.output.InterimFileHandler;
import net.toften.docmaker.postprocessors.PostProcessor;


public interface Chapter {

	DocPart getDocPart();
	
	/**
	 * Return the name of the fragment as specified in the title attribute in the chapter element:
	 * 
	 * @return the name of the fragment
	 */
	String getFragmentName();

	public String getAsHtml();
	
	/**
	 * Return the complete <div> open tag to include in the {@link InterimFileHandler interim file}
	 * 
	 * @param t the processed TOC
	 * @return
	 */
	String getDivOpenTag(TOC t);

	String getDivCloseTag();
	
	/**
	 * @return <code>true</code> if the chapter is rotated
	 */
	boolean isRotated();

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
	 * Returns the ID attribute for the chapter.
	 * <p>
	 * This should be used as the value of the id attribute in the <div> tag that surrounds
	 * the chapter section.
	 * This will allow links to reference the chapter section specifically.
	 * 
	 * @param t
	 * @return
	 */
	String getIdAttr(TOC t);

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
