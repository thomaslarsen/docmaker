package net.toften.docmaker;

import java.util.HashMap;
import java.util.Map;

public enum DocPart {
	DOCUMENT ("document", null, null, false),
	/**
	 * This is the group of all the sections of the document.
	 * We expect to have only one of those per TOC
	 */
	SECTIONS ("sections", "<body>", "</body>", false),
	SECTION ("section", null, null, true),
	CHAPTERS ("chapters", null, null, true),
	CHAPTER ("chapter", null, null, false), 
	LINK ("link", null, null, false),
	HEADER ("header", "<head>", "</head>", false), 
	META ("meta", null, null, false),
	PROPERTIY ("property", null, null, false),
	ELEMENT ("element", null, null, true),
	REPO ("repo", null, null, false),
	;
	
	private String name;
	private String preElement;
	private String postElement;
	private boolean writeDiv;
	private static Map<String, DocPart> lookup;

	private DocPart(String name, String preElement, String postElement, boolean writeDiv) {
		this.name = name;
		this.preElement = preElement;
		this.postElement = postElement;
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
		return concat(writeDiv ? "<div class=\"" + name + "\">" : null, preElement);
	}

	public String postElement() {
		return concat(writeDiv ? "</div>" : null, postElement);
	}
	
	private String concat(String s1, String s2) {
		if (s1 == null) {
			if (s2 == null) {
				return null;
			} else {
				return s2;
			}
		} else {
			if (s2 == null) {
				return s1;
			} else {
				return s1 + s2;
			}
		}
	}
}
