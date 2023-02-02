package exceptions;

public class InstructionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6083985389142811290L;

	public InstructionException() {
	}

	public InstructionException(String message) {
		super(message);
	}

	public InstructionException(Throwable cause) {
		super(cause);
	}

	public InstructionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstructionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
