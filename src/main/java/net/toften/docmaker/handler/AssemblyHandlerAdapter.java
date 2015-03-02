package net.toften.docmaker.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.SectionType;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AssemblyHandlerAdapter extends DefaultHandler implements
		AssemblyHandler, TOC {

	public static String headerRegex = "(\\</?h)(\\d)(>)";

	public static final String POSTPROCESSOR_CLASSNAME = "classname";
	public static final String REPO_URI = "uri";
	public static final String REPO_ID = "id";
	public static final String ELEMENT_KEY = "key";
	public static final String SECTION_ROTATE = "rotate";
	public static final String SECTION_LEVEL = "level";
	public static final String SECTION_TITLE = "title";
	public static final String SECTION_CLASSNAME = "classname";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_KEY = "key";
	public static final String HEADER_TITLE = "title";
	public static final String PROPERTY_SRC = "src";
	public static final String CHAPTER_ROTATE = "rotate";
	public static final String CHAPTER_CONFIG = "config";
	public static final String CHAPTER_LEVEL = "level";
	public static final String CHAPTER_REPO = "repo";
	public static final String CHAPTER_FRAGMENT = "fragment";
	
	private Map<String, Map<String, String>> htmlMeta;
	private Properties metaData;
	private List<String> cssFiles;
	private Map<String, Repo> repos;
	private List<PostProcessor> postProcessors = new LinkedList<PostProcessor>();;
	private String tocFileNameWithoutExtension;
	private String documentTitle;
	private String currentSectionName;
	private Integer currentSectionLevel;
	private boolean currentSectionRotate;
	private URI baseURI;
	private Map<String, MarkupProcessor> markupProcessor;
	private String defaultExtension;

	public AssemblyHandlerAdapter() {
	}
	
	protected Map<String, Repo> getRepos() {
		return repos;
	}
	
	protected  List<PostProcessor> getPostProcessors() {
		return postProcessors;
	}
	
	@Override
	public Properties getMetaData() {
		return metaData;
	}

	@Override
	public Map<String, Map<String, String>> getHtmlMeta() {
		return htmlMeta;
	}
	
	@Override
	public URI getBaseURI() {
		return baseURI;
	}

	@Override
	public MarkupProcessor getMarkupProcessor(String extension) {
		return markupProcessor.get(extension);
	}

	@Override
	public List<String> getStyleSheets() {
		return cssFiles;
	}

	@Override
	public TOC parse(InputStream tocStream, String tocName, String defaultExtension, URI baseURI, Map<String, MarkupProcessor> markupProcessor, Properties baseProperties, List<String> cssFiles) 
			throws Exception {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");

		// Initialise handler
		htmlMeta = new HashMap<String, Map<String, String>>();
		metaData = new Properties();
		repos = new HashMap<String, Repo>();

		if (baseProperties != null)
			this.metaData.putAll(baseProperties);
		this.baseURI = baseURI;
		this.markupProcessor = markupProcessor;
		this.defaultExtension = defaultExtension;
		this.tocFileNameWithoutExtension = tocName.replaceFirst("[.][^.]+$", "");
		this.cssFiles = cssFiles;
		
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
	
	protected void handleHeaderSection(Attributes attributes) throws Exception  {
	}

	protected abstract void handleChapterElement(Attributes attributes) throws Exception;

	protected abstract void handlePseudoSection(Attributes attributes) throws Exception;

	protected abstract void handleMetaSectionElement(Attributes attributes) throws Exception;

	protected abstract void handleContentSectionElement(Attributes attributes) throws Exception;

	protected abstract void handleElementElement(Attributes attributes) throws Exception;

	protected void handlePostProcessor(Attributes attributes) throws Exception {
		PostProcessor pp = (PostProcessor) Class.forName(attributes.getValue(POSTPROCESSOR_CLASSNAME)).newInstance();
		pp.init(attributes);
		
		postProcessors.add(pp);
	}

	protected void handleRepoElement(Attributes attributes) throws Exception {
		// Add the fragment repo to the repo list
		String repoId = attributes.getValue(REPO_ID);
		String repoURIPath = attributes.getValue(REPO_URI);
		
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

	protected void handlePropertyElement(Attributes attributes) throws Exception {
		if (attributes.getValue(PROPERTY_SRC) != null) {
			/*
			 * A properties file has been specified
			 * Load the properties from the file and add them
			 * to the list of metadata
			 */
			String filePath = attributes.getValue(PROPERTY_SRC);
			URI propFileURI = new URI(filePath);
			if (!propFileURI.isAbsolute()) {
				propFileURI = getBaseURI().resolve(propFileURI);
			}
			
			// Load properties
			InputStream is = propFileURI.toURL().openStream();
			Properties fileProps = new Properties();
			fileProps.load(is);
			is.close();
			metaData.putAll(fileProps);
		} else {
			/*
			 * A single property has been specified
			 * Get the key/value pair and add to the metadata
			 */
			String key = attributes.getValue(PROPERTY_KEY);
			if (key == null)
				throw new SAXException("Property key not specified");
			
			if (attributes.getValue(PROPERTY_VALUE) == null)
				throw new SAXException("Value for property " + key + " not specified");
			
			metaData.put(key, attributes.getValue(PROPERTY_VALUE));
		}
	}

	protected void handleHeadElement(String qName, Attributes attributes) {
		Map<String, String> meta = new HashMap<String, String>();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			meta.put(attributes.getQName(i), attributes.getValue(i));
		}
		
		htmlMeta.put(qName, meta);
	}

	protected void handleHeaderElement(Attributes attributes) {
		documentTitle = attributes.getValue(HEADER_TITLE);
		metaData.put(HEADER_TITLE, getDocumentTitle());
	}
	
	protected void runPostProcessors(boolean apply) {
		for (Section s : getSections()) {
			if (s.getSectionType() == SectionType.CONTENTS_SECTION) {
				for (Chapter c : ((ChapterSection)s).getChapters()) {
					c.runPostProcessors(getPostProcessors(), this, apply);
				}
			}
		}
	}
}
