package net.toften.docmaker.pseudosections;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.BaseSection;
import net.toften.docmaker.Chapter;
import net.toften.docmaker.InjectHeaderIdPostProcessor;
import net.toften.docmaker.PseudoSectionHandler;
import net.toften.docmaker.Section;
import net.toften.docmaker.postprocessors.PostProcessor;

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
	public String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler) {
		StringBuffer asHtml = new StringBuffer("<div class=\"toc\">");

		for (BaseSection metaSection : sections) {
			if (metaSection instanceof Section) {
				Section s = (Section)metaSection;
				int sectionLevel = s.getSectionLevel();

				if (sectionLevel <= getMaxLevel()) {
					asHtml.
					append("<a class=\"toc-section level" + sectionLevel + "\" href=\"#").
					append(s.getIdAttr(handler)).
					append("\">").
					append(s.getSectionName()).
					append("</a>");
				}

				for (Chapter c : s.getChapters()) {
					processFragment(c, c.getFragmentAsHtml(), asHtml, handler);
				}
			}
		}

		asHtml.append("</div>");
		
		return asHtml.toString();
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, AssemblyHandler handler) {
		Matcher m = p.matcher(fragmentAsHtml);
		int chapterEffectiveLevel = chapter.calcEffectiveLevel();

		while (m.find()) {
			if (m.group(0).charAt(1) != '/') {	// Check it is not the close tag
				int hLevel = Integer.parseInt(m.group(1));
				String headerText = m.group(2);
				
				int effectiveLevel = hLevel + chapterEffectiveLevel;

				if (effectiveLevel <= getMaxLevel()) {
					out.
					append("<a class=\"toc-section level" + effectiveLevel + "\" href=\"#").
					append(chapter.getIdAttr(handler)).
					append("-" + headerText.trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "")).
					append("\">").
					append(headerText).
					append("</a>");
				}
			}
		}
	}
}
