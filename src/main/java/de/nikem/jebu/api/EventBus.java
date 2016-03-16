package de.nikem.jebu.api;

public interface EventBus extends Subscriber {
	void subscribe(String eventName, Subscriber subscriber);
	void unsubscribe(String eventName, Subscriber subscriber);
	void unsubscribe(Subscriber subscriber);
}
