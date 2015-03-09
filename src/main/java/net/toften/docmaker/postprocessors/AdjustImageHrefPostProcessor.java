package net.toften.docmaker.postprocessors;

import java.util.regex.Matcher;

/**
 * Adjusts the href of image tags, so that they are relative to the interim file.
 * </br>
 * 
 * The assumption is, that the image href is correct in relation to the source markup file.
 * For example, if the markup is:
 * 
 * 	!(./images/picture.jpg)
 * 
 * This will be converted into the following HTML:
 * 
 * 	<img href="./images/picture.jpg" />
 * 
 * The absolute path to the image will need to take the repo URI into account.
 * For example if the repo URI is "./src/main/resources/doc/markdown" and the
 * base URI is "/home/me/project", then the absolute path to the image will be:
 * 
 * 	/home/me/project/src/main/resources/doc/markdown/images/picture.jpg
 * 
 * or
 * 
 * 	<base uri>/<repo path>/<image path>
 * 
 * @author thomaslarsen
 *
 */
public class AdjustImageHrefPostProcessor extends RegexPostProcessor {
	@Override
	protected String getRegex() {
		return "<img(.*?)src=\"(.*?)\"(.*?)/>";
	}

	@Override
	protected String getReplacement(Matcher m) {
		String href = getCurrentChapter().getRepo().getURI().resolve("./" + m.group(2)).normalize().getPath();
		
		return	"<img$1src=\"" + href + "\"$3/>";
	}

}
