package nes.exceptions;

public class NotNesFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -496075772082376474L;

	public NotNesFileException() {
	}

	public NotNesFileException(String message) {
		super(message);
	}

	public NotNesFileException(Throwable cause) {
		super(cause);
	}

	public NotNesFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotNesFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
