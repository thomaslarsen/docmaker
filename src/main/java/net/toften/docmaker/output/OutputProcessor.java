package net.toften.docmaker.output;

import java.io.File;

public interface OutputProcessor {

	void process(File inFile, String outFileName) throws Exception;

	String getFileExtension();
}