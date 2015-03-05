package net.toften.docmaker.toc;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.Repo;

/**
 * This interface represents a single TOC. It provides a data model
 * of an XML TOC file.
 * <br>
 * The {@link AssemblyHandler#parse(java.io.InputStream, String, String, URI, Map, Properties)} method
 * will return an instance of this interface.
 * 
 * @author thomaslarsen
 *
 */
public interface TOC {

	Properties getMetaData();

	/**
	 * This method returns a {@link Map} of the HTML header elements.
	 * <br>
	 * The HTML head section can contain the following elements:
	 * * link
	 * * base
	 * * meta
	 * 
	 * The key of the outer Map contains the head element name.
	 * The inner Map contains the attributes of the head element.
	 * 
	 * @return
	 * 
	 * @see <a href="http://www.w3schools.com/html/html_head.asp">HTML Head</a>
	 */
	Map<String, Map<String, String>> getHtmlMeta();

	List<GeneratedSection> getHeaderSections();

	List<Section> getSections();
	
	/**
	 * @return a List of paths to specified stylesheets
	 */
	List<String> getStyleSheets();

	/**
	 * @return The filename of the XML TOC file without the extension
	 */
	String getTocFileName();

	/**
	 * If a {@link Repo} has a relative path, then this path is
	 * base on this {@link URI}
	 * 
	 * @return The base {@link URI}
	 * 
	 */
	URI getBaseURI();
	
	String getDocumentTitle();
}
