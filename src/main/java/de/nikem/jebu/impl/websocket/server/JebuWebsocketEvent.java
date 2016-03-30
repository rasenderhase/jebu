package de.nikem.jebu.impl.websocket.server;

import java.io.Serializable;

public class JebuWebsocketEvent implements Serializable {
	private static final long serialVersionUID = -5376971525277775618L;

	public static enum Action {
		publish, subscribe, unsubscribe;
	}
	
	private final String eventName;
	private final Action action;
	private final Serializable data;
	
	/**
	 * 
	 * @param eventName
	 * @param action
	 * @param data
	 */
	public JebuWebsocketEvent(String eventName, Action action, Serializable data) {
		super();
		this.eventName = eventName;
		this.action = action;
		this.data = data;
	}

	public String getEventName() {
		return eventName;
	}

	public Action getAction() {
		return action;
	}

	public Serializable getData() {
		return data;
	}

	@Override
	public String toString() {
		return "[" + eventName + "-" + action + ", data=" + data + "]";
	}
}
