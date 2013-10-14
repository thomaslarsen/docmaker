package net.toften.docmaker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.SAXParser;

import net.toften.docmaker.markup.MarkupProcessor;

import org.xml.sax.SAXException;



public interface AssemblyHandler extends OutputFileHandler {

	MarkupProcessor getMarkupProcessor();
	
	void setMarkupProcessor(MarkupProcessor markupProcessor);

	void insertCSSFile(String path);

	void setBaseURI(URI baseURI);
	
	void parse(SAXParser parser, InputStream tocStream, String tocName) throws SAXException, IOException;
}