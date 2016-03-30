package de.nikem.jebu.impl.websocket.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.Subscriber;
import de.nikem.jebu.impl.JebuRemoveSubscriberException;

/**
 * This is the server subscriber for the jebu websocket implementation. It wraps the
 * wbsocket session. 
 * @author uawet0ju
 */
public class JebuWebSocketSubscriber implements Subscriber {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Session session;

	/**
	 * @param session
	 */
	public JebuWebSocketSubscriber(Session session) {
		super();
		this.session = session;
	}

	@Override
	public void publish(String eventName, Object data) {
		sendData(data);
	}

	public Session getSession() {
		return session;
	}
	
	@Override
	public String getId() {
		return getSession().getId();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JebuWebSocketSubscriber other = (JebuWebSocketSubscriber) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JebuWebSocketSubscriber [" + session.getId() + "]";
	}
}
