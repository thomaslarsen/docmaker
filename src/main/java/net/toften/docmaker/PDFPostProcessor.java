package net.toften.docmaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class PDFPostProcessor implements PostProcessor {
	public void postProcess(File inFile, String outFileName) throws Exception {
		if (!inFile.exists())
			throw new IllegalArgumentException("Input file " + inFile.getName() + " can not be found");
		
	    ITextRenderer renderer = new ITextRenderer();

	    // parse the markup into an xml Document
//	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//	    Document doc = builder.parse(new StringBufferInputStream(null));
//	    renderer.setDocument(doc, null);

	    renderer.setDocument(inFile);

	    OutputStream os = new FileOutputStream(outFileName);
	    renderer.layout();
	    renderer.createPDF(os);
	    os.close();
	}

	public String getFileExtension() {
		return "pdf";
	}
}
