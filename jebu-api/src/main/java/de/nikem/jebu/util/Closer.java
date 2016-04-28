package de.nikem.jebu.util;

import java.io.Closeable;
import java.io.IOException;

public final class Closer {

	private Closer() {
		// TODO Auto-generated constructor stub
	}

	public static void close(Closeable toClose) {
		try {
			if (toClose != null) {
					toClose.close();
			}
		} catch (IOException ignore) {
			// ignore
		}
	}
}
