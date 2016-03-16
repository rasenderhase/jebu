package de.nikem.jebu.api;

public interface Subscriber {
	void publish(String eventName, Object data);
}
