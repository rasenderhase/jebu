package de.nikem.jebu.impl.websocket.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.api.Subscriber;
import de.nikem.jebu.impl.EventBusImpl;

/**
 * WebSocket implementation for jebu.<br>
 * exchanged messages
 * 
 * @author uawet0ju
 *
 */
@ServerEndpoint(value = "/{path}/")
public class JebuServerEndpoint {
	public final static String PATH_EVENTBUS = "eventbus";
	public final static String PATH_MANAGER = "manager";

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private EventBusImpl jebu = null;
	private Collection<Session> managerSessions = null;

	@SuppressWarnings("unchecked")
	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("path") String path) {
		log.debug("connect on {} {{}} from {}", this, path, session.getId());
		jebu = (EventBusImpl) config.getUserProperties().get("jebu");
		managerSessions = (Collection<Session>) config.getUserProperties().get("managerSessions");
		
		if (PATH_MANAGER.equals(path)) {
			getManagerSessions().add(session);
		}
		publishManagers();
	}

	@OnMessage
	public void onMessage(byte[] message, Session session, @PathParam("path") String path) {
		log.trace("message from {}", session.getId());
		if (PATH_EVENTBUS.equals(path)) {
			onEventbusMessage(message, session);
		} else if (PATH_MANAGER.equals(path)) {
			log.debug("cannot handle manager message");
		}
	}

	private void onEventbusMessage(byte[] message, Session session) {
		try (InputStream is = new ByteArrayInputStream(message); ObjectInputStream ois = new ObjectInputStream(is);) {
			JebuWebsocketEvent event = (JebuWebsocketEvent) ois.readObject();
			log.trace("event from {}: {}", session.getId(), event);
			EventBus jebu = getJebu();
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
			log.error("message processing error", e);
		}
		publishManagers();
	}

	@OnMessage
	public void onMessage(String message, Session session, @PathParam("path") String path) {
		log.debug("String message: {}", message);
		//System.out.println("String-Nachricht: " + new String(Base64.getDecoder().decode(message)));
		if (PATH_MANAGER.equals(path)) {
			log.debug("cannot handle manager message");
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason, @PathParam("path") String path) {
		log.debug("Socket Closed: {}", reason);
		if (PATH_MANAGER.equals(path)) {
			getManagerSessions().remove(session);
		}
	}

	@OnError
	public void onError(Throwable cause, @PathParam("path") String path) {
		log.error("websocket error", System.err);
	}

	private void publishManagers() {
		Writer w = new StringWriter();
		try {
			w.append("{");
			quote("subscriberMap", w).append(": {");
			boolean firstEvt = true;
			for (Map.Entry<String, Collection<Subscriber>> entry : getJebu().getSubscriberMap().entrySet()) {
				if (!firstEvt) {
					w.append(',');
				}
				firstEvt = false;
				quote(entry.getKey(), w).append(": [");
				boolean firstSub = true;
				for (Subscriber s : entry.getValue()) {
					if (!firstSub) {
						w.append(',');
					}
					firstSub = false;
					quote(s.getId(), w);
				}
				w.append(']');
			}
			w.append('}');
			w.append('}');
			
			for (Session session : getManagerSessions()) {
				session.getAsyncRemote().sendText(w.toString());
			}
		} catch (IOException e) {
			log.debug("JSON wirte error", e);
		}
	}

	protected Collection<Session> getManagerSessions() {
		return managerSessions;
	}

	protected EventBusImpl getJebu() {
		return jebu;
	}

	public static Writer quote(String string, Writer w) throws IOException {
		if (string == null || string.length() == 0) {
			w.write("\"\"");
			return w;
		}

		char b;
		char c = 0;
		String hhhh;
		int i;
		int len = string.length();

		w.write('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				w.write('\\');
				w.write(c);
				break;
			case '/':
				if (b == '<') {
					w.write('\\');
				}
				w.write(c);
				break;
			case '\b':
				w.write("\\b");
				break;
			case '\t':
				w.write("\\t");
				break;
			case '\n':
				w.write("\\n");
				break;
			case '\f':
				w.write("\\f");
				break;
			case '\r':
				w.write("\\r");
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
						|| (c >= '\u2000' && c < '\u2100')) {
					w.write("\\u");
					hhhh = Integer.toHexString(c);
					w.write("0000", 0, 4 - hhhh.length());
					w.write(hhhh);
				} else {
					w.write(c);
				}
			}
		}
		w.write('"');
		return w;
	}
}
