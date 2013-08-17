package net.toften.docmaker;


public interface ProcessorHandlerCallback extends OutputFileHandler {

	String getCurrentSectionName();
	
	int getCurrentSectionLevel();

	String getCurrentFragmentName();
	
	String getDocumentTitle();
}
