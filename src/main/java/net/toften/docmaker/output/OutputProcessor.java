package net.toften.docmaker.output;

import java.io.File;

import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.toc.TOC;

/**
 * Interface describing the processor of the interim output file.
 * <p>
 * The {@link OutputProcessor} is responsible for converting the interim file into
 * the appropriate format and write it to the specified file or files.
 * 
 * @author tlarsen
 *
 */
public interface OutputProcessor {

	/**
	 * Process and convert the interim file and write it to the file
	 * specified.
	 * <p>
	 * The OutputProcessor must create and write
	 * 
	 * @param inFile the interim file
	 * @param outputDir the absolute path the converted file; if this is the path to a file
	 * 	and the implementing can support output to a single file, this can be used as
	 * 	the output file
	 * @param outputName the name of the output file, or group of files dependent on the
	 * 	behaviour of the implementing class
	 * @throws Exception
	 */
	void process(File outputDir, String outputName, String encoding, TOC t, LogWrapper lw) throws Exception;
	
	public String getFileExtension();
}