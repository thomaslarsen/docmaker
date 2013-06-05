package net.toften.docmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class AssemblyAndProcessHandler extends AssemblyHandler {
	private String inputDir;
	private MarkupProcessor mdProcessor;


	public AssemblyAndProcessHandler(String inputDir, String sectionsDir, String resultFilename, MarkupProcessor mdProcessor) {
		super(sectionsDir, resultFilename);

		this.inputDir = inputDir;
		this.mdProcessor = mdProcessor;
	}

	private String convertFile(String group, String fragment) throws IOException {
		String inFileName = inputDir + File.separator + "sections" + File.separator + group + File.separator + fragment + ".md";
		File inFile = new File(inFileName);

		if (inFile.exists()) {
			return mdProcessor.process(inFile);
		} else {
			throw new FileNotFoundException("Could not find input file: " + inFileName);
		}
	}

	@Override
	protected void addFile(FileWriter outFile, String sectionDir, String group, String fragment, int chapterLevel) throws IOException {
		String asHtml = convertFile(group, fragment);

		if (chapterLevel > 1) {
			asHtml = replaceHTag(asHtml, chapterLevel - getCurrentSectionLevel());
		}

		outFile.write(asHtml);
	}
}
