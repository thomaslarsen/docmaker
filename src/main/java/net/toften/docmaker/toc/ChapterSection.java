package net.toften.docmaker.toc;

import java.util.List;

public interface ChapterSection extends Section, ElementsSection {
	List<Chapter> getChapters();

	Integer getSectionLevel();
}
