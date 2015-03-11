package net.toften.docmaker.maven;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.toften.docmaker.DocMakerException;
import net.toften.docmaker.DocMakerMain;
import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.output.OutputProcessor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "docmaker")
public class DocMakerMojo extends AbstractMojo {
	/**
     * The path to a TOC file, or a directory containing a number of TOC files.
     * <p>
     * If a path to a directory is given, all TOC files in this directory will be processed.
     *
     * @see #tocFileExt
     */
    @Parameter(required = true)
    private String toc;

    /**
     * The extension of the TOC files.
     */
    @Parameter(defaultValue = "xml")
    private String tocFileExt;

    /**
     * The base URI from where fragment repositories will be identified.
     * <p>
     * Note, that this will only be used if a relative repository URI is provided. If an absolute repository URI is
     * provided, this will be ignored.
     */
    @Parameter(defaultValue = "file:///${basedir}/")
    private String fragmentURI;

    /**
     * The directory where the generated file and the transient HTML file will be located.
     */
    @Parameter(defaultValue = "${project.build.directory}/docmaker")
    private File outputDir;

    /**
     * List of {@link MarkupProcessor} class name mappings to file extensions.
     */
	@Parameter
	private Map<String, String> markupProcessors;
	
	/**
	 * The class name of the default {@link MarkupProcessor} if the markupProcessors is not specified
	 */
    @Parameter(defaultValue = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor")
    private String markupProcessorClassname;

	/** 
	 * The default extension to use if files don't specify an extension
	 */
	@Parameter (defaultValue = "md")
	private String defaultExtension;
	
    /**
     * List of {@link OutputProcessor} classnames
     */
    @Parameter
    private List<String> outputProcessors;
    
    /**
     * The class name of the {@link AssemblyHandler}
     */
    @Parameter(defaultValue = "net.toften.docmaker.handler.standard.StandardHandler")
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
    private String[] cssFilePaths;
    
    @Parameter(defaultValue = "${project.filters}")
    private String[] filters;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	// Add the Maven log instead of the console log
    	Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
          rootLogger.removeHandler(handlers[0]);
        }
        rootLogger.addHandler(new MavenLoggerHandler(getLog()));

        try {
            DocMakerMain dm = new DocMakerMain(Level.FINEST, this.encoding, this.outputDir, this.fragmentURI, this.markupProcessors, this.markupProcessorClassname,
                    this.outputProcessors, this.assemblyHandlerClassname, this.tocFileExt,
                    Arrays.asList(cssFilePaths), this.defaultExtension, Arrays.asList(filters));
    		
    		dm.run(this.toc);
        } catch (DocMakerException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }
}
