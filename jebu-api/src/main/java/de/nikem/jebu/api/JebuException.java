package de.nikem.jebu.api;

public class JebuException extends RuntimeException {
	private static final long serialVersionUID = 4321771063829000613L;

	public JebuException() {
	}

	public JebuException(String message) {
		super(message);
	}

	public JebuException(Throwable cause) {
		super(cause);
	}

	public JebuException(String message, Throwable cause) {
		super(message, cause);
	}
}
