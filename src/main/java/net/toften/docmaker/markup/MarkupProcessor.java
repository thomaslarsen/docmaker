package net.toften.docmaker.markup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.toften.docmaker.handler.AssemblyHandler;

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
	String process(File inFile, String config, AssemblyHandler handler) throws IOException;
	
	/**
	 * This method must process a file containing a markup language text. The output must be a String
	 * containing the HTML version of the markup.
	 * 
	 * @param inFile the markup file
	 * @return HTML fragment of the processed markup file
	 * @throws IOException
	 */
	String process(InputStream is, String config, AssemblyHandler handler) throws IOException;
	
	/**
	 * This method must process a String containing a markup language text. The output must be a String
	 * containing the HTML version of the markup.
	 * 
	 * @param inString the markup
	 * @param config
	 * @return HTML fragment of the processed markup file
	 * @throws IOException
	 */
	String process(String inString, String config, AssemblyHandler handler) throws IOException;

	/**
	 * @return the extension of the markup file
	 */
	String getFileExtension();
	
	/**
	 * Sets the Charset encoding string to be used.
	 * 
	 * @param encodingString a valid charset.
	 */
	void setEncoding(String encodingString);
}
