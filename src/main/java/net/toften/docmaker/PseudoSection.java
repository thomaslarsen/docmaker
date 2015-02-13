package net.toften.docmaker;

import org.xml.sax.Attributes;

public class PseudoSection extends BaseSection {

    private PseudoSectionHandler sectionHandler;

    public PseudoSection(final String sectionName, final String pSectionHandlerClassname, final Attributes attributes,
            final boolean isRotated) throws Exception {
        super(sectionName, isRotated);

        this.sectionHandler = Docmaker.newInstance(PseudoSectionHandler.class, pSectionHandlerClassname);
        this.sectionHandler.init(attributes);
    }

    public PseudoSectionHandler getSectionHandler() {
        return this.sectionHandler;
    }

    @Override
    protected String getDivClassName() {
        return "pseudo-section";
    }
}
