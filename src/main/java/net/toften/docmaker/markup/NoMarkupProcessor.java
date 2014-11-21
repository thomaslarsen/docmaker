package net.toften.docmaker.markup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.toften.docmaker.AssemblyHandler;

/**
 * {@link MarkupProcessor} that passes the fragment file through without any
 * processing.
 * Used when the fragments are already in the interim format.
 * 
 * @author tlarsen
 *
 */
public class NoMarkupProcessor implements MarkupProcessor {

	private String encoding;

	@Override
	public String process(File inFile, String config, AssemblyHandler handler) throws IOException {
		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(inFile),
			Charset.forName(this.encoding));
		BufferedReader reader = new BufferedReader(fileReader);

		StringBuffer asHTML = new StringBuffer();
		String line;
		while( ( line = reader.readLine() ) != null ) {
			asHTML.append(line);
		}

		reader.close();

		return asHTML.toString();
	}

	@Override
	public String getFileExtension() {
		return "html";
	}
	
	@Override
	public void setEncoding(final String encodingString) {
		this.encoding = encodingString;
	}

	@Override
	public String process(String inString, String config, AssemblyHandler handler) throws IOException {
		return inString;
	}
}
