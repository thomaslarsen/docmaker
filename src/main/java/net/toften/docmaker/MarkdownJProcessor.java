package net.toften.docmaker;

import java.io.File;
import java.io.IOException;

public class MarkdownJProcessor implements MarkupProcessor {

	public String process(File inFile) throws IOException {
		return new MarkdownJProcessor().process(inFile);
	}
}
