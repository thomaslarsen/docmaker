package net.toften.docmaker.markup.markdown.pegdown;

import java.io.IOException;

import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.markdown.MarkupProcessorAdapter;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

public class PegdownProcessor extends MarkupProcessorAdapter implements MarkupProcessor {
	public String getFileExtension() {
		return "md";
	}

	@Override
	public String process(String inString, String config, AssemblyHandler handler, LogWrapper lw) throws IOException {
		return new PegDownProcessor(Extensions.TABLES).markdownToHtml(inString);
	}
}
