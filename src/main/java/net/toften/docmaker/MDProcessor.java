package net.toften.docmaker;

import java.io.File;
import java.io.IOException;

public interface MDProcessor {

	String process(File inFile) throws IOException;

}
