package net.toften.docmaker;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public enum DocPart {
	//			TOC ELEMENT		HTML TAG	ADD DIV?
	DOCUMENT 	("document", 	"html", 	false) {
		@Override
		public String preElement() {
			// Override so we can write the proper xhtml bumpf
			return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
		}
	},
	/**
	 * This is the group of all the sections of the document.
	 * We expect to have only one of those per TOC
	 */
	SECTIONS 	("sections", 		"body", 	false),
	SECTION 	("section", 		null, 		true),
	METASECTION ("metasection",		null,		true),
	PSECTION	("psection",		null,		true),
	CHAPTERS 	("chapters", 		null, 		true),
	CHAPTER 	("chapter", 		null, 		false), 
	LINK 		("link", 			null, 		false),
	HEADER 		("header", 			"head", 	false), 
	META 		("meta", 			null, 		false),
	BASE 		("base", 			null, 		false),
	PROPERTIES 	("properties", 		null, 		true),
	PROPERTY 	("property", 		null, 		true),
	ELEMENT 	("element", 		null, 		true),
	REPOS 		("repos", 			null, 		false),
	REPO 		("repo", 			null, 		false),
	PROCESSORS	("processors",		null, 		false),
	POSTPROC	("postprocessor",	null, 		false),
	HSECTION	("hsection",		null, 		false),
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

	/**
	 * The default pre element method will insert a <div> tag with the doc part
	 * as the class. For example the SECTION will insert:
	 * 
	 * {@code
	 * 	<div class="section">
	 * }
	 * 
	 * @return
	 * 
	 * @see #writeDiv
	 */
	public String preElement() {
		return concat(writeDiv ? "<div class=\"" + name + "\">" : null, tag, "");
	}

	public String preElement(DocPartCallback c, Attributes a) {
		if (writeDiv) {
			String[][] e = c.getPreElementAttributes(this, a);

			if (e != null) {
				return preElement(e, false);
			} else
				return preElement();
		}
		
		return null;
	}
	
	public String preElement(String[][] e, boolean includeClass) {
		if (writeDiv) {
			String divTag = null;
			if (e != null) {
				divTag = "<div" + (includeClass ? " class=\"" + name + "\"": "");
				for (String[] ee : e) {
					divTag += " " + ee[0];
					divTag += "=\"" + ee[1] + "\"";
				}
	
				divTag +=">";
			}
			
			return concat(divTag, tag, "");
		}
		
		return null;
	}

	public String postElement() {
		return concat(writeDiv ? "</div>" : null, tag, "/");
	}

	public static String concat(String div, String tag, String postElement) {
		if (div == null) {
			if (tag == null) {
				return null;
			} else {
				return "<" + postElement + tag + ">\n";
			}
		} else {
			if (tag == null) {
				return div + "\n";
			} else {
				return div + "<" + postElement + tag + ">\n";
			}
		}
	}
}
