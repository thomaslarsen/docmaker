package net.toften.docmaker;

import java.util.List;

import org.xml.sax.Attributes;

public interface PseudoSectionHandler {

	String getSectionAsHtml(List<MetaSection> sections, AssemblyHandler handler);

	void init(Attributes attributes);

}
