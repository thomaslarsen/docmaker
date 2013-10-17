package net.toften.docmaker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.SAXParser;

import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.NoMarkupProcessor;

import org.xml.sax.SAXException;

/**
 * The {@link AssemblyHandler} is responsible for converting the TOC into the
 * interim file written as described in the {@link InterimFileHandler}.
 * <p>
 * The AssemblyHandler will use a {@link SAXParser} to process the TOC XML file.
 * 
 * <h2>Fragments</h2>
 * The fragments defined in the TOC will be written in markup.
 * It is the responsibility of the AssemblyHandler to convert these fragments into the
 * interim file format using the {@link #setMarkupProcessor(MarkupProcessor) specified}
 * {@link MarkupProcessor}.
 * 
 * If the fragments are already in the appropriate formate, the {@link NoMarkupProcessor}
 * should be used.
 * 
 * @author tlarsen
 *
 */
public interface AssemblyHandler extends InterimFileHandler {
	/**
	 * @return the {@link MarkupProcessor} used to convert the fragments
	 */
	MarkupProcessor getMarkupProcessor();

	/**
	 * Set the markup processor to be used to convert the fragments
	 * 
	 * @param markupProcessor
	 */
	void setMarkupProcessor(MarkupProcessor markupProcessor);

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
}