package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AssemblyAndProcessHandler extends AssemblyHandler {
	private MarkupProcessor markupProcessor;

	/**
	 * @param baseURI the URI from which all relative repo paths will be calculated
	 * @param htmlFilename the name of the assembled output file
	 * @param markupProcessor the markup processor to use to process each element file
	 */
	public AssemblyAndProcessHandler(URI baseURI, String htmlFilename, MarkupProcessor markupProcessor) {
		super(baseURI, htmlFilename);

		this.markupProcessor = markupProcessor;
	}
	
	public void setMarkupProcessor(MarkupProcessor markupProcessor) {
		this.markupProcessor = markupProcessor;
	}

	@Override
	protected void addFile(URI repoURI, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		String markupFilename = fragmentName + "." + markupProcessor.getExtension();
		File markupFile = new File(repoURI.resolve(markupFilename));

		if (!markupFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + markupFile.getAbsolutePath().toString());
		}

		String asHtml = markupProcessor.process(markupFile);
		if (chapterLevelOffset > 1) {
			asHtml = replaceHTag(asHtml, chapterLevelOffset - getCurrentSectionLevel());
		}

		getHtmlFile().write(asHtml);
	}
}
