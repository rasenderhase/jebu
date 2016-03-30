package de.nikem.jebu.impl.websocket.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.api.JebuException;
import de.nikem.jebu.api.Subscriber;
import de.nikem.jebu.impl.EventBusImpl;
import de.nikem.jebu.impl.JebuRemoveSubscriberException;
import de.nikem.jebu.impl.websocket.JebuWebsocketEvent;
import de.nikem.jebu.impl.websocket.JebuWebsocketEvent.Action;

public class JebuWebSocketClient implements EventBus {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final URI uri;
	private final EventBusImpl clientJebu;
	private Session session = null;
	
	public JebuWebSocketClient(URI uri) {
		this.uri = uri;
		this.clientJebu = new EventBusImpl();
	}

	@Override
	public void publish(String eventName, Object data) {
		JebuWebsocketEvent event = new JebuWebsocketEvent(eventName, Action.publish, (Serializable) data);
		sendData(event);
	}

	@Override
	public String getId() {
		return toString();
	}

	@Override
	public void subscribe(String eventName, Subscriber subscriber) {
		clientJebu.subscribe(eventName, subscriber);
		
		JebuWebsocketEvent data = new JebuWebsocketEvent(eventName, Action.subscribe, null);
		sendData(data);
	}

	@Override
	public void unsubscribe(String eventName, Subscriber subscriber) {
		clientJebu.unsubscribe(eventName, subscriber);
		
		if (!clientJebu.hasSubscribers(eventName)) {
			JebuWebsocketEvent data = new JebuWebsocketEvent(eventName, Action.unsubscribe, null);
			sendData(data);
		}
	}

	@Override
	public void unsubscribe(Subscriber subscriber) {
		clientJebu.unsubscribe(subscriber);
		
		if (!clientJebu.hasSubscribers()) {
			JebuWebsocketEvent data = new JebuWebsocketEvent(null, Action.unsubscribe, null);
			sendData(data);
		}
	}

	private Session getSession() {
		if (session == null) {
			log.info("trying to connect to {}" , uri);
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			
			try {
				session = container.connectToServer(new JebuClientEndpoint(clientJebu), uri);
			} catch (DeploymentException | IOException e) {
				log.error("cannot establish connection to websocket server", e);
				throw new JebuException(e);
			}
		}
		return session;
	}
	
	/**
	 * @param data
	 */
	private void sendData(Object data) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos);) {
			out.writeObject(data);
			out.flush();
			ByteBuffer buf = ByteBuffer.wrap(bos.toByteArray());
			getSession().getAsyncRemote().sendBinary(buf);
		} catch (IOException e) {
			log.debug("error during communication of session {}", session.getId());
			throw new JebuRemoveSubscriberException(e);
		}
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
