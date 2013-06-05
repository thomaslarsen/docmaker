package net.toften.docmaker;

import java.io.File;
import java.io.IOException;

/**
 * Interface for implementation of a Markdown processor.
 * <p>
 * The implementing class will likely wrap an external library providing the actual processing.
 * 
 * @author thomaslarsen
 *
 */
public interface MDProcessor {
	/**
	 * This method must process a file containing Markdown. The output must be a String
	 * containing the HTML version of the Markdown.
	 * 
	 * @param inFile the Markdown file
	 * @return HTML version of the processed Markdown file
	 * @throws IOException
	 */
	String process(File inFile) throws IOException;
}
