package net.toften.docmaker.output.pdf.flyingsaucer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.toften.docmaker.output.OutputProcessor;
import net.toften.docmaker.toc.TOC;

import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.PdfWriter;

public class FlyingSaucerOutputProcessor extends SingleInterimFile implements OutputProcessor {
	public void process(File outputDir, String outputName, String encoding, TOC t) throws Exception {
		if (outputName == null)
			throw new NullPointerException("Output filename is null");
		
		File inFile = buildInterimFile(outputDir, outputName, encoding, t);
		
		File outputFile;
		
		if (outputDir.isDirectory()) {
			outputFile = new File(outputDir, outputName + ".pdf");
		} else
			outputFile = outputDir;
		
	    OutputStream os = new FileOutputStream(outputFile);
		
	    ITextRenderer renderer = new ITextRenderer();
	    renderer.setDocument(inFile);
	    renderer.setPDFVersion(PdfWriter.VERSION_1_7);
	    renderer.layout();
	    renderer.createPDF(os);
	    os.close();
	}
}
