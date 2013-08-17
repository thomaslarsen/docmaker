package net.toften.docmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;

import net.toften.docmaker.markup.MarkupProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The {@link AbstractAssemblyHandler} will process the TOC file, and assemble the complete
 * result HTML document.
 * <p>
 * The AssemblyHandler extends the {@link DefaultHandler} class, and overrides the 
 * appropriate methods.
 * <p>
 * To use the AssemblyHandler, do the following:
 * <pre>
 * 		SAXParser p = SAXParserFactory.newInstance().newSAXParser();
 *		AssemblyHandler ah = new AssemblyHandler(inputDir, outputDir);
 *		
 *		p.parse(new File("toc.xml"), ah);
 * </pre>
 * 
 * Note, that this class expects the section fragments to have already been converted 
 * into HTML
 * 
 * @author thomaslarsen
 *
 */
public abstract class AbstractAssemblyHandler 
extends 
DefaultHandler 
implements 
ProcessorHandlerCallback, 
DocPartCallback,
OutputFileHandler, AssemblyHandler {
	private FileWriter htmlFile;

	private int currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;

	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	private Map<String, String> metaData = new HashMap<String, String>();
	private Map<String, String> repos = new HashMap<String, String>();
	private URI baseURI;

	private String documentTitle;


	public void parse(SAXParser parser, File tocFile) throws SAXException, IOException {
		parser.parse(tocFile, this);
	}
	
	/* (non-Javadoc)
	 * @see net.toften.docmaker.AssemblyHandler#setMarkupProcessor(net.toften.docmaker.markup.MarkupProcessor)
	 */
	public abstract void setMarkupProcessor(MarkupProcessor markupProcessor);

	public void init(String filename) throws IOException {
		htmlFile = new FileWriter(filename);
	}

	public void close() throws IOException {
		try {
			htmlFile.flush();
		} finally {
			htmlFile.close();
		}
	}

	public void writeToOutputFile(String text) throws IOException {
		if (text != null)
			htmlFile.write(text);
	}

	/* (non-Javadoc)
	 * @see net.toften.docmaker.AssemblyHandler#insertCSSFile(java.lang.String)
	 */
	public void insertCSSFile(String path) {
		// TODO ability to add multiple CSS files
		this.cssFilePath = path;
	}

	/* (non-Javadoc)
	 * @see net.toften.docmaker.AssemblyHandler#setBaseURI(java.net.URI)
	 */
	public void setBaseURI(URI baseURI) {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		this.baseURI = baseURI;
	}

	public int getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	public String getCurrentSectionName() {
		return currentSectionName;
	}

	public String getCurrentFragmentName() {
		return currentFragmentName;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	protected String getCssFilePath() {
		return cssFilePath;
	}
	
	@Override
	public void endDocument() throws SAXException {
		try {
			close();
		} catch (IOException e) {
			throw new SAXException("Outfile could not be closed", e);
		}
	}

	public String[][] getPreElementAttributes(DocPart dp, Attributes attributes) {
		String[][] a = null;

		switch (dp) {
		case CHAPTER:
			currentFragmentName = attributes.getValue("fragment");

			a = new String[][] { { "class", "chapter" }, { "id", getCurrentSectionName() + "-" + getCurrentFragmentName() } };
			break;

		case SECTION:
			currentSectionName = attributes.getValue("title");

			if (attributes.getValue("level") != null) {
				currentSectionLevel = Integer.valueOf(attributes.getValue("level"));

				a = new String[][] { { "class", "section-header" }, { "id", getCurrentSectionName() } };
			} else {
				// When no level is specified, treat this as a metasection
				a = new String[][] { { "class", "meta-section" }, { "id", getCurrentSectionName() } };
			}

		case ELEMENT:
			String key = attributes.getValue("key");
			if (metaData.containsKey(key)) {
				a = new String[][] { { "key", key } };
			}
			break;

		default:
			break;
		}

		return a;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		DocPart dp = DocPart.valueOfString(qName);

		if (dp != null) {
			try {
				writeToOutputFile(dp.preElement(this, attributes));

				switch (dp) {
				case REPO:
					handleRepoElement(attributes);
					break;

				case HEADER:
					handleHeaderElement(attributes);
					break;

				case LINK:
				case META:
					handleMetaElement(qName, attributes);
					break;

				case PROPERTY:
					handlePropertyElement(attributes);
					break;

				case ELEMENT:
					handleElementElement(attributes);
					break;
					
				case SECTION:
					handleSectionElement(attributes);

				case SECTIONS:
					handleSectionsElement(attributes);
					break;

				case CHAPTER:
					handleChapterElement(attributes);
					break;
				}
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			} catch (URISyntaxException e) {
				throw new SAXException("Creating URI for element " + qName + " failed", e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		DocPart dp = DocPart.valueOfString(qName);

		if (dp != null) {
			try {
				writeToOutputFile(dp.postElement());
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			}
		}	
	}

	protected void handleSectionElement(Attributes attributes) {
	}

	protected void handleChapterElement(Attributes attributes) throws URISyntaxException, SAXException, IOException {
		String repo = attributes.getValue("repo");
		if (repos.containsKey(repo)) {
			String repoURIPath = repos.get(repo);
			URI repoURI = new URI(repoURIPath);
			if (!repoURI.isAbsolute()) {
				repoURI = baseURI.resolve(repoURI);
			} else System.out.println("REPO URI IS ABSOLUTE!!!!");

			if (!repoURI.isAbsolute()) {
				throw new SAXException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath);
			}

			int chapterLevelOffset = attributes.getValue("level") == null ? 0 : Integer.valueOf(attributes.getValue("level"));
			String htmlFragment = getFragmentAsHTML(repoURI, getCurrentFragmentName(), chapterLevelOffset);
			writeToOutputFile(htmlFragment);
		} else {
			throw new SAXException("Repo " + repo + " not declared");
		}
	}

	protected void handleSectionsElement(Attributes attributes) throws IOException {
		/*
		 * Just before the fragments, we include a metadata section
		 * This section will include all the metadata defined in the property section
		 */
		writeMetadataElement();
	}

	protected void handleElementElement(Attributes attributes) throws IOException {
		/*
		 * An element is a div tag, that references a metadata key/value pair
		 */
		String key = attributes.getValue("key");
		if (metaData.containsKey(key)) {
			writeToOutputFile(metaData.get(key));
		}
	}

	protected void handlePropertyElement(Attributes attributes) {
		metaData.put(attributes.getValue("key"), attributes.getValue("value"));
	}

	protected void handleMetaElement(String metaName, Attributes attributes) throws IOException {
		writeToOutputFile("<" + metaName);

		for (int i = 0; i < attributes.getLength(); i++) {
			writeToOutputFile(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
		}

		writeToOutputFile("/>");
	}

	protected void handleHeaderElement(Attributes attributes) throws IOException {
		documentTitle = attributes.getValue("title");

		writeTitleElement();

		// Also add the title to the meta data elements
		metaData.put("title", getDocumentTitle());

		// Add the CSS file
		writeCSSElement();
	}

	protected void handleRepoElement(Attributes attributes) {
		// Add the fragment repo to the repo list
		repos.put(attributes.getValue("id"), attributes.getValue("uri"));
	}

	protected void writeCSSElement() throws IOException {
		if (getCssFilePath() != null) {
			writeToOutputFile("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + getCssFilePath() + "\" />");
		}
	}

	protected void writeTitleElement() throws IOException {
		writeToOutputFile("<title>" + getDocumentTitle() + "</title>");
	}

	protected void writeMetadataElement() throws IOException {
		writeToOutputFile("<div class=\"metadata\">");
		for (Map.Entry<String, String> m : metaData.entrySet()) {
			writeToOutputFile("<div class=\"meta\" key=\"" + m.getKey() + "\">");
			writeToOutputFile(m.getValue());
			writeToOutputFile("</div>");
		}
		writeToOutputFile("</div>");
	}

	/**
	 * @param repoURI the URI of the repo where the fragment to add is located
	 * @param fragmentName the name of the fragment to add
	 * @param chapterLevelOffset the chapter level offset to apply to the fragment. This will be added to the {@link #getCurrentSectionLevel() current section level}
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected String getFragmentAsHTML(URI repoURI, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		File inFile = new File(repoURI.resolve(File.separator + fragmentName + ".html"));
		BufferedReader reader = new BufferedReader(new FileReader(inFile));

		StringBuffer asHTML = new StringBuffer();
		String line;
		while( ( line = reader.readLine() ) != null ) {
			if (chapterLevelOffset > 0) {
				line = incrementHTag(line, chapterLevelOffset);
			}

			asHTML.append(line);
		}

		reader.close();

		return asHTML.toString();
	}

	/**
	 * Increment the HTML <code>Hx</code> tag.
	 * <p>
	 * The Hx tag will be incremented with the amount of the <code>increment</code> parameter.
	 * If the line contains more than one Hx tag, they will all be incremented.
	 * 
	 * @param line the line if HTML (potentially) with Hx tag(s)
	 * @param increment the number to increment the Hx tag with
	 * @return
	 */
	public static String incrementHTag(String line, int increment) {
		// Only increase the level if greater than one
		Matcher m = p.matcher(line);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int l = Integer.valueOf(m.group(2));
			m.appendReplacement(sb, "$1" + (l + increment) + "$3");
		}
		m.appendTail(sb);

		return sb.toString();
	}
}