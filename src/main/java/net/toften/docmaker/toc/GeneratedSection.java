package net.toften.docmaker.toc;

public interface GeneratedSection extends Section {
	/**
	 * Return the contents to HTML
	 * 
	 * @return String containing the HTML
	 */
	public String getAsHtml(TOC t);
}
