package net.toften.docmaker.maven;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import net.toften.docmaker.DocMaker;
import net.toften.docmaker.DocMakerException;
import net.toften.docmaker.LogWrapper;
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
     * The class name of the {@link OutputProcessor}
     */
    @Parameter(defaultValue = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor")
    private String outputProcessorClassname;

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
        LogWrapper lw = new LogWrapper() {

            @Override
            public void warn(final String message) {
                getLog().warn(message);
            }

            @Override
            public void info(final String message) {
                getLog().info(message);
            }

			@Override
			public void debug(String message) {
				getLog().debug(message);
			}
        };

        try {
            DocMaker dm = new DocMaker(lw, this.encoding, this.outputDir, this.fragmentURI, this.markupProcessors, this.markupProcessorClassname,
                    this.outputProcessorClassname, this.assemblyHandlerClassname, this.tocFileExt,
                    Arrays.asList(cssFilePaths), this.defaultExtension, Arrays.asList(filters));

    		
    		dm.run(this.toc);
        } catch (DocMakerException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
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
}
