package net.toften.docmaker;

import org.xml.sax.Attributes;

public interface DocPartCallback {
	/**
	 * This method must return a two-dimensional String array with a list of
	 * attributes that must be added to the pre element's start <div> tag.
	 * 
	 * @param dp
	 * @param attributes the attributes of the TOC element
	 * @return
	 */
	String[][] getPreElementAttributes(DocPart dp, Attributes attributes);
}
