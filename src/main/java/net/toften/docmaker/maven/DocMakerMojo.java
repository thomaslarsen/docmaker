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
	@Parameter (required = true )
	private String toc;
	
	@Parameter ( defaultValue = "file://${basedir}" )
	private String fragmentURI;
	
	@Parameter ( defaultValue = "${project.build.directory}/docmaker" )
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
		// Create the path to the output dir if it doesn't exist
		new File(outputDir).mkdirs();

		SAXParser p;
		try {
			p = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create SAX parser", e);
		}
		
		// Validate the base URI
		URI baseURI;
		try {
			baseURI = new URI(fragmentURI);
		} catch (URISyntaxException e1) {
			throw new MojoFailureException("Could not parse base URI", e1);
		}
		if (!baseURI.isAbsolute())
			throw new MojoFailureException("Base URI is not absolute");
		
		MarkupProcessor markupProcessor = newInstance(MarkupProcessor.class, processorClassname);

		String htmlFileName = outputDir + File.separator + outputFilename + ".html";
		
		// TODO parameter for handler classname
		AssemblyHandler ah = new AssemblyAndProcessHandler(baseURI, htmlFileName, markupProcessor);
		ah.insertCSSFile(cssFilePath);

		try {
			p.parse(new File(toc), ah);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not parse file", e);
		}
		
		PostProcessor pp = newInstance(PostProcessor.class, postProcessorClassname);
		try {
			pp.postProcess(new File(htmlFileName), outputDir + "/" + outputFilename + "." + pp.getFileExtension());
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
