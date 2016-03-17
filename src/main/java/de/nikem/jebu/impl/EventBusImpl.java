package de.nikem.jebu.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.api.Subscriber;

public class EventBusImpl implements EventBus {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, Collection<Subscriber>> subscriberMap = Collections
			.synchronizedMap(new HashMap<String, Collection<Subscriber>>());

	@Override
	public void publish(String eventName, Object data) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection != null) {
			synchronized (subscriberCollection) {
				for (Iterator<Subscriber> it = subscriberCollection.iterator(); it.hasNext();) {
					Subscriber subscriber = it.next();
					try {
						subscriber.publish(eventName, data);
					} catch (JebuRemoveSubscriberException ex) {
						log.debug("remove subscriber due to exception: " + subscriber.getId(), ex);
						it.remove();
					}
				}
			}
		}
	}

	@Override
	public void subscribe(String eventName, Subscriber subscriber) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection == null) {
			subscriberCollection = Collections.synchronizedCollection(new LinkedHashSet<>());
			getSubscriberMap().put(eventName, subscriberCollection);
		}
		subscriberCollection.add(subscriber);
	}

	@Override
	public void unsubscribe(String eventName, Subscriber subscriber) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection != null) {
			subscriberCollection.remove(subscriber);
			if (subscriberCollection.isEmpty()) {
				getSubscriberMap().remove(eventName);
			}
		}
	}

	@Override
	public void unsubscribe(Subscriber subscriber) {
		Map<String, Collection<Subscriber>> subscriberMap = getSubscriberMap();
		synchronized (subscriberMap) {
			for (Iterator<Collection<Subscriber>> it = subscriberMap.values().iterator(); it.hasNext();) {
				Collection<Subscriber> subscriberCollection = it.next();
				subscriberCollection.remove(subscriber);
				if (subscriberCollection.isEmpty()) {
					it.remove();
				}
			}
		}
	}

	public boolean hasSubscribers(String eventName) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		return subscriberCollection != null && !subscriberCollection.isEmpty();
	}

	public boolean hasSubscribers() {
		return getSubscriberMap().isEmpty();
	}

	public Map<String, Collection<Subscriber>> getSubscriberMap() {
		return subscriberMap;
	}

	@Override
	public String getId() {
		return toString();
	}
}
