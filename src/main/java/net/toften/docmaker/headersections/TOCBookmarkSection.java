package net.toften.docmaker.headersections;

import java.util.List;
import java.util.regex.Matcher;

import net.toften.docmaker.AssemblyHandler;
import net.toften.docmaker.BaseSection;
import net.toften.docmaker.Chapter;
import net.toften.docmaker.Section;
import net.toften.docmaker.pseudosections.TOCPseudoSection;

public class TOCBookmarkSection extends TOCPseudoSection {
	
	@Override
	public String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler) {
		StringBuffer asHtml = new StringBuffer("<bookmarks>");

		for (BaseSection metaSection : sections) {
			if (metaSection instanceof Section) {
				Section s = (Section)metaSection;
				int sectionLevel = s.getSectionLevel();

				if (sectionLevel <= getMaxLevel()) {
					asHtml.
					append("<bookmark name=\"" + s.getSectionName() + "\" href=\"#").
					append(s.getIdAttr(handler)).
					append("\">");
					
					for (Chapter c : s.getChapters()) {
						processFragment(c, c.getFragmentAsHtml(), asHtml, handler);
					}
					
					asHtml.append("</bookmark>");
				}

			}
		}

		asHtml.append("</bookmarks>");
		
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
					append("<bookmark name=\"" + headerText + "\" href=\"#").
					append(chapter.getIdAttr(handler)).
					append("-" + headerText.trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "")).
					append("\" />");
				}
			}
		}
	}

}
