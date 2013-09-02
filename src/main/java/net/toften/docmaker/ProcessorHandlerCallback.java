package net.toften.docmaker;


public interface ProcessorHandlerCallback extends OutputFileHandler {

	String getCurrentSectionName();
	
	Integer getCurrentSectionLevel();

	String getCurrentFragmentName();
	
	String getDocumentTitle();
}
