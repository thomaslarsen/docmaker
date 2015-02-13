package net.toften.docmaker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor;
import net.toften.docmaker.postprocessors.PostProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends DefaultAssemblyHandler {

	public SplitTOCHandler() {
		postProcessors.add(new HeaderIncrementPostProcessor());
		postProcessors.add(new InjectHeaderIdPostProcessor());
	}
	
	@Override
	public void close() throws IOException {
		// Trap this, so we don't prematurely close the file
		if (writeToOutput)
			super.close();
	}
	
	@Override
	public void writeToOutputFile(String text) throws IOException {
		if (writeToOutput && text != null)
			super.writeToOutputFile(text);
	}
	
	@Override
	protected void handleSectionElement(Attributes attributes) throws IOException, SAXException {
		super.handleSectionElement(attributes);
		
		// if the current section level is null, then this is a meta section and
		// should then *not* be added as a "normal" section
		if (getCurrentSectionLevel() != null) {
			currentSection = new Section(getCurrentSectionName(), getCurrentSectionLevel(), isRotateCurrentSection());
			sections.add(currentSection);
		}
	}
	
	@Override
	protected void handleMetaSectionElement(Attributes attributes)
			throws IOException, SAXException {
		super.handleMetaSectionElement(attributes);
		
		currentSection = new MetaSection(getCurrentSectionName(), isRotateCurrentSection());
		sections.add(currentSection);
	}
	
	@Override
	protected void handleUnknownElement(DocPart dp, Attributes attributes) throws Exception {
		if (dp == DocPart.PSECTION) {
			handlePseudoSectionElement(attributes);
		} else if (dp == DocPart.PPROCESSOR) {
			handlePostProcessorElement(attributes);
		} else if (dp == DocPart.HSECTION) {
			handleHeaderSectionElement(attributes);
		}
	}
	
	private void handlePostProcessorElement(Attributes attributes) throws Exception {
		String postProcessorHandlerClassname = attributes.getValue("classname");
		
		postProcessors.add(DocMakerMojo.newInstance(PostProcessor.class, postProcessorHandlerClassname));
	}

    public SplitTOCHandler() {
        this.postProcessors.add(new HeaderIncrementPostProcessor());
        this.postProcessors.add(new InjectHeaderIdPostProcessor());
    }

    @Override
    public void close() throws IOException {
        // Trap this, so we don't prematurely close the file
        if (this.writeToOutput) {
            super.close();
        }
    }

    @Override
    public void writeToOutputFile(final String text) throws IOException {
        if (this.writeToOutput && text != null) {
            super.writeToOutputFile(text);
        }
    }

    @Override
    protected void handleSectionElement(final Attributes attributes) throws IOException, SAXException {
        super.handleSectionElement(attributes);

        // if the current section level is null, then this is a meta section and
        // should then *not* be added as a "normal" section
        if (getCurrentSectionLevel() != null) {
            this.currentSection = new Section(getCurrentSectionName(), getCurrentSectionLevel(),
                    isRotateCurrentSection());
            this.sections.add(this.currentSection);
        }
    }

    @Override
    protected void handleMetaSectionElement(final Attributes attributes) throws IOException, SAXException {
        super.handleMetaSectionElement(attributes);

        this.currentSection = new MetaSection(getCurrentSectionName(), isRotateCurrentSection());
        this.sections.add(this.currentSection);
    }

    @Override
    protected void handleUnknownElement(final DocPart dp, final Attributes attributes) throws Exception {
        if (dp == DocPart.PSECTION) {
            handlePseudoSectionElement(attributes);
        } else if (dp == DocPart.PPROCESSOR) {
            handlePostProcessorElement(attributes);
        }
    }

    private void handlePostProcessorElement(final Attributes attributes) throws Exception {
        String postProcessorHandlerClassname = attributes.getValue("classname");

        this.postProcessors.add(Docmaker.newInstance(PostProcessor.class, postProcessorHandlerClassname));
    }

    protected void handlePseudoSectionElement(final Attributes attributes) throws Exception {
        setCurrentSectionName(attributes.getValue("title"));
        String pSectionHandlerClassname = attributes.getValue("classname");
        boolean rotateCurrentSection = attributes.getValue("rotate") != null;

        this.currentSection = new PseudoSection(getCurrentSectionName(), pSectionHandlerClassname, attributes,
                rotateCurrentSection);
        this.sections.add(this.currentSection);
    }

    @Override
    protected void handleElementElement(final Attributes attributes) throws IOException, SAXException {
        if (this.currentSection instanceof MetaSection) {
            super.handleElementElement(attributes);

            String key = attributes.getValue("key");
            if (this.metaData.containsKey(key)) {
                ((MetaSection) this.currentSection).addElement(key, this.metaData.get(key));
            }
        }
    }

    @Override
    protected void handleMetaElement(final String metaName, final Attributes attributes) throws IOException,
            SAXException {
        Map<String, String> meta = new HashMap<String, String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            meta.put(attributes.getQName(i), attributes.getValue(i));
        }

        this.htmlMeta.add(meta);
    }

    @Override
    protected String getFragmentAsHTML(final String repoName, final String fragmentName, final int chapterLevelOffset)
            throws IOException, URISyntaxException {
        if (this.currentSection instanceof Section) {
            // Deliberately using '0' as the offset
            String fragmentAsHtml = super.getFragmentAsHTML(repoName, fragmentName, 0);

            ((Section) this.currentSection).addChapter(fragmentName, repoName, chapterLevelOffset, fragmentAsHtml,
                    isRotateCurrentChapter());
        } else {
            throw new IllegalStateException("Current section: " + this.currentSection.getSectionName()
                    + " is not a standard section");
        }

        // We return null as we don't want to write anything to the file as yet
        return null;
    }

    @Override
    public void endDocument() throws SAXException {
        /*
         * We trap this method as an indication the TOC has now been fully processed.
         */
        /*
         * Now we assemble and write the output HTML doc
         */
        this.writeToOutput = true;
        try {
            // Start the document
            writeToOutputFile(DocPart.DOCUMENT.preElement());

            // Header section
            writeToOutputFile(DocPart.HEADER.preElement());
            writeTitleElement();
            writeCSSElement();

            /*
             * Write HTML meta tags
             */
            for (Map<String, String> meta : this.htmlMeta) {
                writeToOutputFile("<meta ");

                for (String key : meta.keySet()) {
                    writeToOutputFile(" " + key + "=\"" + meta.get(key) + "\"");
                }

                writeToOutputFile("/>");
            }

            writeToOutputFile(DocPart.HEADER.postElement());

            // Metadata
            writeToOutputFile(DocPart.SECTIONS.preElement());
            writeMetadataElement();

            // Sections
            for (BaseSection s : this.sections) {
                writeToOutputFile(DocPart.SECTION.preElement());
                writeToOutputFile(s.getDivOpenTag(this));

                if (s instanceof Section) {
                    writeToOutputFile(DocPart.CHAPTERS.preElement());

                    for (Chapter c : ((Section) s).getChapters()) {
                        writeToOutputFile(DocPart.CHAPTER.preElement());
                        // writeToOutputFile(c.getDivOpenTag(this));

                        String htmlFragment = c.getFragmentAsHtml();
                        for (PostProcessor pp : this.postProcessors) {
                            StringBuffer out = new StringBuffer();
                            pp.processFragment(c, htmlFragment, out, this);

                            htmlFragment = out.toString();
                        }

                        writeToOutputFile(htmlFragment);
                        // writeDivCloseTag();

                        writeToOutputFile(DocPart.CHAPTER.postElement());
                    }

                    writeToOutputFile(DocPart.CHAPTERS.postElement());
                    writeDivCloseTag();
                } else if (s instanceof PseudoSection) {
                    writeToOutputFile(((PseudoSection) s).getSectionHandler().getSectionAsHtml(this.sections, this));
                    writeDivCloseTag();
                } else if (s instanceof MetaSection) {
                    for (String[] e : ((MetaSection) s).getElements()) {
                        writeElement(e[0], e[1]);
                    }
                    writeDivCloseTag();
                }

                writeToOutputFile(DocPart.SECTION.postElement());
            }

            // All the post tags
            writeToOutputFile(DocPart.SECTIONS.postElement());
            writeToOutputFile(DocPart.DOCUMENT.postElement());

        } catch (IOException e) {
            throw new SAXException("Can not write output file", e);
        } finally {
            super.endDocument();
        }
    }
}
