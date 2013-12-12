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
	/**
	 * The path to a TOC file, or a directory containing a number of TOC files.
	 * <p>
	 * If a path to a directory is given, all TOC files in this directory will be processed.
	 * 
	 * @see #tocFileExt
	 */
	@Parameter (required = true )
	private String toc;
	
	/**
	 * The extension of the TOC files.
	 */
	@Parameter ( defaultValue = "xml" )
	private String tocFileExt;
	
	/**
	 * The base URI from where fragment repositories will be identified.
	 * <p>
	 * Note, that this will only be used if a relative repository URI is provided.
	 * If an absolute repository URI is provided, this will be ignored.
	 */
	@Parameter ( defaultValue = "file:///${basedir}/" )
	private String fragmentURI;
	
	/**
	 * The directory where the generated file and the transient HTML file will be located.
	 */
	@Parameter ( defaultValue = "${project.build.directory}/docmaker" )
	private File outputDir;
	
	/**
	 * The class name of the {@link MarkupProcessor}
	 */
	@Parameter (defaultValue = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor" )
	private String markupProcessorClassname;
	
	/**
	 * The class name of the {@link OutputProcessor}
	 */
	@Parameter ( defaultValue = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor" )
	private String outputProcessorClassname;
	
	/**
	 * The class name of the {@link AssemblyHandler}
	 */
	@Parameter ( defaultValue = "net.toften.docmaker.DefaultAssemblyHandler" )
	private String assemblyHandlerClassname;
	
	/**
	 * Path to the CSS file to be used to style the generated output
	 */
	@Parameter
	private String cssFilePath;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Create the path to the output dir if it doesn't exist
		getLog().info("Writing output to: " + outputDir);
		outputDir.mkdirs();

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
			postProcessor.process(new File(htmlFileName), outputDir, outputFilename);
		} catch (Exception e) {
			throw new MojoExecutionException("Could not post process file " + tocFile.getAbsolutePath(), e);
		}
	}
	
	/**
	 * Utility method to instantiate a class, given an interface.
	 * 
	 * @param type the interface type to use as return type
	 * @param className the name of the class (implementing the interface) to instantiate
	 * @return an instance of the class, returned as the interface type
	 * @throws Exception
	 */
	public static <K> K newInstance(Class<K> type, String className) throws Exception {
		return ((Class<K>) Class.forName(className)).newInstance();
	}
}
