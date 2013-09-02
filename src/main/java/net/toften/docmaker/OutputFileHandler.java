package net.toften.docmaker;

import java.io.IOException;

/**
 * This interface defines the handler of writing the converted markup contents
 * to the interim output file.
 * 
 * @author thomaslarsen
 *
 */
public interface OutputFileHandler {

	/**
	 * Write specified text to the output file.
	 * 
	 * @param text
	 * @throws IOException
	 */
	void writeToOutputFile(String text) throws IOException;
	
	void init(String filename) throws IOException;
	
	void close() throws IOException;
	
	String getFileExtension();
}
