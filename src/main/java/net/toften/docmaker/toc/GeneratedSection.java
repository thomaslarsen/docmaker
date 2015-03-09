package net.toften.docmaker.toc;

import net.toften.docmaker.LogWrapper;


public interface GeneratedSection extends Section {
	/**
	 * Return the contents to HTML
	 * 
	 * @return String containing the HTML
	 */
	public String getAsHtml(TOC t, LogWrapper lw);
}
