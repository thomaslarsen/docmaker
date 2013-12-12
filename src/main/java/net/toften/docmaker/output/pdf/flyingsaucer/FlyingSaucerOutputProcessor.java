package net.toften.docmaker.output.pdf.flyingsaucer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.toften.docmaker.output.OutputProcessor;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class FlyingSaucerOutputProcessor implements OutputProcessor {
	public void process(File inFile, File outputDir, String outputName) throws Exception {
		if (!inFile.exists())
			throw new IllegalArgumentException("Input file " + inFile.getName() + " can not be found");
		
		File outputFile;
		
		if (outputDir.isDirectory()) {
			if (outputName == null)
				throw new NullPointerException("Output filename is null");
			
			outputFile = new File(outputDir, outputName + "." + getFileExtension());
		} else
			outputFile = outputDir;
		
	    OutputStream os = new FileOutputStream(outputFile);
		
	    ITextRenderer renderer = new ITextRenderer();
	    renderer.setDocument(inFile);
	    renderer.layout();
	    renderer.createPDF(os);

	    os.close();
	}

	private String getFileExtension() {
		return "pdf";
	}
}
