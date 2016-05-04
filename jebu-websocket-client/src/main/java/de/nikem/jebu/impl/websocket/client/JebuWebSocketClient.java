package de.nikem.jebu.impl.websocket.client;

import static de.nikem.jebu.util.Closer.close;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
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
import de.nikem.jebu.util.function.Function;

public class JebuWebSocketClient implements EventBus {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final URI uri;
	private final EventBusImpl clientJebu;
	private Session session = null;
	private int reconnectMs = 3000;
	
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
			final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

			JebuClientEndpoint client = new JebuClientEndpoint(clientJebu, new Function<JebuClientEndpoint, Boolean>() {
				
				@Override
				public Boolean apply(JebuClientEndpoint endpoint) {

					boolean connected = false;
					try {
						session = container.connectToServer(endpoint, uri);
						
						//register all of my events
						for (String eventName : clientJebu.getSubscriberMap().keySet()) {
							JebuWebsocketEvent data = new JebuWebsocketEvent(eventName, Action.subscribe, null);
							sendData(data);
						}
						connected = true;
						
					} catch (DeploymentException e) {
						logAndSleep(e);
					} catch (IOException e) {
						logAndSleep(e);
					}
					return connected;
				
				}

				/**
				 * @param e
				 */
				protected void logAndSleep(Exception e) {
					log.error("cannot establish connection to websocket server {}. Retry after {} ms.", e, getReconnectMs());
					
					try {
						Thread.sleep(getReconnectMs());
					} catch (Exception e1) {
						throw new JebuException(e1);
					}
				}
			});
			
			client.connect();
		}
		return session;
	}
	
	/**
	 * @param data
	 */
	private void sendData(Object data) {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream out = null;
		
		Session session = getSession();
		if (session != null) {
			try {
				bos = new ByteArrayOutputStream(); 
				out = new ObjectOutputStream(bos);
				out.writeObject(data);
				out.flush();
				ByteBuffer buf = ByteBuffer.wrap(bos.toByteArray());
				session.getAsyncRemote().sendBinary(buf);
			} catch (IOException e) {
				log.debug("error during communication of session {}", session.getId());
				throw new JebuRemoveSubscriberException(e);
			} finally {
				close(bos);
				close(out);
			}
		} else {
			log.debug("Cannot send data. No connection.");
		}
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	protected int getReconnectMs() {
		return reconnectMs;
	}

	public void setReconnectMs(int reconnectMs) {
		this.reconnectMs = reconnectMs;
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		final Logger log = LoggerFactory.getLogger(JebuWebSocketClient.class);
		
		URI serverUri = new URI(args[0]);
		JebuWebSocketClient client = new JebuWebSocketClient(serverUri);
		client.subscribe("test.event.1", new Subscriber() {
			
			@Override
			public void publish(String eventName, Object data) {
				log.debug("event {} received with data {}", eventName, data);
			}
			
			@Override
			public String getId() {
				return "subscriber1";
			}
		});
		System.out.println("press any key to stop client...");
		System.in.read();
	}
}
