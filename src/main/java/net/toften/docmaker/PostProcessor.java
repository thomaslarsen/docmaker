package net.toften.docmaker;

import java.io.File;

public interface PostProcessor {

	void postProcess(File inFile, String outFileName) throws Exception;

	String getFileExtension();
}