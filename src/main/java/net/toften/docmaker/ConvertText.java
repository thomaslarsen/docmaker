package net.toften.docmaker;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class ConvertText {
	public static void main(String[] args) throws Exception {
//		String inputDir = "/Users/thomaslarsen/workspace/docmaker/src/test/resources/sample";
		String outputDir = "/Users/thomaslarsen/workspace/docmaker/target/out";

//		File bd = new File(inputDir);
//		System.out.println(bd.isDirectory());
//		System.out.println(bd.getName());

		//convert(bd, outputDir);

		SAXParser p = SAXParserFactory.newInstance().newSAXParser();
//		TxtMarkProcessor mdProcessor = new TxtMarkProcessor();
//		MD4JProcessor md4jProcessor = new MD4JProcessor();
//		MarkdownJProcessor mdProcessor = new MarkdownJProcessor();
		PegdownProcessor mdProcessor = new PegdownProcessor();

		String outFileName = outputDir + "/out.html";
		AssemblyAndProcessHandler ah = new AssemblyAndProcessHandler(outFileName, mdProcessor);
//		mdProcessor.setDecorator(new HDecorator(ah));
		
		p.parse(new File("/Users/thomaslarsen/workspace/docmaker/src/test/resources/doc.xml"), ah);
		
		new PDFPostProcessor().postProcess(new File(outFileName), outputDir + "/out.pdf");
	}
}