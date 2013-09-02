package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.toften.docmaker.markup.MarkupProcessor;

public class AssemblyAndProcessHandler extends AbstractAssemblyHandler {
	private MarkupProcessor markupProcessor;

	public void setMarkupProcessor(MarkupProcessor markupProcessor) {
		this.markupProcessor = markupProcessor;
	}

	@Override
	protected String getFragmentAsHTML(URI repoURI, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		if (!repoURI.isAbsolute())
			throw new IllegalArgumentException("The repo URI " + repoURI.toString() + " is not absolute");
		
		URI markupFilenameURI = new URI(fragmentName + "." + markupProcessor.getFileExtension());
		File markupFile = new File(repoURI.resolve(markupFilenameURI));

		if (!markupFile.exists()) {
			throw new FileNotFoundException("Could not find input file: " + markupFile.getAbsolutePath().toString());
		}

		String asHtml = markupProcessor.process(markupFile);
		if (chapterLevelOffset > 0) {
			asHtml = incrementHTag(asHtml, chapterLevelOffset);
		}

		return asHtml;
	}
}
