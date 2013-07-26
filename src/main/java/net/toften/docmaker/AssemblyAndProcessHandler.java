package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AssemblyAndProcessHandler extends AssemblyHandler {
	private MarkupProcessor markupProcessor;

	/**
	 * @param baseURI the URI from which all relative repo paths will be calculated
	 * @param htmlFilename
	 * @param markupProcessor
	 */
	public AssemblyAndProcessHandler(URI baseURI, String htmlFilename, MarkupProcessor markupProcessor) {
		super(baseURI, htmlFilename);

		this.markupProcessor = markupProcessor;
	}

	@Override
	protected void addFile(FileWriter outFile, URI repoURI, String fragment, int chapterLevel) throws IOException, URISyntaxException {
		String markupFilename = fragment + "." + markupProcessor.getExtension();
		File markupFile = new File(repoURI.resolve(markupFilename));

		if (!markupFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + markupFile.getAbsolutePath().toString());
		}

		String asHtml = markupProcessor.process(markupFile);
		if (chapterLevel > 1) {
			asHtml = replaceHTag(asHtml, chapterLevel - getCurrentSectionLevel());
		}

		outFile.write(asHtml);
	}
}
