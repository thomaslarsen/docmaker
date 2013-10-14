package net.toften.docmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;

import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.NoMarkupProcessor;

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
 * <h2>Chapter offset</h2>
 * 
 * 
 * @author thomaslarsen
 *
 */
public class AbstractAssemblyHandler 
extends 
DefaultHandler 
implements 
ProcessorHandlerCallback, 
DocPartCallback,
OutputFileHandler, 
AssemblyHandler {
	public static class GenericFileHandler implements OutputFileHandler {
		private FileWriter htmlFile;

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
		
		public String getFileExtension() {
			return "html";
		}
	}
	
	private Integer currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;
	private String tocFileName;
	private OutputFileHandler currentFileHandler = new GenericFileHandler();

	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	protected Map<String, String> metaData = new HashMap<String, String>();
	private Map<String, URI> repos = new HashMap<String, URI>();
	private URI baseURI;

	private String documentTitle;

	private String currentRepoName;
	private MarkupProcessor markupProcessor;

	public AbstractAssemblyHandler() {
		currentFileHandler = new GenericFileHandler();
		markupProcessor = new NoMarkupProcessor();
	}

	public void writeToOutputFile(String text) throws IOException {
		currentFileHandler.writeToOutputFile(text);
	}

	public void init(String filename) throws IOException {
		currentFileHandler.init(filename);
	}

	public void close() throws IOException {
		currentFileHandler.close();
	}

	public String getFileExtension() {
		return currentFileHandler.getFileExtension();
	}
	
	public void setCurrentFileHandler(OutputFileHandler currentFileHandler) {
		this.currentFileHandler = currentFileHandler;
	}

	@Override
	public void parse(SAXParser parser, InputStream tocStream, String tocName) throws SAXException, IOException {
		tocFileName = tocName.replaceFirst("[.][^.]+$", "");
		parser.parse(tocStream, this);
	}
	
	@Override
	public void setMarkupProcessor(MarkupProcessor markupProcessor) {
		this.markupProcessor = markupProcessor;
	}
	
	@Override
	public MarkupProcessor getMarkupProcessor() {
		return markupProcessor;
	}

	public void insertCSSFile(String path) {
		// TODO ability to add multiple CSS files
		this.cssFilePath = path.replace('\\', '/');
	}

	public void setBaseURI(URI baseURI) {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		this.baseURI = baseURI;
	}

	public Integer getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	public String getCurrentSectionName() {
		return currentSectionName;
	}

	public String getCurrentFragmentName() {
		return currentFragmentName;
	}
	
	public String getCurrentRepoName() {
		return currentRepoName;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}
	
	public String getTocFileName() {
		return tocFileName;
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

			a = new String[][] { { "class", "chapter" }, { "id", (tocFileName + "-" + getCurrentSectionName() + "-" + getCurrentFragmentName()).toLowerCase().replace(' ', '-') } };
			break;

		case SECTION:
			currentSectionName = attributes.getValue("title");
			currentSectionLevel = Integer.valueOf(attributes.getValue("level"));

			if (currentSectionLevel != null) {
				a = new String[][] { { "class", "section-header" }, { "id", (tocFileName + "-" + getCurrentSectionName()).toLowerCase().replace(' ', '-') } };
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
				writeToOutputFile(dp.preElement());

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
					break;
					
				case METASECTION:
					handleMetaSectionElement(attributes);
					break;
					
				case SECTIONS:
					handleSectionsElement(attributes);
					break;

				case CHAPTER:
					handleChapterElement(attributes);
					break;
					
				default:
					handleUnknownElement(dp, attributes);
					break;
				}
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			} catch (URISyntaxException e) {
				throw new SAXException("Creating URI for element " + qName + " failed", e);
			}
		}
	}
	
	protected void handleUnknownElement(DocPart dp, Attributes attributes) {
		// Empty
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		DocPart dp = DocPart.valueOfString(qName);
		
		if (dp != null) {
			try {
				switch (dp) {
				case SECTION:
				case METASECTION:
				case PSECTION:
				case CHAPTER:
					writeDivCloseTag();
					break;
					
				default:
					break;
				}

				writeToOutputFile(dp.postElement());
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			}
		}	
	}

	protected void handleSectionElement(Attributes attributes) throws IOException {
		if (attributes.getValue("level") == null) {
			// Included for backwards compatibility
			handleMetaSectionElement(attributes);
		} else {
			currentSectionName = attributes.getValue("title");
			currentSectionLevel = Integer.valueOf(attributes.getValue("level"));
			writeStandardSectionDivOpenTag(currentSectionName);
		}
	}

	protected void handleMetaSectionElement(Attributes attributes) throws IOException {
		currentSectionName = attributes.getValue("title");
		currentSectionLevel = null;

		writeMetaSectionDivOpenTag(currentSectionName);
	}

	protected void handleChapterElement(Attributes attributes) throws URISyntaxException, SAXException, IOException {
		currentFragmentName = attributes.getValue("fragment");

		currentRepoName = attributes.getValue("repo");
		if (repos.containsKey(currentRepoName)) {
			// Write the chapter div tag
			writeChapterDivOpenTag(getCurrentSectionName(), currentFragmentName);

			int chapterLevelOffset = attributes.getValue("level") == null ? 0 : Integer.valueOf(attributes.getValue("level"));
			String htmlFragment = getFragmentAsHTML(repos.get(currentRepoName), currentFragmentName, chapterLevelOffset + getCurrentSectionLevel() - 2);
			
			writeToOutputFile(htmlFragment);
		} else {
			throw new SAXException("Repo " + currentRepoName + " not declared");
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
			writeElement(key, metaData.get(key));
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

	protected void handleRepoElement(Attributes attributes) throws URISyntaxException, SAXException {
		// Add the fragment repo to the repo list
		String repoId = attributes.getValue("id");
		String repoURIPath = attributes.getValue("uri");
		
		if (!repos.containsKey(repoId)) {
			URI repoURI = new URI(repoURIPath);
			if (!repoURI.isAbsolute()) {
				repoURI = baseURI.resolve(repoURI);
			} else 
				System.out.println("REPO URI IS ABSOLUTE!!!!");
	
			if (!repoURI.isAbsolute()) {
				throw new SAXException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath);
			}
	
			if (repoURI.getAuthority() != null) {
				throw new SAXException("Repo URI " + repoURI.toString() + " has an authority (" + repoURI.getAuthority() + "), given " + repoURIPath);
			}
	
			repos.put(repoId, repoURI);
		}
	}

	protected void writeCSSElement() throws IOException {
		if (getCssFilePath() != null) {
			writeToOutputFile("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + getCssFilePath() + "\" />");
		}
	}

	/**
	 * Writes the title element.
	 * 
	 * The title element is part of the HTML header. 
	 * 
	 * <h2>TOC</h2>
	 * The title is an attribute part of the TOC header element.
	 * <p>
	 * {@code
	 * 		<title>DocTitle</title>
	 * }
	 * @throws IOException
	 */
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
	
	protected void writeElement(String key, String value) throws IOException {
		writeToOutputFile("<div key=\"" + key + "\">");
		writeToOutputFile(value);
		writeDivCloseTag();
	}

	protected void writeChapterDivOpenTag(String sectionName, String fragmentName) throws IOException {
		writeDivOpenTag("chapter", (getTocFileName() + "-" + getCurrentRepoName() + "-" + sectionName + "-" + fragmentName).toLowerCase().replace(' ', '-'));
	}
	
	protected void writeMetaSectionDivOpenTag(String sectionName) throws IOException {
		writeDivOpenTag("meta-section", (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}
	
	protected void writeStandardSectionDivOpenTag(String sectionName) throws IOException {
		writeDivOpenTag("section-header", (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}

	protected void writeDivOpenTag(String divClass, String divId) throws IOException {
		writeToOutputFile("<div class=\"" + divClass + "\" id=\"" + divId + "\">");
	}

	protected void writeDivOpenTag(String divClass, String divId, String divName) throws IOException {
		writeToOutputFile("<div class=\"" + divClass + "\" id=\"" + divId + "\" name=\"" + divName + "\">");
	}

	protected void writeDivCloseTag() throws IOException {
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
		if (!repoURI.isAbsolute())
			throw new IllegalArgumentException("The repo URI " + repoURI.toString() + " is not absolute");
		
		URI markupFilenameURI = new URI(fragmentName + "." + getMarkupProcessor().getFileExtension());
		File markupFile = new File(repoURI.resolve(markupFilenameURI));

		if (!markupFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + markupFile.getAbsolutePath().toString());
		}

		String asHtml = getMarkupProcessor().process(markupFile);
		if (chapterLevelOffset > 0) {
			asHtml = incrementHTag(asHtml, chapterLevelOffset);
		}

		return asHtml;
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
