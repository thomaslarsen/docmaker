package net.toften.docmaker.markup.markdown.markdownj;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import net.toften.docmaker.markup.MarkupProcessor;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownJProcessor implements MarkupProcessor {

	public String process(File inFile) throws IOException {
		return new MarkdownProcessor().markdown(new Scanner(inFile).useDelimiter("\\A").next());
	}

	public String getExtension() {
		return "md";
	}
}
