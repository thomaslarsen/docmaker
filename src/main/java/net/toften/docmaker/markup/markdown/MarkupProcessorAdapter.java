package net.toften.docmaker.markup.markdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;

public abstract class MarkupProcessorAdapter implements MarkupProcessor {

	private String encoding;

	@Override
	public String process(File inFile, String config, AssemblyHandler handler)
			throws IOException {
		String markup = new Scanner(inFile, getEncoding()).useDelimiter("\\A").next();

		return process(markup, config, handler);
	}

	protected String getEncoding() {
		return encoding;
	}

	@Override
	public String process(InputStream is, String config, AssemblyHandler handler)
			throws IOException {
		String markup = new Scanner(is, getEncoding()).useDelimiter("\\A").next();

		return process(markup, config, handler);
	}

	@Override
	public void setEncoding(String encodingString) {
		this.encoding = encodingString;
	}
}
