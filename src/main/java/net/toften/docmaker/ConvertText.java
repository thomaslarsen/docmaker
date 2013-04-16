package net.toften.docmaker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.github.rjeschke.txtmark.Processor;

public class ConvertText {
	public static void main(String[] args) throws Exception {
		String inputDir = "/Users/thomaslarsen/workspace/docmaker/src/test/resources/sample";
		String outputDir = "/Users/thomaslarsen/workspace/docmaker/target/out";

		File bd = new File(inputDir);
		System.out.println(bd.isDirectory());
		System.out.println(bd.getName());

		//convert(bd, outputDir);

		SAXParser p = SAXParserFactory.newInstance().newSAXParser();
		TxtMarkProcessor mdProcessor = new TxtMarkProcessor();
		MD4JProcessor md4jProcessor = new MD4JProcessor();

		String outFileName = outputDir + "/out.html";
		AssemblyAndProcessHandler ah = new AssemblyAndProcessHandler(inputDir, outputDir + "/sample", outFileName, md4jProcessor);
		mdProcessor.setDecorator(new HDecorator(ah));
		
		p.parse(new File("/Users/thomaslarsen/workspace/docmaker/src/test/resources/doc.xml"), ah);
		
		new PDFPostProcessor().postProcess(new File(outFileName), outputDir + "/out.pdf");
	}
	
	private void convert(File bd, String outputDir) throws IOException {
		File od = new File(outputDir);
		if (!od.exists()) {
			od.mkdirs();
		}

		String outFilename = bd.getName().replaceFirst("[.][^.]+$", "") + ".html";

		if (bd.isDirectory()) {
			String subPath = outputDir + File.separator + bd.getName();
			for (File f : bd.listFiles()) {
				convert(f, subPath);
			}
		} else {
			try {
				FileWriter fw = new FileWriter(outputDir + File.separator + outFilename);

				try {
					fw.write(Processor.process(bd));

					fw.flush();
				} finally {
					fw.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
	}
}