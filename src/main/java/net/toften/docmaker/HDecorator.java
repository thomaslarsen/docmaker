package net.toften.docmaker;

import com.github.rjeschke.txtmark.DefaultDecorator;

public class HDecorator extends DefaultDecorator {
	private ProcessorHandlerCallback callback;

	public HDecorator(ProcessorHandlerCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public void openImage(StringBuilder out) {
		String sectionName = callback.getCurrentSectionName();
		out.append("<img class=\"" + sectionName + "\"");
	}
	
	
}
