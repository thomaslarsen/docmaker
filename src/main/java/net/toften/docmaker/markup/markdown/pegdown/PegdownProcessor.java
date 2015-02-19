package net.toften.docmaker.markup.markdown.pegdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

public class PegdownProcessor implements MarkupProcessor {

	private String encoding;

	public String process(File file, String config, AssemblyHandler handler) throws IOException {
		return new PegDownProcessor(Extensions.TABLES).markdownToHtml(new Scanner(file, this.encoding).useDelimiter("\\A").next());
	}

	public String process(InputStream is, String config, AssemblyHandler handler) throws IOException {
		return new PegDownProcessor(Extensions.TABLES).markdownToHtml(new Scanner(is, this.encoding).useDelimiter("\\A").next());
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
		return new PegDownProcessor(Extensions.TABLES).markdownToHtml(inString);
	}
}
