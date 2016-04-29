package de.nikem.jebu.impl.websocket.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.websocket.Session;

import de.nikem.jebu.impl.EventBusImpl;

/**
 * Server context singleton object that holds the server jebu implementation
 * and the connected websocket sessions
 * @author uawet0ju
 *
 */
public class JebuServerContext {
	public static final String JEBU_SERVER_CONTEXT = "jebuServerContext";

	private final EventBusImpl jebu = new EventBusImpl();
	private final Collection<Session> managerSessions = Collections.synchronizedCollection(new HashSet<Session>());
	
	public EventBusImpl getJebu() {
		return jebu;
	}

	public Collection<Session> getManagerSessions() {
		return managerSessions;
	}

}
