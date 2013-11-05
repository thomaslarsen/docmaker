package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
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
 * The {@link DefaultAssemblyHandler} will process the TOC file, and assemble the complete
 * result HTML document.
 * <p>
 * The AssemblyHandler extends the {@link DefaultHandler} class, and overrides the 
 * appropriate methods.
 * 
 * <h2>Handlers & Writers</h2>
 * This methods in this class broadly falls in two categories: Handlers and Writers.
 * <p>
 * Handlers are used to deal with the actions of each element in the TOC.
 * For example the {@link #handleChapterElement(Attributes)} method will be invoked
 * when a {@link DocPart#CHAPTER} element is encountered in the TOC.
 * <p>
 * Writers are used to output HTML to the output file. A number of Writers exist for various
 * HTML elements, for example {@link #writeTitleElement()} for outputting the HTML title element.
 * 
 * Additionally a number of helper writers exists:
 * <ul>
 * <li>{@link #writeDivOpenTag(String, String)}</li>
 * <li>{@link #writeDivOpenTag(String, String, String)}</li>
 * <li>{@link #writeDivCloseTag()}</li>
 * </ul>
 * 
 * All writers should be using the {@link InterimFileHandler#writeToOutputFile(String)} method
 * to write the text to the file. The reference to this handler can be found using the
 * {@link #getCurrentFileHandler()} method.
 * 
 * @author thomaslarsen
 *
 */
public class DefaultAssemblyHandler 
extends 
DefaultHandler 
implements 
InterimFileHandler, 
AssemblyHandler {
	public static class GenericFileHandler implements InterimFileHandler {
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
	
	protected Map<String, String> metaData = new HashMap<String, String>();
	protected Map<String, URI> repos = new HashMap<String, URI>();

	public static String headerRegex = "(\\</?h)(\\d)(>)";
	
	private static Pattern p = Pattern.compile(headerRegex);

	private Integer currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;
	private String tocFileName;
	private InterimFileHandler currentFileHandler;

	private URI baseURI;
	private String documentTitle;
	private String currentRepoName;
	private MarkupProcessor markupProcessor;

	public DefaultAssemblyHandler() {
		currentFileHandler = new GenericFileHandler();
		markupProcessor = new NoMarkupProcessor();
	}

	@Override
	public void writeToOutputFile(String text) throws IOException {
		currentFileHandler.writeToOutputFile(text);
	}

	@Override
	public void init(String filename) throws IOException {
		currentFileHandler.init(filename);
	}

	@Override
	public void close() throws IOException {
		currentFileHandler.close();
	}

	@Override
	public String getFileExtension() {
		return currentFileHandler.getFileExtension();
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

	@Override
	public void insertCSSFile(String path) {
		// TODO ability to add multiple CSS files
		this.cssFilePath = path.replace('\\', '/');
	}

	@Override
	public void setBaseURI(URI baseURI) {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		this.baseURI = baseURI;
	}

	@Override
	public Integer getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	@Override
	public String getCurrentSectionName() {
		return currentSectionName;
	}
	
	protected void setCurrentSectionName(String currentSectionName) {
		this.currentSectionName = currentSectionName;
	}

	@Override
	public String getCurrentFragmentName() {
		return currentFragmentName;
	}

	@Override
	public String getDocumentTitle() {
		return documentTitle;
	}
	
	public String getCurrentRepoName() {
		return currentRepoName;
	}

	public String getTocFileName() {
		return tocFileName;
	}
	
	public InterimFileHandler getCurrentFileHandler() {
		return currentFileHandler;
	}
	
	protected void setCurrentFileHandler(InterimFileHandler currentFileHandler) {
		this.currentFileHandler = currentFileHandler;
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
			} catch (Exception e) {
				throw new SAXException("Element " + qName + " failed", e);
			}
		}
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

	protected void handleUnknownElement(DocPart dp, Attributes attributes) throws Exception {
		// Empty
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
			writeChapterDivOpenTag(getCurrentSectionName(), currentFragmentName, getCurrentRepoName());

			int chapterLevelOffset = attributes.getValue("level") == null ? 0 : Integer.valueOf(attributes.getValue("level"));
			int normalisedOffset = calcEffectiveLevel(getCurrentSectionLevel(), chapterLevelOffset);
			String htmlFragment = getFragmentAsHTML(currentRepoName, currentFragmentName, chapterLevelOffset);
			
			if (normalisedOffset > 0) {
				htmlFragment = incrementHTag(htmlFragment, normalisedOffset);
			}

			htmlFragment = injectHeaderIdAttributes(htmlFragment, getTocFileName(), currentRepoName, getCurrentSectionName(), currentFragmentName);

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
				if (baseURI != null) {
					repoURI = baseURI.resolve(repoURI);
				} else {
					throw new SAXException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath + " AND baseURI is null");
				}
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
			writeDivCloseTag();
		}
		writeDivCloseTag();
	}
	
	protected void writeElement(String key, String value) throws IOException {
		writeToOutputFile("<div key=\"" + key + "\">");
		writeToOutputFile(value);
		writeDivCloseTag();
	}

	protected void writeChapterDivOpenTag(String sectionName, String fragmentName, String repoName) throws IOException {
		writeDivOpenTag("chapter", (getTocFileName() + "-" + repoName + "-" + sectionName + "-" + fragmentName).toLowerCase().replace(' ', '-'));
	}
	
	protected void writeMetaSectionDivOpenTag(String sectionName) throws IOException {
		writeDivOpenTag("meta-section", (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}
	
	protected void writeStandardSectionDivOpenTag(String sectionName) throws IOException {
		writeDivOpenTag("section-header", (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}

	/**
	 * Writes a {@code <div>} tag to the output file, including a {@code class} and
	 * {@code id} attribute.
	 * 
	 * @param divClass the value of the {@code class} attribute
	 * @param divId the value of the {@code id} attribute
	 * @throws IOException
	 */
	protected void writeDivOpenTag(String divClass, String divId) throws IOException { 
		writeToOutputFile("<div class=\"" + divClass + "\" id=\"" + divId + "\">"); 
	}

	/**
	 * Writes a {@code <div>} tag to the output file, including a {@code class},
	 * {@code id} and {@code name} attribute.
	 * 
	 * @param divClass the value of the {@code class} attribute
	 * @param divId the value of the {@code id} attribute
	 * @param divName the value of the {@code name} attribute
	 * @throws IOException
	 */
	protected void writeDivOpenTag(String divClass, String divId, String divName) throws IOException {
		writeToOutputFile("<div class=\"" + divClass + "\" id=\"" + divId + "\" name=\"" + divName + "\">");
	}

	/**
	 * Writes a {@code </div>} close tag to the output file.
	 * 
	 * @throws IOException
	 */
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
	protected String getFragmentAsHTML(String repoName, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		URI repoURI = repos.get(repoName);
		
		if (!repoURI.isAbsolute())
			throw new IllegalArgumentException("The repo URI " + repoURI.toString() + " is not absolute");
		
		URI markupFilenameURI = new URI(fragmentName + "." + getMarkupProcessor().getFileExtension());
		File markupFile = new File(repoURI.resolve(markupFilenameURI));

		if (!markupFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + markupFile.getAbsolutePath().toString());
		}

		String asHtml = getMarkupProcessor().process(markupFile);

		return asHtml;
	}
	
	/**
	 * This method returns the effective base heading level of a chapter.
	 * <p>
	 * Examples:
	 * 	SL	CL	EL	+
	 * 	1	0	1	0
	 * 	1	1	2	1
	 * 	1	2	3	2
	 * 	2	0	2	1
	 * 	2	1	3	2
	 * 
	 * @param currentSectionLevel
	 * @param chapterLevelOffset
	 * @return
	 */
	public static int calcEffectiveLevel(int currentSectionLevel, int chapterLevelOffset) {
		return chapterLevelOffset + currentSectionLevel - 1;
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
		if (line != null) {
			Matcher m = p.matcher(line);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				int l = Integer.valueOf(m.group(2));
				m.appendReplacement(sb, "$1" + (l + increment) + "$3");
			}
			m.appendTail(sb);

			return sb.toString();
		} else
			return null;
	}

	public static String injectHeaderIdAttributes(String htmlFragment, String tocFileName, String repoName, String sectionName, String fragmentName) {
		if (htmlFragment != null) {
			Matcher m = p.matcher(htmlFragment);
	
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				if (m.group(0).charAt(1) != '/') { // Ignore rouge close tags
					// Handle open tag
					int l = Integer.valueOf(m.group(2));
					int start = m.end();
					m.appendReplacement(sb, ""); // Remove the open tag
					
					// Handle close tag
					m.find();
					int end = m.start();
					
					String headerText = htmlFragment.substring(start, end);
					String headerId = (tocFileName + "-" + repoName + "-" + sectionName + "-" + fragmentName + "-" + headerText).toLowerCase().replace(' ', '-');
					String hReplace = "<h" + l + " id=\"" + headerId + "\">" + headerText + "</h" + l + ">";
					
					// Insert the new tag
					m.appendReplacement(sb, hReplace);
	
					// Delete the heading title that has been inserted by default
					sb.delete(sb.length() - headerText.length() - hReplace.length(), sb.length() - hReplace.length());
				} else
					m.appendReplacement(sb, "$1$2$3");
			}
			m.appendTail(sb);
	
			return sb.toString();
		} else
			return null;
	}
}
