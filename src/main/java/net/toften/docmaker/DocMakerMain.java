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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.output.BrokenLinks;
import net.toften.docmaker.output.OutputProcessor;
import net.toften.docmaker.toc.TOC;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DocMakerMain {
	private static Logger lw = Logger.getLogger(DocMakerMain.class.getName());	

	static void setLogLevel(Level level){
		Logger log = LogManager.getLogManager().getLogger("");
		log.setLevel(level);
		for (Handler h : log.getHandlers()) {
		    h.setLevel(level);
		}
	}

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
	@Parameter(names = { "-markupProcessor", "-mp" }, description = "List of MarkupProcessor class name mappings to file extensions")
	private List<String> markupProcessors;
	
	/**
	 * The class name of the default {@link MarkupProcessor} if the markupProcessors is not specified.
	 */
    @Parameter(names = "-markupProcessorClassname", description = "The class name of the default MarkupProcessor if mappings are not specified.")
    private String markupProcessorClassname = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor";

	/** 
	 * The default extension to use if fragments in the TOC don't specify an extension.
	 */
	@Parameter (names = "-defaultExtension", description = "The default extension to use if fragments in the TOC don't specify an extension.")
	private String defaultExtension = "md";
	
    /**
     * List of {@link OutputProcessor} class names.
     */
    @Parameter(names = { "-outputProcessor", "-op" }, description = "List of OutputProcessor class names.")
    private List<String> outputProcessors = Arrays.asList(new String[]{"net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor"});

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
    @Parameter (names = { "-keys", "-k" }, description = "Specifies a list of property files for key/value replacement.")
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
    
    @Parameter(names = "-loglevel", description = "The log level to use")
    private String logLevel = Level.INFO.getName();
    
	private Map<String, MarkupProcessor> processors = new HashMap<String, MarkupProcessor>();
	private URI baseURI = new File(".").toURI();
	
    private Map<String, String> markupProcessorsMap;
	private Properties props;
	private String actualEncoding;

    public static void main(final String[] args) throws Exception {
        DocMakerMain mojo = new DocMakerMain();

        JCommander jc = new JCommander(mojo, args);
        
        jc.setProgramName("DocMaker");
        
        if (mojo.help) {
        	jc.usage();
        } else {
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
	        
	        setLogLevel(Level.parse(mojo.logLevel));
	        
	        mojo.initDocMaker();
	        mojo.run(mojo.toc);
        }
    }
    
    private DocMakerMain() {
    	// To use with the jCommander parser
    }
    
    public DocMakerMain(Level logLevel, String encoding, File outputDir,
			String fragmentURI, Map<String, String> markupProcessors,
			String markupProcessorClassname, List<String> outputProcessors,
			String assemblyHandlerClassname, String tocFileExt,
			List<String> cssFilePaths, String defaultExtension,
			List<String> filters) throws DocMakerException {
		this.encoding = encoding;
		this.outputDir = outputDir;
		this.fragmentURI = fragmentURI;
		this.markupProcessorClassname = markupProcessorClassname;
		this.outputProcessors = outputProcessors;
		this.assemblyHandlerClassname = assemblyHandlerClassname;
		this.tocFileExt = tocFileExt;
		this.cssFilePath = cssFilePaths;
		this.defaultExtension = defaultExtension;
		this.markupProcessorsMap = markupProcessors;
		this.propFilenames = filters;

		setLogLevel(logLevel);
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
		if (markupProcessorsMap == null) {
			MarkupProcessor markupProcessor;
			try {
				markupProcessor = newInstance(MarkupProcessor.class, markupProcessorClassname);
				markupProcessor.setEncoding(actualEncoding);
				
				processors.put(defaultExtension, markupProcessor);
			} catch (Exception e) {
				throw new DocMakerException("Can not create MarkupProcessor", e);
			}
			lw.info("Using default " + markupProcessorClassname + " as the " + MarkupProcessor.class.getName() + " for extension " + defaultExtension);
		} else {
			for (String extension : markupProcessorsMap.keySet()) {
				MarkupProcessor markupProcessor;
				try {
					markupProcessor = newInstance(MarkupProcessor.class, markupProcessorsMap.get(extension));
					markupProcessor.setEncoding(actualEncoding);
					
					processors.put(extension, markupProcessor);
				} catch (Exception e) {
					throw new DocMakerException("Can not create MarkupProcessor", e);
				}
				lw.info("Using " + markupProcessorsMap.get(extension) + " as the " + MarkupProcessor.class.getName() + " for extension " + extension);
			}
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
        
        // Add default output processors
        outputProcessors = new LinkedList<String>(outputProcessors);
        outputProcessors.add(0, BrokenLinks.class.getName());
    }
	
	/**
	 * @param tocFilename the path of the TOC file, or directory containing TOC files
	 * @throws DocMakerException
	 */
	public void run(String tocFilename) throws DocMakerException {
        File tocFile = new File(tocFilename);

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

    private void parseAndProcessFile(final File tocFile) throws DocMakerException {
        String outputFilename = tocFile.getName().replaceFirst("[.][^.]+$", ""); // remove the extension

        // Instantiate the AssemblyHandler
        AssemblyHandler ah;
        try {
            ah = newInstance(AssemblyHandler.class, assemblyHandlerClassname);
        } catch (Exception e) {
            throw new DocMakerException("Could not create TOC handler " + tocFile.getAbsolutePath(), e);
        }
        
        lw.fine("Properties pre TOC parsing: " + props.toString());
        
        // Parse the TOC
        TOC t;
        try {
            FileInputStream fis = new FileInputStream(tocFile);

            lw.info("Parsing TOC: " + tocFile.getName());
            t = ah.parse(fis, tocFile.getName(), defaultExtension, baseURI, processors, props, cssFilePath);
        } catch (Exception e) {
            throw new DocMakerException("Could not parse file " + tocFile.getAbsolutePath(), e);
        }
        
        lw.fine("Properties post TOC parsing: " + t.getMetaData().toString());
        
        // Process the output		
        for (String op : outputProcessors) {
        	// Instantiate the outputprocessor
        	OutputProcessor outputProcessor;
	        try {
	            outputProcessor = newInstance(OutputProcessor.class, op);
	            lw.info("Using " + op + " as the " + OutputProcessor.class.getName());
	        } catch (Exception e) {
	            throw new DocMakerException("Can not create OutputProcessor", e);
	        }
	
	        try {
	            outputProcessor.process(outputDir, outputFilename, actualEncoding, t);
	        } catch (Exception e) {
	            throw new DocMakerException("Could not post process file " + tocFile.getAbsolutePath(), e);
	        }
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
                lw.warning("Encoding defined in POM " + encoding + " is unsupported.");
                return usePlatformEncoding();
            } catch (IllegalCharsetNameException e) {
                lw.warning("Encoding defined in POM " + encoding + " is not a legal charset name.");
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
        lw.warning("Using platform encoding (" + platformEncoding
                + " actually) to read doc files, i.e. build is platform dependent!");
        return platformEncoding;
    }

    /**
     * Utility method to instantiate a class, given an interface.
     *
     * @param type the interface type to use as return type
     * @param className the name of the class (implementing the interface) to instantiate
     * @return an instance of the class, returned as the interface type
     * @throws Exception
     */
    private static <K> K newInstance(final Class<K> type, final String className) throws Exception {
    	return ((Class<K>) Class.forName(className)).newInstance();
    }
}
