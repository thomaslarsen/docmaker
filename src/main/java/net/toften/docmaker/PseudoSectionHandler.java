package net.toften.docmaker;

import java.util.List;

public interface PseudoSectionHandler extends PostProcessor {
	String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler);
}
