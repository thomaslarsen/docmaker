package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

/**
 * This {@link PostProcessor} is used to inject a class into a generated {@code <blockquote>} element.
 * <p>
 * The class will be determined using the following rules.
 * <p>
 * For example given this markup (in Markdown):
 * 
 * {@code
 * > **Note**
 * >
 * > This is a note, that I want to draw attention to!
 * }
 * 
 * The Markdown will be converted to the following HTML:
 * 
 * {@code
 * 	...
 * 		<blockquote>
 * 			<p><strong>Note</strong></p>
 * 			<p>This is a note, that I want to draw attention to!</p>
 * 		</blockquote>
 * 	...
 * }
 * 	
 * This post processor will inject the following {@code class} attribute into the {@code blockquote} element:
 * 
 * {@code
 * 	...
 * 		<blockquote class="Note">
 * 			<p><strong>Note<...
 * }
 * 
 * The remaining contents of the {@code blockquote} will remain unchanged.
 * <p>
 * The injected class can then be used to provide specific formating to the blockquote using CSS.	
 * 
 * @author thomaslarsen
 *
 */
public class BQProcessor extends RegexPostProcessor {

	@Override
	protected String getRegex() {
		return "<blockquote>(.*)<strong>(.*)</strong>";
	}

	@Override
	protected String getReplacement(Matcher m) {
		return "<blockquote class=\"$2\">$1<strong>$2</strong>";
	}
}
