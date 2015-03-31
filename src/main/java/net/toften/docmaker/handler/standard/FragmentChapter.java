package net.toften.docmaker.handler.standard;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.TOC;

public class FragmentChapter extends BaseSection implements Chapter {
	public static final int EFFECTIVE_LEVEL_ADJUSTMENT = 2;
	private static final Logger lw = Logger.getLogger(FragmentChapter.class.getName());
	
	private final ContentSection section;
	private final Repo repo;
	private final int chapterLevelOffset;
	private String fragmentAsHtml;
	private String fragmentFilename;

	public FragmentChapter(ContentSection section, String name, String config, AssemblyHandler handler, Repo repo, int chapterLevelOffset, boolean isRotated) throws Exception {
		super(name, isRotated);

		if (handler == null)
			throw new NullPointerException("AssemblyHandler for chapter " + name + " is null");
		
		if (section == null)
			throw new NullPointerException("Parent section for chapter " + name + " is null");
		
		if (repo == null)
			throw new NullPointerException("Repo for chapter " + name + " is null");
		
		this.section = section;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		
		// Normalise the fragment file extension
		fragmentFilename = name;
		String extension = handler.getDefaultExtension();
		int i = name.lastIndexOf('.');
		if (i > 0) {
		    extension = name.substring(i + 1);
		} else {
			fragmentFilename += "." + extension;
		}
		
		lw.fine("Chapter " + name + " has been initialised\n"
				+ "File name: " + fragmentFilename + "\n"
 				+ "Parent: " + section.getName() + "\n"
				+ "Repo: " + repo.getId() + "\n"
				+ "Level offset: " + chapterLevelOffset + "\n"
				+ "Effective level: " + calcEffectiveLevel() + "\n"
				+ "Rotated: " + isRotated);
		
		// Load and process the fragment
		InputStream fragmentIs = getRepo().getFragmentInputStream(fragmentFilename);
		fragmentAsHtml = handler.getMarkupProcessor(extension).process(fragmentIs, config, handler);
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
	
	public String getAsHtml(TOC t) {
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
	public String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply) {
		String htmlFragment = getAsHtml(t);
		
		// Run postprocessors
		for (PostProcessor pp : postProcessors) {
			StringBuffer out = new StringBuffer();
			pp.processFragment(this, htmlFragment, out, t);
			
			htmlFragment = out.toString();
		}
		
		if (apply)
			fragmentAsHtml = htmlFragment;

		return htmlFragment;
	}
}