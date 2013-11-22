package net.toften.docmaker.pseudosections;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.BaseSection;
import net.toften.docmaker.Chapter;
import net.toften.docmaker.DefaultAssemblyHandler;
import net.toften.docmaker.PseudoSectionHandler;
import net.toften.docmaker.Section;

import org.xml.sax.Attributes;

public class TOCPseudoSection implements PseudoSectionHandler {
	private static Pattern p = Pattern.compile(DefaultAssemblyHandler.headerRegex);

	private int maxLevel;

	@Override
	public void init(Attributes attributes) {
		maxLevel = Integer.valueOf(attributes.getValue("level"));
	}

	@Override
	public String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler) {
		StringBuffer asHtml = new StringBuffer("<div class=\"toc\">");

		for (BaseSection metaSection : sections) {
			if (metaSection instanceof Section) {
				Section s = (Section)metaSection;
				int sectionLevel = s.getSectionLevel();

				if (sectionLevel <= maxLevel) {
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

		return asHtml.append("</div>").toString();
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, AssemblyHandler handler) {
		Matcher m = p.matcher(fragmentAsHtml);
		
		while (m.find()) {
			if (m.group(0).charAt(1) != '/') {	// Check it is not the close tag
				int hLevel = Integer.parseInt(m.group(2));
				int effectiveLevel = hLevel + chapter.calcEffectiveLevel();

				if (effectiveLevel <= maxLevel) {
					int start = m.end();
					m.find();
					int end = m.start();
					String heading = fragmentAsHtml.substring(start, end);

					out.
					append("<a class=\"toc-section level" + effectiveLevel + "\" href=\"#").
					append(chapter.getIdAttr(handler)).
					append("\">").
					append(heading).
					append("</a>");
				}
			}
		}
	}
}
