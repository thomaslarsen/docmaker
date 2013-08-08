package net.toften.docmaker.markup;

import java.io.File;
import java.io.IOException;

/**
 * Interface for implementation of a markup processor.
 * <p>
 * The implementing class will likely wrap an external library providing the actual processing.
 * 
 * @author thomaslarsen
 *
 */
public interface MarkupProcessor {
	/**
	 * This method must process a file containing a markup language text. The output must be a String
	 * containing the HTML version of the markup.
	 * 
	 * @param inFile the markup file
	 * @return HTML fragment of the processed markup file
	 * @throws IOException
	 */
	String process(File inFile) throws IOException;

	/**
	 * @return the extension of the markup file
	 */
	String getExtension();
}
