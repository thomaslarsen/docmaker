package net.toften.docmaker;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.maven.LogWrapper;
import net.toften.docmaker.output.OutputProcessor;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Docmaker {
    /**
     * The path to a TOC file, or a directory containing a number of TOC files.
     * <p>
     * If a path to a directory is given, all TOC files in this directory will be processed.
     *
     * @see #tocFileExt
     */
    @Parameter(names = "-toc", required = true)
    private String toc;

    /**
     * The extension of the TOC files.
     */
    @com.beust.jcommander.Parameter(names = "-tocFileExt")
    private String tocFileExt = "xml";

    /**
     * The base URI from where fragment repositories will be identified.
     * <p>
     * Note, that this will only be used if a relative repository URI is provided. If an absolute repository URI is
     * provided, this will be ignored.
     */
    @com.beust.jcommander.Parameter(names = "-fragmentURI")
    private String fragmentURI = "file://./";

    /**
     * The directory where the generated file and the transient HTML file will be located.
     */
    @com.beust.jcommander.Parameter(names = "-outputDir")
    private File outputDir = new File(".");

    /**
     * The class name of the {@link MarkupProcessor}
     */
    @com.beust.jcommander.Parameter(names = "-markupProcessorClassname")
    private String markupProcessorClassname = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor";

    /**
     * The class name of the {@link OutputProcessor}
     */
    @com.beust.jcommander.Parameter(names = "-outputProcessorClassname")
    private String outputProcessorClassname = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor";

    /**
     * The class name of the {@link AssemblyHandler}
     */
    @com.beust.jcommander.Parameter(names = "-assemblyHandlerClassname")
    private String assemblyHandlerClassname = "net.toften.docmaker.DefaultAssemblyHandler";

    /**
     * Specifies the encoding of the files.
     */
    @com.beust.jcommander.Parameter(names = "-encoding")
    private String encoding = "UTF-8";

    /**
     * Path to the CSS file to be used to style the generated output
     */
    @com.beust.jcommander.Parameter(names = "-cssFilePath")
    private String cssFilePath;

    public static void main(final String[] args) throws Exception {
        Docmaker mojo = new Docmaker();

        new JCommander(mojo, args);

        LogWrapper lw = new LogWrapper() {
            @Override
            public void info(final String message) {
                System.out.println("[INFO] " + message);
            }

            @Override
            public void warn(final String message) {
                System.out.println("[WARNING] " + message);
            }
        };

        Docmaker.run(lw, mojo.encoding, mojo.outputDir, mojo.fragmentURI, mojo.markupProcessorClassname,
                mojo.outputProcessorClassname, mojo.assemblyHandlerClassname, mojo.toc, mojo.tocFileExt,
                mojo.cssFilePath);
    }

    public static void run(final LogWrapper lw, final String encoding, final File outputDir, String fragmentURI,
            final String markupProcessorClassname, final String outputProcessorClassname,
            final String assemblyHandlerClassname, final String toc, final String tocFileExt, final String cssFilePath)
                    throws DocMakerException {
        // Check if encoding is supplied and/or valid
        String actualEncoding = checkEncoding(lw, encoding);
        // Create the path to the output dir if it doesn't exist
        lw.info("Writing output to: " + outputDir);
        outputDir.mkdirs();

        // Convert "\" in URI to "/" to support Windows paths
        fragmentURI = fragmentURI.replace('\\', '/');

        // Validate the base URI
        URI baseURI;
        try {
            baseURI = new URI(fragmentURI);
            lw.info("Base URI is: " + baseURI.toString() + ", created from " + fragmentURI);
        } catch (URISyntaxException e1) {
            throw new DocMakerException("Could not parse base URI", e1);
        }

        if (!baseURI.isAbsolute()) {
            throw new DocMakerException("Base URI is not absolute");
        }

        // Create the SAX parser
        SAXParser p;
        try {
            p = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new DocMakerException("Can not create SAX parser", e);
        }

        MarkupProcessor markupProcessor;
        try {
            markupProcessor = newInstance(MarkupProcessor.class, markupProcessorClassname);
            markupProcessor.setEncoding(actualEncoding);
            lw.info("Using " + markupProcessorClassname + " as the " + MarkupProcessor.class.getName());
        } catch (Exception e) {
            throw new DocMakerException("Can not create MarkupProcessor", e);
        }

        OutputProcessor postProcessor;
        try {
            postProcessor = newInstance(OutputProcessor.class, outputProcessorClassname);
            lw.info("Using " + outputProcessorClassname + " as the " + OutputProcessor.class.getName());
        } catch (Exception e) {
            throw new DocMakerException("Can not create OutputProcessor", e);
        }

        File tocFile = new File(toc);

        lw.info("Using " + assemblyHandlerClassname + " as " + AssemblyHandler.class.getName() + " for parsing TOCs");

        if (tocFile.isFile() && tocFile.getName().endsWith(tocFileExt)) {
            parseAndProcessFile(lw, tocFile, p, baseURI, markupProcessor, postProcessor, assemblyHandlerClassname,
                    outputDir, actualEncoding, cssFilePath);
        } else if (tocFile.isDirectory()) {
            for (File f : tocFile.listFiles()) {
                if (f.isFile() && f.getName().endsWith(tocFileExt)) {
                    parseAndProcessFile(lw, f, p, baseURI, markupProcessor, postProcessor, assemblyHandlerClassname,
                            outputDir, actualEncoding, cssFilePath);
                }
            }
        }
    }

    private static void parseAndProcessFile(final LogWrapper lw, final File tocFile, final SAXParser p,
            final URI baseURI, final MarkupProcessor markupProcessor, final OutputProcessor postProcessor,
            final String assemblyHandlerClassname, final File outputDir, final String encoding, final String cssFilePath)
            throws DocMakerException {
        String outputFilename = tocFile.getName().replaceFirst("[.][^.]+$", ""); // remove the extension

        lw.info("Parsing TOC: " + tocFile.getName());

        AssemblyHandler ah;
        String htmlFileName;
        try {
            ah = newInstance(AssemblyHandler.class, assemblyHandlerClassname);
            htmlFileName = outputDir + File.separator + outputFilename + "." + ah.getFileExtension();
            ah.setBaseURI(baseURI);
            ah.init(htmlFileName, encoding);
            ah.setMarkupProcessor(markupProcessor);
        } catch (Exception e) {
            throw new DocMakerException("Could not create TOC handler " + tocFile.getAbsolutePath(), e);
        }

        try {
            ah.insertCSSFile(cssFilePath);
            FileInputStream fis = new FileInputStream(tocFile);
            ah.parse(p, fis, tocFile.getName());
        } catch (Exception e) {
            throw new DocMakerException("Could not parse file " + tocFile.getAbsolutePath(), e);
        }

        try {
            postProcessor.process(new File(htmlFileName), outputDir, outputFilename);
        } catch (Exception e) {
            throw new DocMakerException("Could not post process file " + tocFile.getAbsolutePath(), e);
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
    public static <K> K newInstance(final Class<K> type, final String className) throws Exception {
        return ((Class<K>) Class.forName(className)).newInstance();
    }

    /**
     * Checks if an encoding is defined in the POM. If it is and valid, returns. Otherwise sets the encoding variable
     * to the platform encoding.
     */
    private static String checkEncoding(final LogWrapper lw, final String encoding) {
        if (encoding == null || encoding.length() < 1) {
            return usePlatformEncoding(lw);
        } else {
            try {
                Charset.forName(encoding);
                lw.info("Using '" + encoding + "' encoding to read doc files.");
                return encoding;
            } catch (UnsupportedCharsetException e) {
                lw.warn("Encoding defined in POM " + encoding + " is unsupported.");
                return usePlatformEncoding(lw);
            } catch (IllegalCharsetNameException e) {
                lw.warn("Encoding defined in POM " + encoding + " is not a legal charset name.");
                return usePlatformEncoding(lw);
            }
        }
    }

    /**
     * Retrieves the platform encoding, and logs a warning message to the user informing them of the encoding that
     * will be used. Uses the same method to get encoding as core Maven classes, i.e. getting the system property
     * "file.encoding". Will default to UTF-8 if no value is found.
     */
    private static String usePlatformEncoding(final LogWrapper lw) {
        String platformEncoding = System.getProperty("file.encoding", "UTF-8");
        lw.warn("Using platform encoding (" + platformEncoding
                + " actually) to read doc files, i.e. build is platform dependent!");
        return platformEncoding;
    }
}
