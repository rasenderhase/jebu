package de.nikem.jebu.impl.websocket.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.impl.EventBusImpl;

/**
 * WebSocket implementation for jebu.<br>
 * exchanged messages
 * 
 * @author uawet0ju
 *
 */
@ServerEndpoint(value = "/")
public class JebuServerEndpoint {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private EventBus jebu = new EventBusImpl();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log.debug("connect from " + session.getId());
	}

	@OnMessage
	public void onMessage(byte[] message, Session session) {
		log.trace("message from {}: {}", session.getId());
		try (InputStream is = new ByteArrayInputStream(message); ObjectInputStream ois = new ObjectInputStream(is);) {
			JebuWebsocketEvent event = (JebuWebsocketEvent) ois.readObject();
			log.trace("event from {}: {}", session.getId(), event);
			switch (event.getAction()) {
			case subscribe:
				jebu.subscribe(event.getEventName(), new JebuWebSocketSubscriber(session));
				break;
			case unsubscribe:
				if (event.getEventName() != null) {
					jebu.unsubscribe(event.getEventName(), new JebuWebSocketSubscriber(session));
				} else {
					jebu.unsubscribe(new JebuWebSocketSubscriber(session));
				}
				break;
			case publish:
				jebu.publish(event.getEventName(), event);
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
