package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

/**
 * This {@link PostProcessor} will inject a {@code <span>} element after a
 * {@code <table>} or an {@code <img>} tag.
 * <p>
 * The <b>class</b> of the {@code <span>} element will be the same as the tag, i.e. for
 * an image, it will be {@ img} and for a table it will be {@code table}.
 * The contents of the {@code <span>} element will be:
 * <ul>
 * <li>For an {@code <img>}; the contents of the {@code alt} attribute</li>
 * <li>For a {@code <table>}; the contents of the {@code title} attribute</li>
 * </ul>
 * If these attributes are not present, the {@code <span>} element will
 * <b>not</b> be injected.
 * 
 * @author tlarsen
 *
 */
public class ImgTableNameInject extends RegexPostProcessor {
	@Override
	protected String getRegex() {
		return "<([img|table]+)\\s.*?[alt|title]=\"(.*?)\".*?>";
	}

	@Override
	protected String getReplacement(Matcher m) {
		return "$0 <span class=\"$1\" id=\"" + calcElementId(m.group(2)) + "\">$2</span>";
	}
}
