package de.nikem.jebu.impl.websocket.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
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
import de.nikem.jebu.impl.websocket.JebuWebsocketEvent;

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

	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("path") String path) {
		log.debug("connect on {} {{}} from {}", this, path, session.getId());
		
		JebuServerContext serverContext = (JebuServerContext) config.getUserProperties().get(JebuServerContext.JEBU_SERVER_CONTEXT);
		if (serverContext == null) {
			config.getUserProperties().put(JebuServerContext.JEBU_SERVER_CONTEXT, serverContext);
		}
		
		jebu = serverContext.getJebu();
		managerSessions = serverContext.getManagerSessions();
		
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
		JebuWebsocketEvent event = null;
		try (InputStream is = new ByteArrayInputStream(message); ObjectInputStream ois = new ObjectInputStream(is);) {
			event = (JebuWebsocketEvent) ois.readObject();
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
		publishManagers(event, session);
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
			publishManagers();
		} else if (PATH_EVENTBUS.equals(path)) {
			getJebu().unsubscribe(new JebuWebSocketSubscriber(session));
		}
	}

	@OnError
	public void onError(Throwable cause, @PathParam("path") String path) {
		log.error("websocket error {{}}", path);
		log.error("websocket error", cause);
	}

	private void publishManagers() {
		publishManagers(null, null);
	}
	private void publishManagers(JebuWebsocketEvent event, Session session) {
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
			w.append("},");
			quote("managerSessions", w).append(": [");
			boolean firstSub = true;
			for (Session managerSession : getManagerSessions()) {
				if (!firstSub) {
					w.append(',');
				}
				firstSub = false;
				quote(managerSession.getId(), w);
			}
			w.append(']');
			if (event != null) {
				String data = null;
				if (event.getData() != null) {
					data = event.getData().toString();
					if (data.length() > 100) {
						data = data.substring(0, 97) + "...";
					}
				}
				
				w.append(',');
				quote("event", w).append(": {");
				quote("sender", w).append(':');
				quote(session.getId(), w).append(',');
				quote("action", w).append(':');
				quote(event.getAction().toString(), w).append(',');
				quote("eventName", w).append(':');
				quote(event.getEventName(), w).append(',');
				quote("data", w).append(':');
				quote(data, w).append(',');
				quote("timestamp", w).append(':');
				quote(new Timestamp(System.currentTimeMillis()).toString(), w).append('}');
			}
			w.append('}');
			for (Session managerSession : getManagerSessions()) {
				managerSession.getAsyncRemote().sendText(w.toString());
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

	@Override
	protected void finalize() throws Throwable {
		for (Session session : getManagerSessions()) {
			session.close(new CloseReason(CloseCodes.GOING_AWAY, "good night!"));
		}
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
