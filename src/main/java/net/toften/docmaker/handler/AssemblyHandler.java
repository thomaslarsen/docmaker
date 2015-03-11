package net.toften.docmaker.handler;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.NoMarkupProcessor;
import net.toften.docmaker.toc.TOC;

/**
 * The {@link AssemblyHandler} is responsible for converting the TOC into the
 * {@link TOC TOC model}.
 * <p>
 * 
 * <h2>Fragments</h2>
 * The fragments defined in the TOC will be written in markup.
 * It is the responsibility of the AssemblyHandler to convert these fragments into the
 * interim file format using the {@link #setMarkupProcessor(MarkupProcessor) specified}
 * {@link MarkupProcessor}.
 * 
 * If the fragments are already in the appropriate format, the {@link NoMarkupProcessor}
 * should be used.
 * 
 * @author tlarsen
 *
 */
public interface AssemblyHandler {
	
	/**
	 * Returns the markup processor for a specific file extension.
	 *
	 * @param extension the file extension
	 * @return the {@link MarkupProcessor} used to convert the fragments with the given extension
	 */
	MarkupProcessor getMarkupProcessor(String extension);

	/**
	 * Parse a TOC file and convert it into the interim file.
	 * 
	 * @param lw 
	 * @param tocStream the {@link InputStream} from where to read the TOC
	 * @param tocName the name to use for the TOC
	 * @param defaultExtension the new default file extension
	 * @param baseURI fragment repository base URI
	 * @param processors the processors to use
	 * @param cssFilePath List of CSS files to include
	 * @throws Exception
	 */
	TOC parse(LogWrapper lw, InputStream tocStream, String tocName, String defaultExtension, URI baseURI, Map<String, MarkupProcessor> processors, Properties baseProperties, List<String> cssFilePath) throws Exception;
	
	/**
	 * @return the title of the document
	 */
	String getDocumentTitle();

	/**
	 * This method must return the filename portion of the TOC file, i.e.
	 * the filename <i>without</i> the file extension.
	 * 
	 * @return the filename of the TOC file being processed
	 */
	String getTocFileName();

	/**
	 * Return the file extension to append to fragment files, that is not
	 * specified with an extension
	 * 
	 * @return the default fragment file extension
	 */
	String getDefaultExtension();
}