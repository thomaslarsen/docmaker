package net.toften.docmaker;

import org.xml.sax.Attributes;

public interface DocPartCallback {
	String[][] getPreElementAttributes(DocPart dp, Attributes attributes);
}
