package net.toften.docmaker.markup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * {@link MarkupProcessor} that passes the fragment file through without any
 * processing.
 * Used when the fragments are already in the interim format.
 * 
 * @author tlarsen
 *
 */
public class NoMarkupProcessor implements MarkupProcessor {

	@Override
	public String process(File inFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inFile));

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
}
