package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.InterimFileHandler;
import net.toften.docmaker.handler.Repo;
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
		private OutputStreamWriter htmlFile;

		public void init(final String filename, final String encodingString) throws IOException {
			this.htmlFile = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName(encodingString).newEncoder());
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
	protected Map<String, Repo> repos = new HashMap<String, Repo>();

	public static String headerRegex = "(\\</?h)(\\d)(>)";
	public static final int EFFECTIVE_LEVEL_ADJUSTMENT = 2;
	
	private static Pattern p = Pattern.compile(headerRegex);

	private Integer currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;
	private String tocFileName;
	private InterimFileHandler currentFileHandler;

	private URI baseURI;
	private String documentTitle;
	private Repo currentRepoName;
	private Map<String, MarkupProcessor> markupProcessor;
	private boolean rotateCurrentSection;
	private boolean rotateCurrentChapter;
	private String defaultExtension;

	public DefaultAssemblyHandler() {
		currentFileHandler = new GenericFileHandler();
		markupProcessor = Collections.singletonMap("md", (MarkupProcessor)(new NoMarkupProcessor()));
	}

	@Override
	public void writeToOutputFile(String text) throws IOException {
		currentFileHandler.writeToOutputFile(text);
	}

	@Override
	public void init(final String filename, final String encodingString) throws IOException {
		this.currentFileHandler.init(filename, encodingString);
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
	public void parse(InputStream tocStream, String tocName, String defaultExtension, URI baseURI, Map<String, MarkupProcessor> markupProcessor) throws Exception {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		this.baseURI = baseURI;
		this.markupProcessor = markupProcessor;
		this.defaultExtension = defaultExtension;
		tocFileName = tocName.replaceFirst("[.][^.]+$", "");
        // Create the SAX parser
        SAXParser p;
            p = SAXParserFactory.newInstance().newSAXParser();
		p.parse(tocStream, this);
	}
	
	@Override
	public MarkupProcessor getMarkupProcessor(String extension) {
		return markupProcessor.get(extension);
	}

	@Override
	public void insertCSSFile(String path) {
		// TODO ability to add multiple CSS files
		this.cssFilePath = path.replace('\\', '/');
	}

	@Override
	public Integer getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	@Override
	public String getCurrentSectionName() {
		return currentSectionName;
	}
	
	@Override
	public boolean isCurrentSectionRotated() {
		return rotateCurrentSection;
	}
	
	public boolean isRotateCurrentChapter() {
		return rotateCurrentChapter;
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
	
	public Repo getCurrentRepo() {
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
//				case CHAPTER:
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

	protected void handleSectionElement(Attributes attributes) throws IOException, SAXException {
		if (attributes.getValue("title") == null)
			throw new SAXException("Section title attribute not specified");
		
		if (attributes.getValue("level") == null)
			throw new SAXException("Section level attribute not specified");
		
		currentSectionName = attributes.getValue("title");
		currentSectionLevel = Integer.valueOf(attributes.getValue("level"));
		
		rotateCurrentSection = attributes.getValue("rotate") != null;

		writeStandardSectionDivOpenTag(currentSectionName, rotateCurrentSection);
	}

	protected void handleMetaSectionElement(Attributes attributes) throws IOException, SAXException {
		if (attributes.getValue("title") == null)
			throw new SAXException("Section title attribute not specified");
		
		currentSectionName = attributes.getValue("title");
		currentSectionLevel = null;
		
		rotateCurrentSection = attributes.getValue("rotate") != null;

		writeMetaSectionDivOpenTag(currentSectionName, rotateCurrentSection);
	}

	protected void handleChapterElement(Attributes attributes) throws Exception {
		if (attributes.getValue("fragment") == null)
			throw new SAXException("Chapter fragment attribute not specified");
		
		if (attributes.getValue("repo") == null)
			throw new SAXException("Chapter repo attribute not specified");
		
		currentFragmentName = attributes.getValue("fragment");
		
		rotateCurrentChapter = attributes.getValue("rotate") != null;
		
		String chapterConfig = attributes.getValue("config");

		currentRepoName = repos.get(attributes.getValue("repo"));
		if (repos.containsKey(currentRepoName.getId())) {
			// Write the chapter div tag
//			writeChapterDivOpenTag(getCurrentSectionName(), currentFragmentName, currentRepoName, rotateCurrentChapter);

			int chapterLevelOffset = attributes.getValue("level") == null ? 0 : Integer.valueOf(attributes.getValue("level"));
			int normalisedOffset = calcEffectiveLevel(getCurrentSectionLevel(), chapterLevelOffset);
			
			try {
				String htmlFragment = getFragmentAsHTML(currentRepoName, currentFragmentName, chapterLevelOffset, chapterConfig);
				
				if (normalisedOffset > 0) {
					htmlFragment = incrementHTag(htmlFragment, normalisedOffset);
				}
	
				htmlFragment = injectHeaderIdAttributes(htmlFragment, getTocFileName(), currentRepoName, getCurrentSectionName(), currentFragmentName);
	
				writeToOutputFile(htmlFragment);
			} catch (URISyntaxException e) {
				throw new SAXException("Fragment " + currentFragmentName + " could not be converted", e);
			}
		} else {
			throw new SAXException("Repo " + currentRepoName.getId() + " not declared");
		}
	}

	protected void handleSectionsElement(Attributes attributes) throws IOException, SAXException {
		/*
		 * Just before the fragments, we include a metadata section
		 * This section will include all the metadata defined in the property section
		 */
		writeMetadataElement();
	}

	protected void handleElementElement(Attributes attributes) throws IOException, SAXException {
		/*
		 * An element is a div tag, that references a metadata key/value pair
		 */
		String key = attributes.getValue("key");
		if (metaData.containsKey(key)) {
			writeElement(key, metaData.get(key));
		}
	}

	protected void handlePropertyElement(Attributes attributes) throws IOException, SAXException {
		metaData.put(attributes.getValue("key"), attributes.getValue("value"));
	}

	protected void handleMetaElement(String metaName, Attributes attributes) throws IOException, SAXException {
		writeToOutputFile("<" + metaName);

		for (int i = 0; i < attributes.getLength(); i++) {
			writeToOutputFile(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
		}

		writeToOutputFile("/>");
	}

	protected void handleHeaderElement(Attributes attributes) throws IOException, SAXException {
		documentTitle = attributes.getValue("title");

		writeTitleElement();

		// Also add the title to the meta data elements
		metaData.put("title", getDocumentTitle());

		// Add the CSS file
		writeCSSElement();
	}

	protected void handleRepoElement(Attributes attributes) throws Exception {
		// Add the fragment repo to the repo list
		String repoId = attributes.getValue("id");
		String repoURIPath = attributes.getValue("uri");
		
		if (!repos.containsKey(repoId)) {
			repos.put(repoId, new Repo(repoId, baseURI, repoURIPath));
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

	protected void writeChapterDivOpenTag(String sectionName, String fragmentName, String repoName, boolean isRotated) throws IOException {
		writeDivOpenTag("chapter" + (isRotated ? " rotate" : ""), (getTocFileName() + "-" + sectionName + "-" + fragmentName).toLowerCase().replace(' ', '-'), fragmentName);
	}
	
	protected void writeMetaSectionDivOpenTag(String sectionName, boolean isRotated) throws IOException {
		writeDivOpenTag("meta-section" + (isRotated ? " rotate" : ""), (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}
	
	protected void writeStandardSectionDivOpenTag(String sectionName, boolean isRotated) throws IOException {
		writeDivOpenTag("section-header" + (isRotated ? " rotate" : ""), (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
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
	 * @param divTitle the value of the {@code title} attribute
	 * @throws IOException
	 */
	protected void writeDivOpenTag(String divClass, String divId, String divTitle) throws IOException {
		writeToOutputFile("<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">");
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
	protected String getFragmentAsHTML(Repo repo, String fragmentName, int chapterLevelOffset, String config) throws Exception {
		String extension = defaultExtension;

		int i = fragmentName.lastIndexOf('.');
		if (i > 0) {
		    extension = fragmentName.substring(i+1);
		} else {
			fragmentName += "." + extension;
		}
		
		InputStream fis = repo.getFragmentInputStream(fragmentName);
		String asHtml = getMarkupProcessor(extension).process(fis, config, this);
		fis.close();
		
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
		return chapterLevelOffset + currentSectionLevel - EFFECTIVE_LEVEL_ADJUSTMENT;
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

	public static String injectHeaderIdAttributes(String htmlFragment, String tocFileName, Repo repo, String sectionName, String fragmentName) {
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
					String headerId = (tocFileName + "-" + sectionName + "-" + fragmentName + "-" + headerText).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
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

	@Override
	public String getDefaultExtension() {
		return defaultExtension;
	}
}
