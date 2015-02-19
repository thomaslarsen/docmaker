package net.toften.docmaker.handler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.maven.DocMakerMojo;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AssemblyHandlerAdapter extends DefaultHandler implements
		AssemblyHandler, TOC {

	public static String headerRegex = "(\\</?h)(\\d)(>)";

	public static final String ELEMENT_KEY = "key";
	public static final String SECTION_ROTATE = "rotate";
	public static final String SECTION_LEVEL = "level";
	public static final String SECTION_TITLE = "title";
	public static final String SECTION_CLASSNAME = "classname";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_KEY = "key";
	public static final String HEADER_TITLE = "title";
	
	protected Map<String, Map<String, String>> htmlMeta = new HashMap<String, Map<String, String>>();
	protected Map<String, String> metaData = new HashMap<String, String>();
	protected Map<String, Repo> repos = new HashMap<String, Repo>();
	protected List<PostProcessor> postProcessors = new LinkedList<PostProcessor>();
	
	private String tocFileNameWithoutExtension;
	private String documentTitle;

	private String currentSectionName;

	private Integer currentSectionLevel;

	private boolean currentSectionRotate;

	private URI baseURI;

	private Map<String, MarkupProcessor> markupProcessor;

	private String defaultExtension;

	private InterimFileHandler fileHandlerDelegate = new InterimFileHandler() {
		private OutputStreamWriter htmlFile;
		
		@Override
		public void init(final String filename, final String encodingString) throws IOException {
			this.htmlFile = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName(encodingString).newEncoder());
		}

		@Override
		public void close() throws IOException {
			try {
				htmlFile.flush();
			} finally {
				htmlFile.close();
			}
		}

		@Override
		public void writeToOutputFile(String text) throws IOException {
			if (text != null)
				htmlFile.write(text);
		}

		@Override
		public String getFileExtension() {
			return "html";
		}
	};

	public AssemblyHandlerAdapter() {
		// Empty
	}
	
	public AssemblyHandlerAdapter(InterimFileHandler fileHandlerDelegate) {
		setInterrimFileHandler(fileHandlerDelegate);
	}
	
	public void setInterrimFileHandler(InterimFileHandler fileHandlerDelegate) {
		this.fileHandlerDelegate = fileHandlerDelegate;
	}
	
	@Override
	public Map<String, String> getMetaData() {
		return metaData;
	}

	@Override
	public Map<String, Map<String, String>> getHtmlMeta() {
		return htmlMeta;
	}

	@Override
	public MarkupProcessor getMarkupProcessor(String extension) {
		return markupProcessor.get(extension);
	}

	@Override
	public void insertCSSFile(String path) {
		Map<String, String> cssMap = new HashMap<String, String>();
		cssMap.put("rel", "stylesheet");
		cssMap.put("type", "text/css");
		cssMap.put("href", path);
		htmlMeta.put("link", cssMap);
	}

	@Override
	public TOC parse(InputStream tocStream, String tocName, String defaultExtension, URI baseURI, Map<String, MarkupProcessor> markupProcessor) 
			throws Exception {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		this.baseURI = baseURI;
		this.markupProcessor = markupProcessor;
		this.defaultExtension = defaultExtension;
		this.tocFileNameWithoutExtension = tocName.replaceFirst("[.][^.]+$", "");
		
        // Create the SAX parser
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        p.parse(tocStream, this);
        
        return this;
	}

	@Override
	public String getDefaultExtension() {
		return defaultExtension;
	}
	
	@Override
	public String getCurrentSectionName() {
		return currentSectionName;
	}

	@Override
	public Integer getCurrentSectionLevel() {
		return currentSectionLevel;
	}
	
	@Override
	public boolean isCurrentSectionRotated() {
		return currentSectionRotate;
	}

	@Override
	public String getDocumentTitle() {
		return documentTitle;
	}

	@Override
	public String getTocFileName() {
		return tocFileNameWithoutExtension;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		DocPart dp = DocPart.valueOfString(qName);

		if (dp != null) {
			try {
				switch (dp) {
				case REPO:
					handleRepoElement(attributes);
					break;

				case HEADER:
					handleHeaderElement(attributes);
					break;

				case LINK:
				case META:
				case BASE:
					handleHeadElement(qName, attributes);
					break;

				case PROPERTY:
					handlePropertyElement(attributes);
					break;

				case ELEMENT:
					handleElementElement(attributes);
					break;
					
				case SECTION:
					initSection(dp, attributes);
					handleContentSectionElement(attributes);
					break;
					
				case METASECTION:
					initSection(dp, attributes);
					handleMetaSectionElement(attributes);
					break;
					
				case PSECTION:
					initSection(dp, attributes);
					handlePseudoSection(attributes);
					break;
					
				case HSECTION:
					initSection(dp, attributes);
					handleHeaderSection(attributes);
					break;

				case CHAPTER:
					handleChapterElement(attributes);
					break;
					
				case POSTPROC:
					handlePostProcessor(attributes);
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
	
	protected void handleUnknownElement(DocPart dp, Attributes attributes) {
	}
	
	protected void handleChapterElement(Attributes attributes) throws Exception  {
	}

	protected void handleHeaderSection(Attributes attributes) throws Exception  {
	}

	protected void handlePseudoSection(Attributes attributes) throws Exception  {
	}

	protected void handleMetaSectionElement(Attributes attributes) throws Exception  {
	}

	protected void handleContentSectionElement(Attributes attributes) throws Exception  {
	}

	protected void handleElementElement(Attributes attributes) throws Exception  {
	}

	protected void handlePostProcessor(Attributes attributes) throws Exception {
		postProcessors.add(DocMakerMojo.newInstance(PostProcessor.class, attributes.getValue("classname")));
	}

	protected void handleRepoElement(Attributes attributes) throws Exception {
		// Add the fragment repo to the repo list
		String repoId = attributes.getValue("id");
		String repoURIPath = attributes.getValue("uri");
		
		if (!repos.containsKey(repoId)) {
			repos.put(repoId, new Repo(repoId, baseURI, repoURIPath));
		}
	}

	protected void initSection(DocPart dp, Attributes attributes) throws Exception {
		if (attributes.getValue(SECTION_TITLE) == null)
			throw new SAXException("Section title attribute not specified");
		
		currentSectionName = attributes.getValue(SECTION_TITLE);

		if (attributes.getValue(SECTION_LEVEL) != null)
			currentSectionLevel = Integer.valueOf(attributes.getValue(SECTION_LEVEL));
		else
			currentSectionLevel = null;
		currentSectionRotate = attributes.getValue(SECTION_ROTATE) != null;
	}

	protected void handlePropertyElement(Attributes attributes) {
		metaData.put(attributes.getValue(PROPERTY_KEY), attributes.getValue(PROPERTY_VALUE));
	}

	protected void handleHeadElement(String qName, Attributes attributes) {
		Map<String, String> meta = new HashMap<String, String>();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			meta.put(attributes.getQName(i), attributes.getValue(i));
		}
		
		htmlMeta.put(qName, meta);
	}

	protected void handleHeaderElement(Attributes attributes) {
		metaData.put(HEADER_TITLE, attributes.getValue(HEADER_TITLE));
	}
}
