package net.toften.docmaker.markup.markdown.pegdown;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import net.toften.docmaker.markup.MarkupProcessor;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

public class PegdownProcessor implements MarkupProcessor {

	public String process(File file) throws IOException {
		return new PegDownProcessor(Extensions.TABLES).markdownToHtml(new Scanner(file).useDelimiter("\\A").next());
	}

	public String getFileExtension() {
		return "md";
	}
}
