package net.toften.docmaker.maven;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.output.OutputProcessor;

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
	
	@Parameter ( defaultValue = "file:///${basedir}/" )
	private String fragmentURI;
	
	@Parameter ( defaultValue = "${project.build.directory}/docmaker" )
	private String outputDir;
	
	@Parameter (defaultValue = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor" )
	private String markupProcessorClassname;
	
	@Parameter ( defaultValue = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor" )
	private String outputProcessorClassname;
	
	@Parameter ( defaultValue = "net.toften.docmaker.AbstractAssemblyHandler" )
	private String assemblyHandlerClassname;
	
	@Parameter
	private String cssFilePath;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Create the path to the output dir if it doesn't exist
		getLog().info("Writing output to: " + outputDir);
		new File(outputDir).mkdirs();

		// Convert "\" in URI to "/" to support Windows paths
		fragmentURI = fragmentURI.replace('\\', '/');

		// Validate the base URI
		URI baseURI;
		try {
			baseURI = new URI(fragmentURI);
		} catch (URISyntaxException e1) {
			throw new MojoFailureException("Could not parse base URI", e1);
		}
		
		getLog().info("Base URI is: " + baseURI.toString() + ", created from " + fragmentURI);
		
		if (!baseURI.isAbsolute())
			throw new MojoFailureException("Base URI is not absolute");
		
		// Create the SAX parser
		SAXParser p;
		try {
			p = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create SAX parser", e);
		}
		
		MarkupProcessor markupProcessor;
		try {
			markupProcessor = newInstance(MarkupProcessor.class, markupProcessorClassname);
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create MarkupProcessor", e);
		}
		getLog().info("Using " + markupProcessorClassname + " as the " + MarkupProcessor.class.getName());
		
		OutputProcessor postProcessor;
		try {
			postProcessor = newInstance(OutputProcessor.class, outputProcessorClassname);
		} catch (Exception e) {
			throw new MojoExecutionException("Can not create OutputProcessor", e);
		}
		getLog().info("Using " + outputProcessorClassname + " as the " + OutputProcessor.class.getName());
		
		File tocFile = new File(toc);
		
		getLog().info("Using " + assemblyHandlerClassname + " as " + AssemblyHandler.class.getName() + " for parsing TOCs");
		
		if (tocFile.isFile() && tocFile.getName().endsWith(tocFileExt)) {
			parseAndProcessFile(tocFile, p, baseURI, markupProcessor, postProcessor);
		} else if (tocFile.isDirectory()) {
			for (File f : tocFile.listFiles()) {
				if (f.isFile() && f.getName().endsWith(tocFileExt))
					parseAndProcessFile(f, p, baseURI, markupProcessor, postProcessor);
			}
		}
	}

	private void parseAndProcessFile(File tocFile, SAXParser p, URI baseURI, MarkupProcessor markupProcessor, OutputProcessor postProcessor) throws MojoExecutionException {
		String outputFilename = tocFile.getName().replaceFirst("[.][^.]+$", ""); // remove the extension
		String processedFilename = outputDir + "/" + outputFilename + "." + postProcessor.getFileExtension();
		
		getLog().info("Parsing TOC: " + tocFile.getName());
		
		AssemblyHandler ah;
		String htmlFileName;
		try {
			ah = newInstance(AssemblyHandler.class, assemblyHandlerClassname);
			htmlFileName = outputDir + File.separator + outputFilename + "." + ah.getFileExtension();
			ah.setBaseURI(baseURI);
			ah.init(htmlFileName);
			ah.setMarkupProcessor(markupProcessor);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not create TOC handler " + tocFile.getAbsolutePath(), e);
		}
		
		try {
			ah.insertCSSFile(cssFilePath);
			FileInputStream fis = new FileInputStream(tocFile);
			ah.parse(p, fis, tocFile.getName());
		} catch (Exception e) {
			throw new MojoExecutionException("Could not parse file " + tocFile.getAbsolutePath(), e);
		}
		
		try {
			postProcessor.process(new File(htmlFileName), processedFilename);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not post process file " + tocFile.getAbsolutePath(), e);
		}
	}
	
	public static <K> K newInstance(Class<K> type, String className) throws Exception {
		Class<K> clazz = (Class<K>) Class.forName(className);

		return clazz.newInstance();
	}
}
