package net.toften.docmaker.maven;

import java.io.File;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.DocMakerException;
import net.toften.docmaker.Docmaker;
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
     * The class name of the {@link MarkupProcessor}
     */
    @Parameter(defaultValue = "net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor")
    private String markupProcessorClassname;

    /**
     * The class name of the {@link OutputProcessor}
     */
    @Parameter(defaultValue = "net.toften.docmaker.output.pdf.flyingsaucer.FlyingSaucerOutputProcessor")
    private String outputProcessorClassname;

    /**
     * The class name of the {@link AssemblyHandler}
     */
    @Parameter(defaultValue = "net.toften.docmaker.DefaultAssemblyHandler")
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
        };

        try {
            Docmaker.run(lw, this.encoding, this.outputDir, this.fragmentURI, this.markupProcessorClassname,
                    this.outputProcessorClassname, this.assemblyHandlerClassname, this.toc, this.tocFileExt,
                    this.cssFilePath);
        } catch (DocMakerException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }
}
