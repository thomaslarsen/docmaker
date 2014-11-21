package net.toften.docmaker.markup.markdown.markdownj;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownJProcessor implements MarkupProcessor {

	private String encoding;

	public String process(File inFile, String config, AssemblyHandler handler) throws IOException {
		return new MarkdownProcessor().markdown(new Scanner(inFile, this.encoding).useDelimiter("\\A").next());
	}

	public String getFileExtension() {
		return "md";
	}
	
	@Override
	public void setEncoding(final String encodingString) {
		this.encoding = encodingString;
	}

	@Override
	public String process(String inString, String config, AssemblyHandler handler) throws IOException {
		return new MarkdownProcessor().markdown(inString);
	}
}
