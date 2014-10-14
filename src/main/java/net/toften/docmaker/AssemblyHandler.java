package net.toften.docmaker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.NoMarkupProcessor;

import org.xml.sax.SAXException;

/**
 * The {@link AssemblyHandler} is responsible for converting the TOC into the
 * transient HTML file written as described in the {@link InterimFileHandler}.
 * <p>
 * The AssemblyHandler will use a {@link SAXParser} to process the TOC XML file.
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
public interface AssemblyHandler extends InterimFileHandler {
	
	/**
	 * Gets the markup processor for a specific file extension.
	 *
	 * @param extension the file extension
	 * @return the {@link MarkupProcessor} used to convert the fragments with the given extension
	 */
	MarkupProcessor getMarkupProcessor(String extension);

	/**
	 * Set the markup processors to be used to convert the fragments.
	 *
	 * @param processors the processors to use
	 */
	void setMarkupProcessor(Map<String, MarkupProcessor> processors);

	/**
	 * Specify CSS to be used to style the converted output.
	 * 
	 * @param path path to the CSS file
	 * 
	 * @see InterimFileHandler
	 */
	void insertCSSFile(String path);

	/**
	 * Specify the base URI from where the fragment repositories will be defined.
	 * 
	 * @param baseURI fragment repository base URI
	 */
	void setBaseURI(URI baseURI);

	/**
	 * Parse a TOC file and convert it into the interim file.
	 * 
	 * @param parser the SAX parser to use
	 * @param tocStream the {@link InputStream} from where to read the TOC
	 * @param tocName the name to use for the TOC
	 * @throws SAXException
	 * @throws IOException
	 */
	void parse(SAXParser parser, InputStream tocStream, String tocName) throws SAXException, IOException;
	
	/**
	 * @return the title of the section currently being processed
	 */
	String getCurrentSectionName();
	
	/**
	 * @return the level of the section currently being processed, or <code>null</code>
	 * if the current section is a meta-section
	 */
	Integer getCurrentSectionLevel();

	/**
	 * @return the name of the fragment currently being processed
	 */
	String getCurrentFragmentName();
	
	/**
	 * @return the title of the document
	 */
	String getDocumentTitle();

	/**
	 * @return the id of the repo containing the current fragment
	 */
	String getCurrentRepoName();
	
	/**
	 * @return the filename of the TOC file being processed
	 */
	String getTocFileName();

	/**
	 * Sets the default file extension.
	 *
	 * @param defaultExtension the new default file extension
	 */
	void setDefaultFileExtension(String defaultExtension);
}