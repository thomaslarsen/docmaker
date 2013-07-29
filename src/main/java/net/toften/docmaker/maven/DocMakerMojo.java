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
	
	@Parameter ( defaultValue = "xml" )
	private String tocFileExt;
	
	@Parameter ( defaultValue = "file://${basedir}" )
	private String fragmentURI;
	
	@Parameter ( defaultValue = "${project.build.directory}/docmaker" )
	private String outputDir;
	
	@Parameter (defaultValue = "net.toften.docmaker.PegdownProcessor" )
	private String processorClassname;
	
	@Parameter ( defaultValue = "net.toften.docmaker.PDFPostProcessor" )
	private String postProcessorClassname;
	
	@Parameter
	private String cssFilePath;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Create the path to the output dir if it doesn't exist
		new File(outputDir).mkdirs();

		// Validate the base URI
		URI baseURI;
		try {
			baseURI = new URI(fragmentURI);
		} catch (URISyntaxException e1) {
			throw new MojoFailureException("Could not parse base URI", e1);
		}
		if (!baseURI.isAbsolute())
			throw new MojoFailureException("Base URI is not absolute");
		
		// Create the SAX parser
		SAXParser p;
		try {
			p = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create SAX parser", e);
		}
		
		MarkupProcessor markupProcessor = newInstance(MarkupProcessor.class, processorClassname);
		PostProcessor postProcessor = newInstance(PostProcessor.class, postProcessorClassname);
		
		File tocFile = new File(toc);
		
		if (tocFile.isFile() && tocFile.getName().endsWith(tocFileExt)) {
			parseAndProcessFile(tocFile, p, baseURI, markupProcessor, postProcessor);
		} else if (tocFile.isDirectory()) {
			for (File f : tocFile.listFiles()) {
				if (f.isFile() && f.getName().endsWith(tocFileExt))
					parseAndProcessFile(f, p, baseURI, markupProcessor, postProcessor);
			}
		}
	}

	private void parseAndProcessFile(File tocFile, SAXParser p, URI baseURI, MarkupProcessor markupProcessor, PostProcessor postProcessor) throws MojoExecutionException {
		String outputFilename = tocFile.getName().replaceFirst("[.][^.]+$", ""); // remove the extension
		String htmlFileName = outputDir + File.separator + outputFilename + ".html";
		String processedFilename = outputDir + "/" + outputFilename + "." + postProcessor.getFileExtension();
		
		// TODO parameter for handler classname
		AssemblyHandler ah = new AssemblyAndProcessHandler(baseURI, htmlFileName, markupProcessor);
		ah.insertCSSFile(cssFilePath);

		try {
			p.parse(tocFile, ah);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not parse file " + tocFile.getAbsolutePath(), e);
		}
		
		try {
			postProcessor.postProcess(new File(htmlFileName), processedFilename);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not post process file " + tocFile.getAbsolutePath(), e);
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
