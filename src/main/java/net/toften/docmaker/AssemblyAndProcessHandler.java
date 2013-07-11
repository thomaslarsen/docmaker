package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AssemblyAndProcessHandler extends AssemblyHandler {
	private MarkupProcessor mdProcessor;


	public AssemblyAndProcessHandler(String resultFilename, MarkupProcessor mdProcessor) {
		super(resultFilename);

		this.mdProcessor = mdProcessor;
	}

	private String convertFile(String repo, String fragment) throws IOException, URISyntaxException {
		URI inFileURI = new URI(getRepo(repo) + File.separator + fragment + "." + mdProcessor.getExtension());
		System.out.println("parsing " + inFileURI.toString());
		File inFile = new File(inFileURI);

		if (inFile.exists()) {
			return mdProcessor.process(inFile);
		} else {
			throw new FileNotFoundException("Could not find input file: " + inFile.getAbsolutePath().toString());
		}
	}

	@Override
	protected void addFile(FileWriter outFile, String repo, String fragment, int chapterLevel) throws IOException, URISyntaxException {
		String asHtml = convertFile(repo, fragment);

		if (chapterLevel > 1) {
			asHtml = replaceHTag(asHtml, chapterLevel - getCurrentSectionLevel());
		}

		outFile.write(asHtml);
	}
}
