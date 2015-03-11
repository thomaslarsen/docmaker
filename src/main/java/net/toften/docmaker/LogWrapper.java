package net.toften.docmaker;

/**
 * This interface is used as a facade to any underlying logging framework.
 * 
 * @author thomaslarsen
 *
 */
public interface LogWrapper {
	
	void debug(String message);
	
    void info(String message);

    void warn(String message);
}
