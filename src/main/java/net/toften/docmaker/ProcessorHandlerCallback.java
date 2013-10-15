package net.toften.docmaker;


public interface ProcessorHandlerCallback extends InterimFileHandler {

	String getCurrentSectionName();
	
	Integer getCurrentSectionLevel();

	String getCurrentFragmentName();
	
	String getDocumentTitle();
}
