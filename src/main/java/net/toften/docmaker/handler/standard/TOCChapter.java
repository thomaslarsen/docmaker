package net.toften.docmaker.handler.standard;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.TOC;

public class TOCChapter extends BaseSection implements Chapter {
	public static final int EFFECTIVE_LEVEL_ADJUSTMENT = 2;
	
	private ContentSection section;
	private Repo repo;
	private int chapterLevelOffset;
	private String fragmentAsHtml;

	public TOCChapter(ContentSection section, String name, String config, AssemblyHandler handler, Repo repo, int chapterLevelOffset, boolean isRotated, LogWrapper lw) throws Exception {
		super(name, isRotated);
		
		this.section = section;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		
		// Normalise the fragment file extension
		String fragmentFilename = getName();
		String extension = handler.getDefaultExtension();
		int i = getName().lastIndexOf('.');
		if (i > 0) {
		    extension = getName().substring(i + 1);
		} else {
			fragmentFilename += "." + extension;
		}
		
		// Load and process the fragment
		InputStream fragmentIs = getRepo().getFragmentInputStream(fragmentFilename);
		fragmentAsHtml = handler.getMarkupProcessor(extension).process(fragmentIs, config, handler, lw);
		fragmentIs.close();
	}
		
	public int calcEffectiveLevel() {
		return getChapterLevelOffset() + getSection().getSectionLevel() - EFFECTIVE_LEVEL_ADJUSTMENT;
	}
	
	public ContentSection getSection() {
		return section;
	}
	
	public int getChapterLevelOffset() {
		return chapterLevelOffset;
	}
	
	public String getAsHtml(TOC t, LogWrapper lw) {
		return fragmentAsHtml;
	}
	
	public Repo getRepo() {
		return repo;
	}
	
	public URI getFragmentURI() throws URISyntaxException {
		return getRepo().getFragmentURI(getName());
	}
	
	@Override
	public String getIdAttr(TOC t) {
		return (getSection().getIdAttr(t) + "-" + getName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
	
	@Override
	public DocPart getDocPart() {
		return DocPart.CHAPTER;
	}

	@Override
	public String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply, LogWrapper lw) {
		String htmlFragment = getAsHtml(t, lw);
		
		// Run postprocessors
		for (PostProcessor pp : postProcessors) {
			StringBuffer out = new StringBuffer();
			pp.processFragment(this, htmlFragment, out, t, lw);
			
			htmlFragment = out.toString();
		}
		
		if (apply)
			fragmentAsHtml = htmlFragment;

		return htmlFragment;
	}
}