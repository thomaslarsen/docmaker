package net.toften.docmaker.headersections;

import java.util.regex.Matcher;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.pseudosections.TOCPseudoSection;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public class TOCBookmarkSection extends TOCPseudoSection {
	
	@Override
	public String getSectionAsHtml(TOC t) {
		StringBuffer asHtml = new StringBuffer("<bookmarks>\n");

		for (Section metaSection : t.getSections()) {
			if (metaSection.getDocPart() == DocPart.SECTION) {
				ChapterSection s = (ChapterSection)metaSection;
				int sectionLevel = s.getSectionLevel();

				if (sectionLevel <= getMaxLevel()) {
					asHtml.
					append("<bookmark name=\"" + s.getSectionName() + "\" href=\"#").
					append(s.getIdAttr(t)).
					append("\">\n");
					
					for (Chapter c : s.getChapters()) {
						processFragment(c, c.getAsHtml(), asHtml, t);
					}
					
					asHtml.append("</bookmark>\n");
				}

			}
		}

		asHtml.append("</bookmarks>\n");
		
		return asHtml.toString();
	}

	@Override
	public void processFragment(Chapter chapter, String fragmentAsHtml, StringBuffer out, TOC t) {
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
					append((chapter.getIdAttr(t) + "-" + headerText).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "")).
					append("\" />\n");
				}
			}
		}
	}
}
