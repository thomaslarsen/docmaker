package net.toften.docmaker.markup.markdown.markdownj;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownJProcessor implements MarkupProcessor {

	private String encoding;

	public String process(File inFile, String config, AssemblyHandler handler) throws IOException {
		return process(new FileInputStream(inFile), config, handler);
	}
	
	@Override
	public String process(InputStream is, String config, AssemblyHandler handler)
			throws IOException {
		return new MarkdownProcessor().markdown(new Scanner(is, this.encoding).useDelimiter("\\A").next());
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
