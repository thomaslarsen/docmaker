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

	public TxtMarkProcessor() {
		b = Configuration.builder();
	}

	public void setDecorator(Decorator decorator) {
		b.setDecorator(decorator);
	}

	public String process(File inFile) throws IOException {
		return Processor.process(inFile, b.build());
	}

	public String getExtension() {
		return "md";
	}
}
