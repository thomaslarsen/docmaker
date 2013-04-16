package net.toften.docmaker;

import java.io.File;
import java.io.IOException;

import org.markdown4j.Markdown4jProcessor;

public class MD4JProcessor implements MDProcessor {

	public String process(File inFile) throws IOException {
		return new Markdown4jProcessor().process(inFile);
	}
}
