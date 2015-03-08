package net.toften.docmaker;

public interface LogWrapper {
	public static final class NullLogWrapper implements LogWrapper {

		@Override
		public void debug(String message) {
		}

		@Override
		public void info(String message) {
		}

		@Override
		public void warn(String message) {
		}
	}
	
	void debug(String message);
	
    void info(String message);

    void warn(String message);
}
