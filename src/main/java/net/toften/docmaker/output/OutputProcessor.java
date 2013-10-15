package net.toften.docmaker.output;

import java.io.File;

/**
 * Interface describing the processor of the interim output file.
 * <p>
 * The {@link OutputProcessor} is responsible for converting the interim file into
 * the appropriate format and write it to the specified file.
 * 
 * @author tlarsen
 *
 */
public interface OutputProcessor {

	/**
	 * Process and convert the interim file and write it to the file
	 * specified.
	 * 
	 * @param inFile the interim file
	 * @param outFileName the absolute path and filename of the converted file
	 * @throws Exception
	 */
	void process(File inFile, String outFileName) throws Exception;

	/**
	 * @return the file extension used for the converted file
	 */
	String getFileExtension();
}