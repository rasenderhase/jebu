package de.nikem.jebu.impl;

import de.nikem.jebu.api.JebuException;

public class JebuRemoveSubscriberException extends JebuException {
	private static final long serialVersionUID = -4848983787088248931L;

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
}
