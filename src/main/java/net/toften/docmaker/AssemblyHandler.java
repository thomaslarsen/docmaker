package net.toften.docmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.rjeschke.txtmark.Processor;

public class AssemblyHandler extends DefaultHandler {
	private String sectionDir;
	private String resultFilename;
	private FileWriter outFile;
	private int currentSectionLevel;
	private String inputDir;

	private static Pattern p = Pattern.compile("(\\</?h)(\\d)(>)");

	public AssemblyHandler(String sectionsDir, String resultFilename) {
		this.sectionDir = sectionsDir;
		this.resultFilename = resultFilename;
	}

	public AssemblyHandler(String inputDir, String sectionsDir, String resultFilename) {
		this(sectionsDir, resultFilename);

		this.inputDir = inputDir;
	}

	@Override
	public void startDocument() throws SAXException {
		try {
			outFile = new FileWriter(resultFilename);
		} catch (IOException e) {
			throw new SAXException("Outfile could not be created", e);
		}

		try {
			outFile.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
			outFile.write("<html>");
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
					if (inputDir == null)
						addFile(sectionDir + File.separator + "sections", attributes.getValue("group"), attributes.getValue("fragment"), chapterLevel);
					else
						addAndProcessFile(attributes.getValue("group"), attributes.getValue("fragment"), chapterLevel);
					break;

				case HEADER:
					outFile.write("<title>" + attributes.getValue("title") + "</title>");
					break;

				case LINK:
					outFile.write(
							"<link" +
									(attributes.getValue("rel") != null ? " rel=\"" + attributes.getValue("rel") + "\"" : "") +
									(attributes.getValue("type") != null ? " type=\"" + attributes.getValue("type") + "\"" : "") +
									(attributes.getValue("href") != null ? " href=\"" + attributes.getValue("href") + "\"" : "") +
									(attributes.getValue("media") != null ? " media=\"" + attributes.getValue("media") + "\"" : "") +
							"/>");
					break;

				case SECTION:
					currentSectionLevel = Integer.valueOf(attributes.getValue("level"));
					outFile.write(
							"<h" + currentSectionLevel + " class=\"section\">" +
									attributes.getValue("title") +
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
			} catch (IOException e) {
				throw new SAXException("Processing element " + qName + " failed", e);
			}
		}
	}

	private void addFile(String sectionDir, String group, String fragment, int chapterLevel) throws IOException {
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

	private void addAndProcessFile(String group, String fragment, int chapterLevel) throws IOException {
		String asHtml = convertFile(group, fragment);

		if (chapterLevel > 1) {
			asHtml = replaceHTag(asHtml, chapterLevel - currentSectionLevel);
		}

		outFile.write(asHtml);
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

	private String convertFile(String group, String fragment) throws IOException {
		String inFileName = inputDir + File.separator + "sections" + File.separator + group + File.separator + fragment + ".md";
		File inFile = new File(inFileName);

		if (inFile.exists()) {
			return Processor.process(inFile);
		} else {
			throw new FileNotFoundException("Could not find input file: " + inFileName);
		}
	}
}
