package net.toften.docmaker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.toften.docmaker.markup.MarkupProcessor;

public class SplitTOCHandler extends AssemblyAndProcessHandler {

	public SplitTOCHandler(URI baseURI, String htmlFilename,
			MarkupProcessor markupProcessor) throws IOException {
		super(baseURI, htmlFilename, markupProcessor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void writeToOutputFile(String text) throws IOException {
		// Throw away all these calls
	}

	@Override
	protected String getFragmentAsHTML(URI repoURI, String fragmentName,
			int chapterLevelOffset) throws IOException, URISyntaxException {
		// TODO Auto-generated method stub
		return super.getFragmentAsHTML(repoURI, fragmentName, chapterLevelOffset);
	}
}
