package de.nikem.jebu.impl.websocket.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.impl.websocket.server.JebuWebsocketEvent;

@ClientEndpoint
public class JebuClientEndpoint {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final EventBus jebu;
	
	public JebuClientEndpoint(EventBus clientJebu) {
		this.jebu = clientJebu;
	}

	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log.debug("connect to " + session.getId());
	}

	@OnMessage
	public void onMessage(byte[] message, Session session) {
		try (InputStream is = new ByteArrayInputStream(message); ObjectInputStream ois = new ObjectInputStream(is);) {
			JebuWebsocketEvent event = (JebuWebsocketEvent) ois.readObject();
			switch (event.getAction()) {
			case publish:
				jebu.publish(event.getEventName(), event.getData());
				break;
				default:
					log.debug("action " + event.getAction() + " cannot be processed by client.");
			}
			
		} catch (IOException | ClassNotFoundException e) {
			log.error("message processing error", System.err);
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		log.debug("String message: {}", message);
		//System.out.println("String-Nachricht: " + new String(Base64.getDecoder().decode(message)));
	}

	@OnClose
	public void onClose(CloseReason reason) {
		log.debug("Socket Closed: {}", reason);
	}

	@OnError
	public void onError(Throwable cause) {
		log.error("websocket error", System.err);
	}
}
