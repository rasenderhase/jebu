package de.nikem.jebu.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.api.Subscriber;

public class EventBusImpl implements EventBus {

	private final Map<String, Collection<Subscriber>> subscriberMap = Collections.synchronizedMap(new HashMap<String, Collection<Subscriber>>());

	@Override
	public void publish(String eventName, Object data) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		synchronized (subscriberCollection) {
			for (Subscriber subscriber : subscriberCollection) {
				subscriber.publish(eventName, data);
			}
		}
	}

	@Override
	public void subscribe(String eventName, Subscriber subscriber) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection == null) {
			subscriberCollection = Collections.synchronizedCollection(new LinkedHashSet<Subscriber>());
			getSubscriberMap().put(eventName, subscriberCollection);
		}
		subscriberCollection.add(subscriber);
	}

	@Override
	public void unsubscribe(String eventName, Subscriber subscriber) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection != null) {
			subscriberCollection.remove(subscriber);
		}
	}

	@Override
	public void unsubscribe(Subscriber subscriber) {
		Map<String, Collection<Subscriber>> subscriberMap = getSubscriberMap();
		synchronized (subscriberMap) {
			for (Collection<Subscriber> subscriberCollection : subscriberMap.values()) {
				subscriberCollection.remove(subscriber);
			}
		}
	}

	protected Map<String, Collection<Subscriber>> getSubscriberMap() {
		return subscriberMap;
	}
}
