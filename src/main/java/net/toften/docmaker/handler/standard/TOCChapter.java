package net.toften.docmaker.handler.standard;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.TOC;

public class TOCChapter implements Chapter {
	public static final int EFFECTIVE_LEVEL_ADJUSTMENT = 2;
	public static String headerRegex = "(\\</?h)(\\d)(>)";
	
	private static Pattern p = Pattern.compile(headerRegex);
	
	private ContentSection section;
	private String fragmentName;
	private Repo repo;
	private int chapterLevelOffset;
	private String fragmentAsHtml;
	private boolean isRotated;
	private String extension;

	public TOCChapter(ContentSection section, String fragmentName, String config, AssemblyHandler handler, Repo repo, int chapterLevelOffset, boolean isRotated) throws Exception {
		this.section = section;
		this.fragmentName = fragmentName;
		this.repo = repo;
		this.chapterLevelOffset = chapterLevelOffset;
		this.isRotated = isRotated;
		
		// Normalise the fragment file extension
		this.extension = handler.getDefaultExtension();
		int i = this.fragmentName.lastIndexOf('.');
		if (i > 0) {
		    this.extension = this.fragmentName.substring(i + 1);
		} else {
			this.fragmentName += "." + this.extension;
		}
		
		InputStream fragmentIs = repo.getFragmentInputStream(this.fragmentName);
		fragmentAsHtml = handler.getMarkupProcessor(this.extension).process(fragmentIs, config, handler);
		fragmentIs.close();
	}
		
	/**
	 * This method returns the effective base heading level of a chapter.
	 * <p>
	 * Examples:
	 * 	SL	CL	EL	+
	 * 	1	0	1	0
	 * 	1	1	2	1
	 * 	1	2	3	2
	 * 	2	0	2	1
	 * 	2	1	3	2
	 * 
	 * @param currentSectionLevel
	 * @param chapterLevelOffset
	 * @return
	 */
	public int calcEffectiveLevel() {
		return getChapterLevelOffset() + getSection().getSectionLevel() - EFFECTIVE_LEVEL_ADJUSTMENT;
	}
	
	public ContentSection getSection() {
		return section;
	}
	
	public int getChapterLevelOffset() {
		return chapterLevelOffset;
	}
	
	public String getAsHtml() {
		return fragmentAsHtml;
	}
	
	public String getFragmentName() {
		return fragmentName;
	}
	
	public Repo getRepo() {
		return repo;
	}
	
	public boolean isRotated() {
		return isRotated;
	}

	@Override
	public String getDivOpenTag(TOC t) {
		String classAttr = getDivClassName() + (isRotated() ? " rotate" : "");
		
		return constructDivOpenTag(classAttr, getIdAttr(t), getFragmentName());
	}
	
	@Override
	public String getIdAttr(TOC t) {
		return (t.getTocFileName() + "-" + getSection().getSectionName() + "-" + getFragmentName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}

	private String getDivClassName() {
		return "chapter";
	}

	protected String constructDivOpenTag(String divClass, String divId, String divTitle) {
		return "<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">" + "\n";
	}

	public String getDivCloseTag() {
		return getSection().getDivCloseTag();
	}

	public String injectHeaderIdAttributes(AssemblyHandler handler) {
		Matcher m = p.matcher(fragmentAsHtml);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			if (m.group(0).charAt(1) != '/') { // Ignore rouge close tags
				// Handle open tag
				int l = Integer.valueOf(m.group(2));
				int start = m.end();
				m.appendReplacement(sb, ""); // Remove the open tag

				// Handle close tag
				m.find();
				int end = m.start();

				String headerText = fragmentAsHtml.substring(start, end);
				String headerId = (handler.getTocFileName() + "-" + getSection().getSectionName() + "-" + getFragmentName() + "-" + headerText).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
				String hReplace = "<h" + l + " id=\"" + headerId + "\">" + headerText + "</h" + l + ">";

				// Insert the new tag
				m.appendReplacement(sb, hReplace);

				// Delete the heading title that has been inserted by default
				sb.delete(sb.length() - headerText.length() - hReplace.length(), sb.length() - hReplace.length());
			} else
				m.appendReplacement(sb, "$1$2$3");
		}
		m.appendTail(sb);

		return sb.toString();
	}
	
	/**
	 * Increment the HTML <code>Hx</code> tag.
	 * <p>
	 * The Hx tag will be incremented with the amount of the <code>increment</code> parameter.
	 * If the line contains more than one Hx tag, they will all be incremented.
	 * 
	 * @param fragmentAsHtml the line if HTML (potentially) with Hx tag(s)
	 * @param increment the number to increment the Hx tag with
	 * @return
	 */
	public String incrementHTag(int increment) {
		if (fragmentAsHtml != null) {
			Matcher m = p.matcher(fragmentAsHtml);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				int l = Integer.valueOf(m.group(2));
				m.appendReplacement(sb, "$1" + (l + increment) + "$3");
			}
			m.appendTail(sb);

			return sb.toString();
		} else
			return null;
	}

	@Override
	public String runPostProcessors(List<PostProcessor> postProcessors, TOC t, boolean apply) {
		String htmlFragment = getAsHtml();
		
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