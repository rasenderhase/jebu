package de.nikem.jebu.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

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
						log.trace("publish {} to: {}", eventName, subscriber.getId());
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
			log.trace("create subscriber collection for: {}", eventName);
			subscriberCollection = Collections.synchronizedCollection(new LinkedHashSet<>());
			getSubscriberMap().put(eventName, subscriberCollection);
		}
		subscriberCollection.add(subscriber);
		log.trace("subscribe for {}: {}", eventName, subscriber.getId());
	}

	@Override
	public void unsubscribe(String eventName, Subscriber subscriber) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		if (subscriberCollection != null) {
			subscriberCollection.remove(subscriber);
			if (subscriberCollection.isEmpty()) {
				log.trace("remove subscriber collection for: {}", eventName);
				getSubscriberMap().remove(eventName);
			}
		}
		log.trace("unsubscribe from {}: {}", eventName, subscriber.getId());
	}

	@Override
	public void unsubscribe(Subscriber subscriber) {
		Map<String, Collection<Subscriber>> subscriberMap = getSubscriberMap();
		synchronized (subscriberMap) {
			for (Iterator<Map.Entry<String, Collection<Subscriber>>> it = subscriberMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Collection<Subscriber>> entry = it.next();
				Collection<Subscriber> subscriberCollection = entry.getValue();
				subscriberCollection.remove(subscriber);
				if (subscriberCollection.isEmpty()) {
					log.trace("remove subscriber collection for: {}", entry.getKey());
					it.remove();
				}
			}
		}
		log.trace("unsubscribe from all events: {}", subscriber.getId());
	}

	public boolean hasSubscribers(String eventName) {
		Collection<Subscriber> subscriberCollection = getSubscriberMap().get(eventName);
		return subscriberCollection != null && !subscriberCollection.isEmpty();
	}

	public boolean hasSubscribers() {
		return !getSubscriberMap().isEmpty();
	}

	public Map<String, Collection<Subscriber>> getSubscriberMap() {
		return subscriberMap;
	}

	@Override
	public String getId() {
		return toString();
	}
}
