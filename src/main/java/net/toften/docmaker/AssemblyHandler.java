package net.toften.docmaker;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

import net.toften.docmaker.markup.MarkupProcessor;

public interface AssemblyHandler extends OutputFileHandler {

	void setMarkupProcessor(MarkupProcessor markupProcessor);

	void insertCSSFile(String path);

	void setBaseURI(URI baseURI);
	
	void parse(SAXParser parser, File tocFile) throws SAXException, IOException;
}