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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The {@link AssemblyHandler} will process the TOC file, and assemble the complete
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
public class AssemblyHandler extends DefaultHandler implements ProcessorHandlerCallback {
	private String htmlFilename;
	private FileWriter htmlFile;

	private int currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;

	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	private Map<String, String> metaData = new HashMap<String, String>();
	private Map<String, String> repos = new HashMap<String, String>();
	private URI baseURI;

	/**
	 * @param baseURI the URI from which all relative repo paths will be calculated
	 * @param htmlFilename the name of the assembled output file
	 */
	public AssemblyHandler(URI baseURI, String htmlFilename) {
		if (!baseURI.isAbsolute())
			throw new IllegalArgumentException("The base URI " + baseURI.toString() + " is not absolute");
		
		this.baseURI = baseURI;
		this.htmlFilename = htmlFilename;
	}

	public void insertCSSFile(String path) {
		// TODO ability to add multiple CSS files
		this.cssFilePath = path;
	}
	
	public int getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	public String getCurrentSectionName() {
		return currentSectionName;
	}

	public void setBaseURI(URI baseURI) {
		this.baseURI = baseURI;
	}
	
	public void setHtmlFilename(String htmlFilename) {
		this.htmlFilename = htmlFilename;
	}
	
	protected FileWriter getHtmlFile() {
		return htmlFile;
	}

	@Override
	public void startDocument() throws SAXException {
		try {
			htmlFile = new FileWriter(htmlFilename);
		} catch (IOException e) {
			throw new SAXException("Outfile could not be created", e);
		}

		try {
			htmlFile.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			htmlFile.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		} catch (IOException e) {
			throw new SAXException("Outfile could not be initialised", e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			htmlFile.write("</html>");
		} catch (IOException e) {
			throw new SAXException("Outfile could not be initialised", e);
		}

		try {
			htmlFile.flush();
		} catch (IOException e) {
			throw new SAXException("Outfile could not be flushed", e);
		} finally {
			try {
				htmlFile.close();
			} catch (IOException e) {
				throw new SAXException("Outfile could not be closed", e);
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		DocPart dp = DocPart.valueOfString(qName);

		if (dp != null) {
			try {
				if (dp.preElement() != null)
					htmlFile.write(dp.preElement());

				switch (dp) {
				case REPO:
					// Add the fragment repo to the repo list
					repos.put(attributes.getValue("id"), attributes.getValue("uri"));
					break;

				case HEADER:
					htmlFile.write("<title>" + attributes.getValue("title") + "</title>");
					
					// Also add the title to the meta data elements
					metaData.put("title", attributes.getValue("title"));
					if (cssFilePath != null) {
						htmlFile.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFilePath + "\" />");
					}
					break;

				case LINK:
				case META:
					addElementAndAttributes(htmlFile, qName, attributes, null);
					break;

				case PROPERTY:
					metaData.put(attributes.getValue("key"), attributes.getValue("value"));
					break;

				case SECTIONS:
					/*
					 * Just before the fragments, we include a metadata section
					 * This section will include all the metadata defined in the property section
					 */
					htmlFile.write("<div class=\"metadata\">");
					for (Map.Entry<String, String> m : metaData.entrySet()) {
						htmlFile.write("<div class=\"meta\" key=\"" + m.getKey() + "\">");
						htmlFile.write(m.getValue());
						htmlFile.write("</div>");
					}
					htmlFile.write("</div>");
					break;

				case SECTION:
					currentSectionName = attributes.getValue("title");
					/*
					 * If the section does not have a level adjustment it will be treated
					 * as a meta-section, which does not have any frament, but might
					 * have elements
					 */
					if (attributes.getValue("level") != null) {
						currentSectionLevel = Integer.valueOf(attributes.getValue("level"));

						htmlFile.write("<div class=\"section-header\" id=\"" + currentSectionName + "\">");
					} else {
						// When no level is specified, treat this as a metasection
						htmlFile.write("<div class=\"meta-section\" id=\"" + currentSectionName + "\">");
					}
					break;

				case CHAPTER:
					currentFragmentName = attributes.getValue("fragment");

					htmlFile.write("<div class=\"chapter\" id=\"" + currentSectionName + "-" + currentFragmentName + "\">");

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
						addFragment(repoURI, currentFragmentName, chapterLevelOffset);
					} else {
						throw new SAXException("Repo " + repo + " not declared");
					}
					break;

				case ELEMENT:
					/*
					 * An element is a div tag, that references a metadata key/value pair
					 */
					String key = attributes.getValue("key");
					if (metaData.containsKey(key)) {
						htmlFile.write("<div key=\"" + key + "\">");
						htmlFile.write(metaData.get(key));
						htmlFile.write("</div>");
					}
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
				switch (dp) {
				case CHAPTER:
				case SECTION:
					htmlFile.write("</div>");
					break;

				default:
					break;
				}

				if (dp.postElement() != null)
					htmlFile.write(dp.postElement());
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			}
		}	
	}

	private Map<String, String> addElementAndAttributes(FileWriter outFile, String qName, Attributes attributes, String elementClassName) throws IOException {
		Map<String, String> attr = new HashMap<String, String>();

		outFile.write("<" + qName);

		if (elementClassName != null) 
			outFile.write(" class=\"" + elementClassName + "\"");

		for (int i = 0; i < attributes.getLength(); i++) {
			outFile.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
			attr.put(attributes.getQName(i), attributes.getValue(i));
		}

		outFile.write("/>");

		return attr;
	}

	/**
	 * @param repoURI the URI of the repo where the fragment to add is located
	 * @param fragmentName the name of the fragment to add
	 * @param chapterLevelOffset the chapter level offset to apply to the fragment. This will be added to the {@link #getCurrentSectionLevel() current section level}
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected void addFragment(URI repoURI, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		File inFile = new File(repoURI.resolve(File.separator + fragmentName + ".html"));
		BufferedReader reader = new BufferedReader(new FileReader(inFile));

		String line;
		while( ( line = reader.readLine() ) != null ) {
			if (chapterLevelOffset > 0) {
				line = incrementHTag(line, chapterLevelOffset);
			}

			htmlFile.write(line);
		}

		reader.close();
	}

	protected final String getRepoURI(String id) {
		return repos.get(id);
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
