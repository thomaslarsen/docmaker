package net.toften.docmaker.markup.markdown.txtmark;

import net.toften.docmaker.handler.AssemblyHandler;

import com.github.rjeschke.txtmark.DefaultDecorator;

public class HDecorator extends DefaultDecorator {
	private AssemblyHandler callback;

	public HDecorator(AssemblyHandler callback) {
		this.callback = callback;
	}
	
	@Override
	public void openImage(StringBuilder out) {
		String sectionName = callback.getCurrentSectionName();
		out.append("<img class=\"" + sectionName + "\"");
	}
}
