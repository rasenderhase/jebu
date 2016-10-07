package de.nikem.jebu.impl.websocket.server;

import java.util.Collection;

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

	private EventBusImpl jebu;
	private Collection<Session> managerSessions;
	
	public EventBusImpl getJebu() {
		return jebu;
	}

	public Collection<Session> getManagerSessions() {
		return managerSessions;
	}

	public void setJebu(EventBusImpl jebu) {
		this.jebu = jebu;
	}

	public void setManagerSessions(Collection<Session> managerSessions) {
		this.managerSessions = managerSessions;
	}

}
