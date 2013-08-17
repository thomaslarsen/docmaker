package net.toften.docmaker;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public enum DocPart {
	//			TOC ELEMENT		HTML TAG	ADD DIV?
	DOCUMENT 	("document", 	"html", 	false) {
		@Override
		public String preElement() {
			return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
		}
	},
	/**
	 * This is the group of all the sections of the document.
	 * We expect to have only one of those per TOC
	 */
	SECTIONS 	("sections", 	"body", 	false),
	SECTION 	("section", 	null, 		true),
	CHAPTERS 	("chapters", 	null, 		true),
	CHAPTER 	("chapter", 	null, 		true), 
	LINK 		("link", 		null, 		false),
	HEADER 		("header", 		"head", 	false), 
	META 		("meta", 		null, 		false),
	PROPERTY 	("property", 	null, 		false),
	ELEMENT 	("element", 	null, 		true),
	REPO 		("repo", 		null, 		false),
	;

	private String name;
	private String tag;
	private boolean writeDiv;
	private static Map<String, DocPart> lookup;

	private DocPart(String name, String tag, boolean writeDiv) {
		this.name = name;
		this.tag = tag;
		this.writeDiv = writeDiv;
	}

	public static DocPart valueOfString(String qName) {
		if (lookup == null) {
			lookup = new HashMap<String, DocPart>();

			for (DocPart dp : DocPart.values()) {
				lookup.put(dp.name, dp);
			}
		}

		return lookup.get(qName);
	}

	public String preElement() {
		return concat(writeDiv ? "<div class=\"" + name + "\">" : null, tag, "");
	}

	public String preElement(DocPartCallback c, Attributes a) {
		String divTag = null;

		if (writeDiv) {
			String[][] e = c.getPreElementAttributes(this, a);

			if (e != null) {
				divTag = "<div";
				for (String[] ee : e) {
					divTag += " " + ee[0];
					divTag += "=\"" + ee[1] + "\"";
				}

				divTag +=">";
			} else
				return preElement();
		}

		return concat(divTag, tag, "");
	}

	public String postElement() {
		return concat(writeDiv ? "</div>" : null, tag, "/");
	}

	private String concat(String div, String tag, String postElement) {
		if (div == null) {
			if (tag == null) {
				return null;
			} else {
				return "<" + postElement + tag + ">";
			}
		} else {
			if (tag == null) {
				return div;
			} else {
				return div + "<" + postElement + tag + ">";
			}
		}
	}
}
