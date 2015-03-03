package net.toften.docmaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.maven.DocMakerMojo;
import net.toften.docmaker.output.OutputProcessor;
import net.toften.docmaker.toc.TOC;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DocMaker {
    /**
     * The path to a TOC file, or a directory containing a number of TOC files.
     * <p>
     * If a path to a directory is given, all TOC files in this directory will be processed.
     *
     * @see #tocFileExt
     */
    @Parameter(names = { "-toc", "-t" }, required = true, description = "The path to a TOC file, or a directory containing a number of TOC files.")
    private String toc;

    /**
     * The extension of the TOC files.
     */
    @Parameter(names = "-tocFileExt", description = "The extension of the TOC files.")
    private String tocFileExt = "xml";

    /**
     * The base URI from where fragment repositories will be identified.
     * <p>
     * Note, that this will only be used if a relative repository URI is provided. If an absolute repository URI is
     * provided, this will be ignored.
     */
    @Parameter(names = "-fragmentURI", description = "The base URI from where fragment repositories will be identified.")
    private String fragmentURI;

    /**
     * The directory where the generated file and the transient HTML file will be located.
     */
    @Parameter(names = { "-outputDir", "-o" }, description = "The directory where the generated file and the transient HTML file will be located.")
    private File outputDir = new File(".");

	/**
	 * List of {@link MarkupProcessor} class name mappings to file extensions.
	 * 
	 * Syntax is "<file extension>:<markup processor classname>"
	 */
	@Parameter(names = "-markupProcessors", description = "List of MarkupProcessor class name mappings to file extensions")
	private List<String> markupProcessors;
	
	/**
	 * The class name of the default {@link MarkupProcessor} if the markupProcessors is not specified.
	 */
    @Parameter(names = "-markupProcessorClassname", description = "The class name of the default MarkupProcessor if mappings are not specified is not specified.")
    private String markupProcessorClassname = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor";

	/** 
	 * The default extension to use if fragments in the TOC don't specify an extension.
	 */
	@Parameter (names = "-defaultExtension", description = "The default extension to use if fragments in the TOC don't specify an extension.")
	private String defaultExtension = "md";
	
    /**
     * The class name of the {@link OutputProcessor}
     */
    @Parameter(names = "-outputProcessorClassname", description = "The class name of the OutputProcessor")
    private String outputProcessorClassname = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor";

    /**
     * The class name of the {@link AssemblyHandler}
     */
    @Parameter(names = "-assemblyHandlerClassname", description = "The class name of the {@link AssemblyHandler}")
    private String assemblyHandlerClassname = "net.toften.docmaker.handler.standard.StandardHandler";

    /**
     * Specifies the encoding of the files.
     */
    @Parameter(names = "-encoding", description = "Specifies the encoding of the files.")
    private String encoding;
    
    /**
     * Specifies a list of property files for key/value replacement.
     */
    @Parameter (names = "-keys", description = "Specifies a list of property files for key/value replacement.")
    private List<String> propFilenames;

    /**
     * List of CSS file to be used to style the generated output.
     */
    @Parameter(names = { "-cssFilePath", "-css" }, required = true, description = "List of CSS file to be used to style the generated output.")
    private List<String> cssFilePath;

    /**
     * Provides help
     */
    @Parameter(names =  { "--help", "-h" }, help = true, description = "Provides help")
    private boolean help;
    
	private LogWrapper lw;

	private OutputProcessor outputProcessor;
	private Map<String, MarkupProcessor> processors = new HashMap<String, MarkupProcessor>();
	private URI baseURI = new File(".").toURI();

    private Map<String, String> markupProcessorsMap;

	private Properties props;

	private String actualEncoding;


    public static void main(final String[] args) throws Exception {
        DocMaker mojo = new DocMaker();

        JCommander jc = new JCommander(mojo, args);
        
        jc.setProgramName("DocMaker");
        
        if (mojo.help) {
        	jc.usage();
        } else {
	        mojo.lw = new LogWrapper() {
	            @Override
	            public void info(final String message) {
	                System.out.println("[INFO] " + message);
	            }
	
	            @Override
	            public void warn(final String message) {
	                System.out.println("[WARNING] " + message);
	            }
	
				@Override
				public void debug(String message) {
	                System.out.println("[DEBUG] " + message);
				}
	        };
	        
	        /*
	         * Parse the jCommander version of the markupprocessor list
	         * This is necessary, as you can't pass Maps into jCommander
	         */
	        if (mojo.markupProcessors != null) {
	        	mojo.markupProcessorsMap = new HashMap<String, String>();
		        for (String processorMapping : mojo.markupProcessors) {
					String[] pm = processorMapping.split(":");
					mojo.markupProcessorsMap.put(pm[0], pm[1]);
				}
	        }
	        
	        mojo.initDocMaker();
	        mojo.run(mojo.toc);
        }
    }
    
    private DocMaker() {
    	// To use with the jCommander parser
    }
    
    public DocMaker(LogWrapper lw, String encoding2, File outputDir2,
			String fragmentURI2, Map<String, String> markupProcessors,
			String markupProcessorClassname2, String outputProcessorClassname2,
			String assemblyHandlerClassname2, String tocFileExt2,
			List<String> cssFilePath2, String defaultExtension2) throws DocMakerException {
		this.lw = lw;
		this.encoding = encoding2;
		this.outputDir = outputDir2;
		this.fragmentURI = fragmentURI2;
		this.markupProcessorClassname = markupProcessorClassname2;
		this.outputProcessorClassname = outputProcessorClassname2;
		this.assemblyHandlerClassname = assemblyHandlerClassname2;
		this.tocFileExt = tocFileExt2;
		this.cssFilePath = cssFilePath2;
		this.defaultExtension = defaultExtension2;
		this.markupProcessorsMap = markupProcessors;
		
		initDocMaker();
	}

	private void initDocMaker() throws DocMakerException {
        // Check if encoding is supplied and/or valid
        actualEncoding = checkEncoding();
        // Create the path to the output dir if it doesn't exist
        lw.info("Writing output to: " + outputDir);
        outputDir.mkdirs();

        if (fragmentURI != null) {
	        // Convert "\" in URI to "/" to support Windows paths
	        fragmentURI = fragmentURI.replace('\\', '/');
	
	        // Validate the base URI
	        try {
	            baseURI = new URI(fragmentURI);
	            lw.info("Base URI is: " + baseURI.toString() + ", created from " + fragmentURI);
	        } catch (URISyntaxException e1) {
	            throw new DocMakerException("Could not parse base URI", e1);
	        }
	
	        if (!baseURI.isAbsolute()) {
	            throw new DocMakerException("Base URI is not absolute");
	        }
        }

        // Instantiate the markup processors
		if (markupProcessors == null) {
			MarkupProcessor markupProcessor;
			try {
				markupProcessor = DocMakerMojo.newInstance(MarkupProcessor.class, markupProcessorClassname);
				markupProcessor.setEncoding(actualEncoding);
				
				processors.put(defaultExtension, markupProcessor);
			} catch (Exception e) {
				throw new DocMakerException("Can not create MarkupProcessor", e);
			}
			lw.info("Using default" + markupProcessorClassname + " as the " + MarkupProcessor.class.getName() + " for extension " + defaultExtension);
		} else {
			for (String extension : markupProcessorsMap.keySet()) {
				MarkupProcessor markupProcessor;
				try {
					markupProcessor = DocMakerMojo.newInstance(MarkupProcessor.class, markupProcessorsMap.get(extension));
					markupProcessor.setEncoding(actualEncoding);
					
					processors.put(extension, markupProcessor);
				} catch (Exception e) {
					throw new DocMakerException("Can not create MarkupProcessor", e);
				}
				lw.info("Using " + markupProcessorsMap.get(extension) + " as the " + MarkupProcessor.class.getName() + " for extension " + extension);
			}
		}

		// Instantiate the outputprocessor
        try {
            outputProcessor = DocMakerMojo.newInstance(OutputProcessor.class, outputProcessorClassname);
            lw.info("Using " + outputProcessorClassname + " as the " + OutputProcessor.class.getName());
        } catch (Exception e) {
            throw new DocMakerException("Can not create OutputProcessor", e);
        }

        lw.info("Using " + assemblyHandlerClassname + " as " + AssemblyHandler.class.getName() + " for parsing TOCs");
        
        // Load the keys
        props = new Properties();
        if (propFilenames != null) {
        	try {
        		for (String keyFilename : propFilenames) {
        			lw.info("Loading propertyfile: " + keyFilename);
        			InputStream in = new FileInputStream(keyFilename);
        			props.load(in);
        			in.close();
        		}
        	} catch (IOException e) {
        		throw new DocMakerException("Can not load key file", e);
        	}
        }
    }
	
	public void run(String toc) throws DocMakerException {
        File tocFile = new File(toc);

        if (tocFile.isFile() && tocFile.getName().endsWith(tocFileExt)) {
            parseAndProcessFile(tocFile);
        } else if (tocFile.isDirectory()) {
            for (File f : tocFile.listFiles()) {
                if (f.isFile() && f.getName().endsWith(tocFileExt)) {
                    parseAndProcessFile(f);
                }
            }
        }
    }

    private void parseAndProcessFile(final File tocFile)
            throws DocMakerException {
        String outputFilename = tocFile.getName().replaceFirst("[.][^.]+$", ""); // remove the extension

        // Instantiate the AssemblyHandler
        AssemblyHandler ah;
        try {
            ah = DocMakerMojo.newInstance(AssemblyHandler.class, assemblyHandlerClassname);
        } catch (Exception e) {
            throw new DocMakerException("Could not create TOC handler " + tocFile.getAbsolutePath(), e);
        }
lw.info("Props: " + props.toString());
        // Parse the TOC
        TOC t;
        try {
            FileInputStream fis = new FileInputStream(tocFile);

            lw.info("Parsing TOC: " + tocFile.getName());
            t = ah.parse(fis, tocFile.getName(), defaultExtension, baseURI, processors, props, cssFilePath);
        } catch (Exception e) {
            throw new DocMakerException("Could not parse file " + tocFile.getAbsolutePath(), e);
        }
        lw.info("TOC metadata: " + t.getMetaData().toString());
        // Process the output
        try {
            outputProcessor.process(outputDir, outputFilename, actualEncoding, t);
        } catch (Exception e) {
            throw new DocMakerException("Could not post process file " + tocFile.getAbsolutePath(), e);
        }
    }

    /**
     * Checks if an encoding is defined in the POM. If it is and valid, returns. Otherwise sets the encoding variable
     * to the platform encoding.
     */
    private String checkEncoding() {
        if (encoding == null || encoding.length() < 1) {
            return usePlatformEncoding();
        } else {
            try {
                Charset.forName(encoding);
                lw.info("Using '" + encoding + "' encoding to read doc files.");
                return encoding;
            } catch (UnsupportedCharsetException e) {
                lw.warn("Encoding defined in POM " + encoding + " is unsupported.");
                return usePlatformEncoding();
            } catch (IllegalCharsetNameException e) {
                lw.warn("Encoding defined in POM " + encoding + " is not a legal charset name.");
                return usePlatformEncoding();
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
        lw.warn("Using platform encoding (" + platformEncoding
                + " actually) to read doc files, i.e. build is platform dependent!");
        return platformEncoding;
    }
}
