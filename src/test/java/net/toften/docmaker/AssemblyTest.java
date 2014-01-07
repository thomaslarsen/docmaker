package net.toften.docmaker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.SAXException;

public class AssemblyTest {
	@Test
	public void testHandler() throws ParserConfigurationException, SAXException, IOException {
		String toc = 
				"<document>" +
						"<repos>" +
						"<repo" +
						" id=\"test\"" +
						" uri=\"file:///src/resources\"" +
						"/>" +
						"</repos>" +
						"</document>";

		AssemblyHandler ah = new DefaultAssemblyHandler() {
			{
				this.setCurrentFileHandler(new DefaultAssemblyHandler.GenericFileHandler() {

					@Override
					public void writeToOutputFile(String text) throws IOException {
						System.out.println(text);
					}

					@Override
					public void init(String filename, String encodingString) throws IOException {
						System.out.println("Output file: " + filename + ", Encoding: " + encodingString);
					}

					@Override
					public void close() throws IOException {
						// TODO Auto-generated method stub

					}
				});
			}
		};

		SAXParser p = SAXParserFactory.newInstance().newSAXParser();

		InputStream tocFile = new ByteArrayInputStream(toc.getBytes("UTF-8"));
		ah.parse(p, tocFile, "toc1");
	}
}
