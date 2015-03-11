package net.toften.docmaker.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

/**
 * This {@link PostProcessor} will identify broken links, referencing sections
 * or headers in the output (cross-references).
 * 
 * @author thomaslarsen
 *
 */
public class BrokenLinks implements OutputProcessor {
	private static Logger lw = Logger.getLogger(BrokenLinks.class.getName());
	
	private static String HEADER_ID_REGEX = "<h(\\d).*?id=\\\"(.*?)\\\"";
	private static String LINK_HREF_REGEX = "<a.*href=\"#(.*?)\".*</a>";
	
	@Override
	public void process(File outputDir, String outputName, String encoding, TOC t) throws Exception {
		
		// Build list of link target ids
		List<String> ids = new ArrayList<String>();

		Pattern p = Pattern.compile(HEADER_ID_REGEX);
		for (Section s : t.getSections()) {
			if (s.getDocPart() == DocPart.SECTION) {
				ChapterSection cs = (ChapterSection)s;
				
				ids.add(cs.getIdAttr(t));
				
				for (Chapter c : cs.getChapters()) {
					Matcher m = p.matcher(c.getAsHtml(t));
					
					while (m.find()) {
						ids.add(m.group(2));
						lw.fine("Found header (" + m.group(1) + ") id: " + m.group(2) + " in " + c.getName());
					}
				}
			}
		}
		
		// Check for broken links
		p = Pattern.compile(LINK_HREF_REGEX);
		for (Section s : t.getSections()) {
			if (s.getDocPart() == DocPart.SECTION) {
				ChapterSection cs = (ChapterSection)s;
				
				for (Chapter c : cs.getChapters()) {
					Matcher m = p.matcher(c.getAsHtml(t));
					
					while (m.find()) {
						lw.fine("Found link href: " + m.group(1) + " in " + c.getName());
						
						if (!ids.contains(m.group(1))) {
							lw.warning("Link href: " + m.group(1) + " in " + c.getName() + " NOT FOUND. Full link: " + m.group(0));
						}
					}
					
				}
			}
		}
	}

	@Override
	public String getFileExtension() {
		return null;
	}
}
