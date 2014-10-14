package net.toften.docmaker.markup.markdown.txtmark;

import java.io.File;
import java.io.IOException;

import net.toften.docmaker.markup.MarkupProcessor;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Decorator;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;

public class TxtMarkProcessor implements MarkupProcessor {
	private Builder b;
	private String encoding;

	public TxtMarkProcessor() {
		b = Configuration.builder();
	}

	public void setDecorator(Decorator decorator) {
		b.setDecorator(decorator);
	}

	public String process(File inFile, String config) throws IOException {
		this.b.setEncoding(this.encoding);
		return Processor.process(inFile, b.build());
	}

	public String getFileExtension() {
		return "md";
	}
	
	@Override
	public void setEncoding(final String encodingString) {
		this.encoding = encodingString;
	}	
}
