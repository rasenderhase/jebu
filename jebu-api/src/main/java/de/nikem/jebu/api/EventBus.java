package de.nikem.jebu.api;

/**
 * jebu event bus
 * @author uawet0ju
 *
 */
public interface EventBus extends Subscriber {
	/**
	 * register a subscriber for a certain event name
	 * @param eventName name of the event. If empty the subscriber will be notified
	 * by all events
	 * @param subscriber subscriber (see {@link Subscriber})
	 */
	void subscribe(String eventName, Subscriber subscriber);
	/**
	 * unregister a subscriber from an event name.
	 * @param eventName name of the event to unregister from.
	 * @param subscriber the subscriber to unregsiter
	 */
	void unsubscribe(String eventName, Subscriber subscriber);
	/**
	 * unregister the subscriber from all events
	 * @param subscriber the subscriber to unregsiter
	 */
	void unsubscribe(Subscriber subscriber);
}
