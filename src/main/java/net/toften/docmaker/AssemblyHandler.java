package net.toften.docmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	private String sectionDir;
	private String resultFilename;
	private FileWriter outFile;
	
	private int currentSectionLevel;
	private String currentSectionName;
	private String cssFilePath;
	
	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	public AssemblyHandler(String sectionsDir, String resultFilename) {
		this.sectionDir = sectionsDir;
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
				case CHAPTER:
					int chapterLevel = attributes.getValue("level") == null ? currentSectionLevel : Integer.valueOf(attributes.getValue("level"));
					
					String fragmentName = attributes.getValue("fragment");
					outFile.write("<div class=\"chapter\" id=\"" + currentSectionName + "-" + fragmentName + "\">");
					
					addFile(outFile, sectionDir + File.separator + "sections", attributes.getValue("group"), fragmentName, chapterLevel);
					break;

				case HEADER:
					outFile.write("<title>" + attributes.getValue("title") + "</title>");
					if (cssFilePath != null) {
						outFile.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFilePath + "\" />");
					}
					break;

				case LINK:
				case META:
					addElementAndAttributes(outFile, qName, attributes);
					break;

				case SECTION:
					currentSectionLevel = Integer.valueOf(attributes.getValue("level"));
					currentSectionName = attributes.getValue("title");
					outFile.write("<div class=\"section-header\">" + currentSectionName + "</div>");
					outFile.write(
							"<h" + currentSectionLevel + " class=\"section\">" +
									currentSectionName +
									"</h" + currentSectionLevel + ">");
				}
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
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
	
	private void addElementAndAttributes(FileWriter outFile, String qName, Attributes attributes) throws IOException {
		outFile.write("<" + qName);
		for (int i = 0; i < attributes.getLength(); i++) {
			outFile.write(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
		}
		outFile.write("/>");
	}

	protected void addFile(FileWriter outFile, String sectionDir, String group, String fragment, int chapterLevel) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(sectionDir + File.separator + group + File.separator + fragment + ".html"));

		String line;
		while( ( line = reader.readLine() ) != null ) {
			if (chapterLevel > 1) {
				line = replaceHTag(line, chapterLevel - currentSectionLevel);
			}

			outFile.write(line);
		}

		reader.close();
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
