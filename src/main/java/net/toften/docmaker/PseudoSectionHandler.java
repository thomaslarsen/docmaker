package net.toften.docmaker;

import java.util.List;

import net.toften.docmaker.postprocessors.PostProcessor;

public interface PseudoSectionHandler extends PostProcessor {
	String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler);
}
