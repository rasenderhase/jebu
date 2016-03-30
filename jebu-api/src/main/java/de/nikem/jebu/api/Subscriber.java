package de.nikem.jebu.api;

/**
 * Classes that implement this interface can subscribe
 * to jeubu {@link EventBus}
 * @author uawet0ju
 */
public interface Subscriber {
	/**
	 * Method is invoked on events that match the eventName
	 * of the subscription
	 * @param eventName name of the event
	 * @param data event payload
	 */
	void publish(String eventName, Object data);
	
	/**
	 * @return ID of the subscriber
	 */
	String getId();
}
