package net.toften.docmaker.maven;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

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
	@Parameter
	private Map<String, String> markupProcessorClassname;
	
	/** 
	 * The default extension to use if files don't specify an extension
	 */
	@Parameter (defaultValue = "md")
	private String defaultExtension;
	
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
	 * Specifies the encoding of the files.
	 */
	@Parameter(defaultValue = "${project.build.sourceEncoding}")
	private String encoding;
	
	/**
	 * Path to the CSS file to be used to style the generated output
	 */
	@Parameter
	private String cssFilePath;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Check if encoding is supplied and/or valid
		checkEncoding();
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
		
		Map<String, MarkupProcessor> processors = new HashMap<String, MarkupProcessor>();
		for (String extension : markupProcessorClassname.keySet()) {
			MarkupProcessor markupProcessor;
			try {
				markupProcessor = newInstance(MarkupProcessor.class, markupProcessorClassname.get(extension));
				markupProcessor.setEncoding(encoding);
				
				processors.put(extension, markupProcessor);
			} catch (Exception e) {
				throw new MojoExecutionException("Can not create MarkupProcessor", e);
			}
			getLog().info("Using " + markupProcessorClassname + " as the " + MarkupProcessor.class.getName() + " for extension " + extension);
		}
		
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
			parseAndProcessFile(tocFile, p, baseURI, processors, postProcessor);
		} else if (tocFile.isDirectory()) {
			for (File f : tocFile.listFiles()) {
				if (f.isFile() && f.getName().endsWith(tocFileExt))
					parseAndProcessFile(f, p, baseURI, processors, postProcessor);
			}
		}
	}

	private void parseAndProcessFile(File tocFile, SAXParser p, URI baseURI, Map<String, MarkupProcessor> processors, OutputProcessor postProcessor) throws MojoExecutionException {
		getLog().info("Parsing TOC: " + tocFile.getName());
		
		AssemblyHandler ah;
		String htmlFileName;
		try {
			ah = newInstance(AssemblyHandler.class, assemblyHandlerClassname);
			htmlFileName = outputDir + File.separator + tocFile.getName().replaceFirst("[.][^.]+$", "") + "." + ah.getFileExtension();
			ah.setBaseURI(baseURI);
			ah.init(htmlFileName, encoding);
			ah.setMarkupProcessor(processors);
			ah.setDefaultFileExtension(defaultExtension);
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
			postProcessor.process(new File(htmlFileName), outputDir, ah.getDocumentTitle());
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
	
	/**
	 * Checks if an encoding is defined in the POM. If it is and valid, returns. Otherwise sets the encoding variable
	 * to the platform encoding. 
	 */
	private void checkEncoding() {
		if (this.encoding == null || encoding.length() < 1) {
			this.encoding = usePlatformEncoding();
			return;
		}
		else
		{
			try {
				Charset.forName(this.encoding);
				getLog().info( "Using '" + encoding + "' encoding to read doc files.");
				return;
			} catch (UnsupportedCharsetException e) {
				getLog().warn("Encoding defined in POM " + this.encoding + " is unsupported.");
				this.encoding = usePlatformEncoding();
				return;
			} catch (IllegalCharsetNameException e) {
				getLog().warn("Encoding defined in POM " + this.encoding + " is not a legal charset name.");
				this.encoding = usePlatformEncoding();
				return;
			}
		}
	}

	/**
	 * Retrieves the platform encoding, and logs a warning message to the user informing them of the encoding that
	 * will be used. Uses the same method to get encoding as core Maven classes, i.e. getting the system property 
	 * "file.encoding". Will default to UTF-8 if no value is found.   
	 */
	private String usePlatformEncoding() {
		String platformEncoding = System.getProperty("file.encoding", "UTF-8");
		getLog().warn( "Using platform encoding (" + platformEncoding
			+ " actually) to read doc files, i.e. build is platform dependent!" );
		return platformEncoding;
	}
}
