package net.toften.docmaker.maven;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.AssemblyAndProcessHandler;
import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.MarkupProcessor;
import net.toften.docmaker.PostProcessor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo ( name = "docmaker" )
public class DocMakerMojo extends AbstractMojo {
	@Parameter ( defaultValue = "${basedir}/src/test/resources/doc.xml" )
	private String toc;
	
	@Parameter ( defaultValue = "/Users/thomaslarsen/workspace/docmaker/src/test/resources/sample" )
	private String inputDir;
	
	@Parameter ( defaultValue = "${project.build.directory}/out" )
	private String outputDir;
	
	@Parameter ( defaultValue = "out" )
	private String outputFilename;
	
	@Parameter (defaultValue = "net.toften.docmaker.PegdownProcessor" )
	private String processorClassname;
	
	@Parameter ( defaultValue = "net.toften.docmaker.PDFPostProcessor" )
	private String postProcessorClassname;
	
	@Parameter
	private String cssFilePath;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		SAXParser p;
		try {
			p = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create SAX parser", e);
		}
		
		MarkupProcessor markupProcessor = newInstance(MarkupProcessor.class, processorClassname);

		String outFileName = outputDir + "/" + outputFilename + ".html";
		URI baseURI = null;
		try {
			baseURI = new URI("file://" + inputDir);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// TODO parameter for handler classname
		AssemblyHandler ah = new AssemblyAndProcessHandler(baseURI, outFileName, markupProcessor);
		ah.insertCSSFile(cssFilePath);

		new File(outputDir).mkdirs();

		try {
			p.parse(new File(toc), ah);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not parse file", e);
		}
		
		PostProcessor pp = newInstance(PostProcessor.class, postProcessorClassname);
		try {
			pp.postProcess(new File(outFileName), outputDir + "/" + outputFilename + "." + pp.getFileExtension());
		} catch (Exception e) {
			throw new MojoExecutionException("Could not post process file", e);
		}
	}
	
	private <K> K newInstance(Class<K> type, String className) throws MojoExecutionException {
		K i;
		try {
			Class<K> clazz = (Class<K>) Class.forName(className);
			
			i = clazz.newInstance();
		} catch (Exception e) {
			throw new MojoExecutionException("Could not instantiate class " + className, e);
		}
		
		return i;
	}
}
