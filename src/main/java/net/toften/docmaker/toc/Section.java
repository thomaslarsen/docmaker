package net.toften.docmaker.toc;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.output.InterimFileHandler;

public interface Section {
	/**
	 * Return the {@link DocPart} that correspond to the section type.
	 * 
	 * @return
	 */
	DocPart getDocPart();
	
	/**
	 * Return the complete <div> open tag to include in the {@link InterimFileHandler interim file}
	 * 
	 * @param t the processed TOC
	 * @return the div tag, or <code>null</code> if no div tag should be written
	 */
	String getDivOpenTag(TOC t);

	/**
	 * The </div> close tag.
	 * 
	 * @return the div tag, or <code>null</code> if no div tag should be written
	 */
	String getDivCloseTag();
	
	/**
	 * @return <code>true</code> if the contents is rotated
	 */
	boolean isRotated();
	
	/**
	 * Return the name as specified in the title attribute in the TOC element:
	 * 
	 * @return the name
	 */
	String getName();
	
	/**
	 * Returns the ID attribute for the chapter.
	 * <p>
	 * This should be used as the value of the id attribute in the <div> tag that surrounds
	 * the chapter section.
	 * This will allow links to reference the chapter section specifically.
	 * <p>
	 * The ID attribute should only contain upper and lower case characters, digits and "-"
	 * 
	 * @param t
	 * @return
	 */
	String getIdAttr(TOC t);
}
