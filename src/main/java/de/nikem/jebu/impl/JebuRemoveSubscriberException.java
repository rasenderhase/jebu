package de.nikem.jebu.impl;

import de.nikem.jebu.api.JebuException;

public class JebuRemoveSubscriberException extends JebuException {

	public JebuRemoveSubscriberException() {
	}

	public JebuRemoveSubscriberException(String message) {
		super(message);
	}

	public JebuRemoveSubscriberException(Throwable cause) {
		super(cause);
	}

	public JebuRemoveSubscriberException(String message, Throwable cause) {
		super(message, cause);
	}

	public JebuRemoveSubscriberException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
