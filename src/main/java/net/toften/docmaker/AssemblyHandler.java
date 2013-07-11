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
	private String resultFilename;
	private FileWriter outFile;

	private int currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	private String currentFragmentName;

	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	private Map<String, String> metaData = new HashMap<String, String>();
	private Map<String, String> repos = new HashMap<String, String>();
	private URI baseURI;

	public AssemblyHandler(URI baseURI, String resultFilename) {
		this.baseURI = baseURI;
		this.resultFilename = resultFilename;
	}

	public void insertCSSFile(String path) {
		this.cssFilePath = path;
	}

	@Override
	public void startDocument() throws SAXException {
		try {
			outFile = new FileWriter(resultFilename);
		} catch (IOException e) {
			throw new SAXException("Outfile could not be created", e);
		}

		try {
			outFile.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			outFile.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		} catch (IOException e) {
			throw new SAXException("Outfile could not be initialised", e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			outFile.write("</html>");
		} catch (IOException e) {
			throw new SAXException("Outfile could not be initialised", e);
		}

		try {
			outFile.flush();
		} catch (IOException e) {
			throw new SAXException("Outfile could not be flushed", e);
		} finally {
			try {
				outFile.close();
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
					outFile.write(dp.preElement());

				switch (dp) {
				case REPO:
					// Add the fragment repo to the repo list
					repos.put(attributes.getValue("id"), attributes.getValue("uri"));
					break;

				case HEADER:
					outFile.write("<title>" + attributes.getValue("title") + "</title>");
					metaData.put("title", attributes.getValue("title"));
					if (cssFilePath != null) {
						outFile.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFilePath + "\" />");
					}
					break;

				case LINK:
				case META:
					addElementAndAttributes(outFile, qName, attributes, null);
					break;

				case PROPERTIY:
					metaData.put(attributes.getValue("key"), attributes.getValue("value"));
					break;

				case SECTIONS:
					/*
					 * Just before the fragments, we include a metadata section
					 * This section will include all the metadata defined in the property section
					 */
					outFile.write("<div class=\"metadata\">");
					for (Map.Entry<String, String> m : metaData.entrySet()) {
						outFile.write("<div class=\"meta\" key=\"" + m.getKey() + "\">");
						outFile.write(m.getValue());
						outFile.write("</div>");
					}
					outFile.write("</div>");
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

						outFile.write("<div class=\"section-header\" id=\"" + currentSectionName + "\">");
					} else {
						// When no level is specified, treat this as a metasection
						outFile.write("<div class=\"meta-section\" id=\"" + currentSectionName + "\">");
					}
					break;

				case CHAPTER:
					currentFragmentName = attributes.getValue("fragment");

					outFile.write("<div class=\"chapter\" id=\"" + currentSectionName + "-" + currentFragmentName + "\">");

					String repo = attributes.getValue("repo");
					if (repos.containsKey(repo)) {
						URI fileURI = baseURI.resolve(repos.get(repo));

						int chapterLevel = attributes.getValue("level") == null ? currentSectionLevel : Integer.valueOf(attributes.getValue("level"));
						addFile(outFile, fileURI, currentFragmentName, chapterLevel);
					} else {
						throw new SAXException("Repo " + repo + " npt declared");
					}
					break;

				case ELEMENT:
					/*
					 * An element is a div tag, that references a metadata key/value pair
					 */
					String key = attributes.getValue("key");
					if (metaData.containsKey(key)) {
						outFile.write("<div key=\"" + key + "\">");
						outFile.write(metaData.get(key));
						outFile.write("</div>");
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
				if (dp.postElement() != null)
					outFile.write(dp.postElement());

				switch (dp) {
				case CHAPTER:
				case SECTION:
					outFile.write("</div>");
					break;

				default:
					break;
				}
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

	protected void addFile(FileWriter outFile, URI fileURI, String fragment, int chapterLevel) throws IOException, URISyntaxException {
		File inFile = new File(fileURI.resolve(File.separator + fragment + ".html"));
		BufferedReader reader = new BufferedReader(new FileReader(inFile));

		String line;
		while( ( line = reader.readLine() ) != null ) {
			if (chapterLevel > 1) {
				line = replaceHTag(line, chapterLevel - currentSectionLevel);
			}

			outFile.write(line);
		}

		reader.close();
	}

	protected final String getRepo(String id) {
		return repos.get(id);
	}

	public static String replaceHTag(String line, int increment) {
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

	public int getCurrentSectionLevel() {
		return currentSectionLevel;
	}

	public String getCurrentSectionName() {
		return currentSectionName;
	}
}
