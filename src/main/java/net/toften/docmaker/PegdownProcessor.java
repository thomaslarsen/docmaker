package net.toften.docmaker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.pegdown.PegDownProcessor;

public class PegdownProcessor implements MarkupProcessor {

	public String process(File file) throws IOException {
		return new PegDownProcessor().markdownToHtml(new Scanner(file).useDelimiter("\\A").next());
	}
}
