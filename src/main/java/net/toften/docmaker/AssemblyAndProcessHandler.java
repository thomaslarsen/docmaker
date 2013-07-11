package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AssemblyAndProcessHandler extends AssemblyHandler {
	private MarkupProcessor mdProcessor;

	public AssemblyAndProcessHandler(URI baseURI, String resultFilename, MarkupProcessor mdProcessor) {
		super(baseURI, resultFilename);

		this.mdProcessor = mdProcessor;
	}

	@Override
	protected void addFile(FileWriter outFile, URI fileURI, String fragment, int chapterLevel) throws IOException, URISyntaxException {
		File inFile = new File(fileURI.resolve(fragment + "." + mdProcessor.getExtension()));

		if (!inFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + inFile.getAbsolutePath().toString());
		}

		String asHtml = mdProcessor.process(inFile);
		if (chapterLevel > 1) {
			asHtml = replaceHTag(asHtml, chapterLevel - getCurrentSectionLevel());
		}

		outFile.write(asHtml);
	}
}
