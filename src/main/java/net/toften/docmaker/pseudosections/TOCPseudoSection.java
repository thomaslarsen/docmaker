package net.toften.docmaker.pseudosections;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.postprocessors.RegexPostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

/**
 * This pseudo section will generate a table-of-contents section from a TOC.
 * <p>
 * The table-of-contents will be generated as a list of {@code <a>} links, surrounded by
 * a {@code <div>} tag:
 * 
 * {@code
 * 	<div class="toc">
 * 		<a class="toc-section level1" href="#toc-section-chapter-heading">Heading</a>
 * 		...
 * 	</div>
 * }
 * 
 * <p>
 * An element will be inserted for the following elements extracted from the TOC and
 * the HTML contents:
 * <ul>
 * <li>Section</li>
 * <li>Chapter</li>
 * <li>Heading, as extracted from the chapter contents after it is converted to HTML</li>
 * </ul>
 * 
 * <p>
 * A maximum header level to include in the table-of-contents can be specified in the TOC:
 * 
 * For example:
 * 
 * {@code
 * 	...
 * 		<psection classname="net.toften.docmaker.pseudosections.TOCPseudoSection" level="3" />
 * 	...
 * }
 * 
 * where the maximum header level to include is {@code h3}.
 * 
 * @author thomaslarsen
 *
 */
public class TOCPseudoSection implements PseudoSectionHandler, PostProcessor {
	private static Logger lw = Logger.getLogger(TOCPseudoSection.class.getName());	

	protected static Pattern p = Pattern.compile(InjectHeaderIdPostProcessor.HEADER_SEARCH_REGEX);

	private int maxLevel;

	@Override
	public void init(Attributes attributes) {
		maxLevel = Integer.valueOf(attributes.getValue("level"));
	}

	protected int getMaxLevel() {
		return maxLevel;
	}
	
	@Override
	public String getSectionAsHtml(TOC t) {
		lw.info("Writing TOC section, max level " + maxLevel);
		
		StringBuffer asHtml = new StringBuffer("<div class=\"toc\">\n");

		for (Section s : t.getSections()) {
			if (s.getDocPart() == DocPart.SECTION) {
				ChapterSection cs = (ChapterSection)s;
				int level = cs.getSectionLevel();

				lw.fine("TOC section (level " + level + ")" + (level > getMaxLevel() ? "[SKIPPED]" : "") +": " + cs.getName());
				if (level <= getMaxLevel()) {
					asHtml.
					append("<a class=\"toc-section level" + level + "\" href=\"#").
					append(cs.getIdAttr(t)).
					append("\">").
					append(cs.getName()).
					append("</a>\n");
				}

				for (Chapter c : cs.getChapters()) {
					processFragment(c, c.getAsHtml(t), asHtml, t);
				}
			}
		}

		asHtml.append("</div>\n");
		
		return asHtml.toString();
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, TOC t) {
		Matcher m = p.matcher(fragmentAsHtml);
		int chapterEffectiveLevel = chapter.calcEffectiveLevel();

		while (m.find()) {
			if (m.group(0).charAt(1) != '/') {	// Check it is not the close tag
				int hLevel = Integer.parseInt(m.group(1));
				String headerText = m.group(3);
				
				int level = hLevel + chapterEffectiveLevel;

				lw.fine("TOC chapter (level " + level + "/" + hLevel + "/" + chapterEffectiveLevel + ")" + (level > getMaxLevel() ? "[SKIPPED]" : "") +": " + chapter.getName());
				
				if (level <= getMaxLevel()) {
					out.
					append("<a class=\"toc-section level" + level + "\" href=\"#").
					append(RegexPostProcessor.calcHeaderId(t, chapter, headerText)).
					append("\">").
					append(headerText).
					append("</a>\n");
				}
			}
		}
	}
}
