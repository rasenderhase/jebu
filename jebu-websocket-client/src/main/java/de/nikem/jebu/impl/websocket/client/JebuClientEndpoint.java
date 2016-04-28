package de.nikem.jebu.impl.websocket.client;

import static de.nikem.jebu.util.Closer.close;

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
import de.nikem.jebu.impl.websocket.JebuWebsocketEvent;
import de.nikem.jebu.util.function.Function;

@ClientEndpoint
public class JebuClientEndpoint {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final EventBus jebu;
	private final Function<JebuClientEndpoint, Boolean> connect;

	public JebuClientEndpoint(EventBus clientJebu, Function<JebuClientEndpoint, Boolean> connect) {
		this.jebu = clientJebu;
		this.connect = connect;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log.debug("connect to " + session.getId());
	}

	@OnMessage
	public void onMessage(byte[] message, Session session) {
		InputStream is = null;
		ObjectInputStream ois = null;
		try {
			is = new ByteArrayInputStream(message); 
			ois = new ObjectInputStream(is);
			JebuWebsocketEvent event = (JebuWebsocketEvent) ois.readObject();
			switch (event.getAction()) {
			case publish:
				jebu.publish(event.getEventName(), event.getData());
				break;
			default:
				log.debug("action " + event.getAction() + " cannot be processed by client.");
				break;
			}

		} catch (IOException e) {
			log.error("message processing error", e);
		} catch (ClassNotFoundException e) {
			log.error("message processing error", e);
		} finally {
			close(is);
			close(ois);
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
		connect();
	}

	@OnError
	public void onError(Throwable cause) {
		log.error("websocket error", System.err);
	}
	
	public void connect() {
		while (!connect.apply(this));
	}
}
