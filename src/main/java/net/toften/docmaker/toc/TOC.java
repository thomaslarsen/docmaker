package net.toften.docmaker.toc;

import java.util.List;
import java.util.Map;

public interface TOC {

	Map<String, String> getMetaData();

	Map<String, Map<String, String>> getHtmlMeta();

	List<GeneratedSection> getHeaderSections();

	List<Section> getSections();

	String getTocFileName();

}
