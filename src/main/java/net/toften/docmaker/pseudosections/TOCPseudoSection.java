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

	private int level;

	@Override
	public void init(Attributes attributes) {
		level = Integer.valueOf(attributes.getValue("level"));
	}

	@Override
	public String getSectionAsHtml(List<BaseSection> sections, AssemblyHandler handler) {
		StringBuffer asHtml = new StringBuffer("<div class=\"toc\">");

		for (BaseSection metaSection : sections) {
			if (metaSection instanceof Section) {
				Section s = (Section)metaSection;
				int sectionLevel = s.getSectionLevel();
				
				if (sectionLevel <= level) {
					asHtml.
					append("<a class=\"toc-section level" + sectionLevel + "\" href=\"#").
					append((handler.getTocFileName() + "-" + s.getSectionName()).toLowerCase().replace(' ', '-')).
					append("\">").
					append(s.getSectionName()).
					append("</a>");
				}
				
				for (Chapter c : s.getChapters()) {
					int effectiveLevel = DefaultAssemblyHandler.calcEffectiveLevel(sectionLevel, c.getChapterLevelOffset());
					
					String fragment = c.getFragmentAsHtml();
					Matcher m = p.matcher(fragment);
					while (m.find()) {
						if (m.group(0).charAt(1) != '/') {	// Check it is not the close tag
							int hLevel = Integer.parseInt(m.group(2));
							
							if (hLevel + effectiveLevel <= level) {
								int start = m.end();
								m.find();
								int end = m.start();
								String heading = fragment.substring(start, end);
								
								asHtml.
								append("<a class=\"toc-section level" + (hLevel + effectiveLevel) + "\" href=\"#").
								append((handler.getTocFileName() + "-" + c.getRepoName() + "-" + s.getSectionName() + "-" + c.getFragmentName() + "-" + heading).toLowerCase().replace(' ', '-')).
								append("\">").
								append(heading).
								append("</a>");
							}
						}
					}
				}
			}
		}
		
		return asHtml.append("</div>").toString();
	}
}
