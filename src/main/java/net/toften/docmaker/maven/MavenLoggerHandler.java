package net.toften.docmaker.maven;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.maven.plugin.logging.Log;

public class MavenLoggerHandler extends Handler {
	private Log l;

	public MavenLoggerHandler(Log l) {
		this.l = l;
		
	}
	
	@Override
	public void publish(LogRecord record) {
		Level level = record.getLevel();
		
		if (level == Level.INFO)
			l.info(record.getMessage(), record.getThrown());
		else if (level == Level.SEVERE)
			l.error(record.getMessage(), record.getThrown());
		else if (level == Level.WARNING)
			l.warn(record.getMessage(), record.getThrown());
		else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST)
			l.debug(record.getMessage(), record.getThrown());
	}

	@Override
	public void flush() {
		// Empty
	}

	@Override
	public void close() throws SecurityException {
		// Empty
	}
}
