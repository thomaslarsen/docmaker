package net.toften.docmaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class PDFPostProcessor implements PostProcessor {
	public void postProcess(File inFile, String outFileName) throws Exception {
		if (!inFile.exists())
			throw new IllegalArgumentException("Input file " + inFile.getName() + " can not be found");
	    OutputStream os = new FileOutputStream(outFileName);
		
	    ITextRenderer renderer = new ITextRenderer();
	    renderer.setDocument(inFile);
	    renderer.layout();
	    renderer.createPDF(os);

	    os.close();
	}

	public String getFileExtension() {
		return "pdf";
	}
}
