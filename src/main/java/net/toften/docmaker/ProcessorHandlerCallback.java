package net.toften.docmaker;

import java.io.IOException;

public interface ProcessorHandlerCallback {

	String getCurrentSectionName();
	
	int getCurrentSectionLevel();

	void writeToOutputFile(String text) throws IOException;
}
