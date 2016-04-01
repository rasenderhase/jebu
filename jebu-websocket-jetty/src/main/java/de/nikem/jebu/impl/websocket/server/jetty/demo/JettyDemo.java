package de.nikem.jebu.impl.websocket.server.jetty.demo;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.Subscriber;
import de.nikem.jebu.impl.websocket.client.JebuWebSocketClient;
import de.nikem.jebu.impl.websocket.server.jetty.JebuWebsocketServer;

/**
 * Demo application for websocket implementation of JEBU based on Jetty embedded
 * server
 * @author uawet0ju
 *
 */
public class JettyDemo {

	public static void main(String[] args) throws Exception {
		URI uri = new URI("ws://localhost:8080/jebu/eventbus/");
		
		JebuWebSocketClient client = new JebuWebSocketClient(uri);
		
		class DemoSubscriber implements Subscriber {
			private final Logger log = LoggerFactory.getLogger(getClass());
			
			@Override
			public void publish(String eventName, Object data) {
				log.debug("{} received event {}: {}", getId(), eventName, data);
			}

			@Override
			public String getId() {
				return toString();
			}
		}
		
		JebuWebsocketServer server = new JebuWebsocketServer();
		server.setPort(8080);
		server.startServer();
		
		Thread.sleep(3000);
		
		DemoSubscriber s = new DemoSubscriber();
		client.subscribe("test.event.1", s);
		Thread.sleep(3000);
		client.subscribe("test.event.2", s);
		Thread.sleep(3000);
		client.subscribe("test.event.1", new DemoSubscriber());
		Thread.sleep(3000);
		client.publish("test.event.1", "Hallo!");
		
		while (true) {
			Thread.sleep(3000);
			client.unsubscribe("test.event.2", s);
			Thread.sleep(1500);
			server.stopServer();
			client.publish("test.event.2", "Hi 2!");
			Thread.sleep(1000);
			client.subscribe("test.event.2", s);
			Thread.sleep(1000);
			server.startServer();
			client.publish("test.event.2", "Hoi 2!");
		}
	}
}
