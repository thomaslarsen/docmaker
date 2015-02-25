package net.toften.docmaker.handler;

import java.io.File;
import java.io.IOException;

/**
 * This interface defines the handler of writing the converted markup contents
 * to the interim output file.
 * <p>
 * This file is usually an HTML file, but can be otherwise.
 * 
 * @author thomaslarsen
 *
 * @see net.toften.docmaker.output.OutputProcessor
 */
public interface InterimFileHandler {

	/**
	 * Write specified text to the output file.
	 * 
	 * @param text
	 * @throws IOException
	 */
	void writeToOutputFile(String text) throws IOException;
	
	/**
	 * Initialise the output file, so it is ready to be {@link #writeToOutputFile(String) written} to.
	 * 
	 * @param filename the absolute filename of the output file
	 * @param encodingString a valid charset.
	 * @throws IOException
	 */
	void init(File interimFileDir, String filename, String encodingString) throws IOException;
	
	/**
	 * Flush and close the output file.
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;
	
	/**
	 * @return the extension of the output file; usually {@code html}
	 */
	String getFileExtension();
}
