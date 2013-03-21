package net.toften.docmaker;

import java.util.HashMap;
import java.util.Map;

public enum DocPart {
	DOCUMENT ("document", null, null, false),
	SECTIONS ("sections", "<body>", "</body>", false),
	SECTION ("section", null, null, true),
	CHAPTERS ("chapters", null, null, true),
	CHAPTER ("chapter", null, null, true), 
	LINK ("link", null, null, false),
	HEADER ("header", "<head>", "</head>", false),
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
