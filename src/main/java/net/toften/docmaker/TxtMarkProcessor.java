package net.toften.docmaker;

import java.io.File;
import java.io.IOException;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Decorator;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;

public class TxtMarkProcessor implements MDProcessor {
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
}
