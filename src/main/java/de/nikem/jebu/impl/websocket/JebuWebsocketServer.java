package de.nikem.jebu.impl.websocket;

import java.net.URL;

import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import de.nikem.jebu.api.EventBus;
import de.nikem.jebu.api.JebuException;
import de.nikem.jebu.impl.EventBusImpl;

/**
 * Simple Jetty Server to start WebSocket interface of <i>jebu</i> event bus.
 * 
 * @author andreas
 * @see https://github.com/jetty-project/embedded-jetty-websocket-examples/blob/
 *      master/javax.websocket-example/src/main/java/org/eclipse/jetty/demo/
 *      EventServer.java
 */
public class JebuWebsocketServer {

	private int port;
	private EventBus eventBus;

	public void startServer() {
		try {
			Server server = new Server(port);

			ContextHandlerCollection collection = new ContextHandlerCollection();
			server.setHandler(collection);

			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/jebu");
			collection.addHandler(context);

			// Initialize javax.websocket layer
			ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
			
			// Add WebSocket endpoint to javax.websocket layer
			wscontainer.addEndpoint(JebuWebSocket.class);

			final URL warUrl = JebuWebsocketServer.class.getClassLoader().getResource("de/nikem/jebu/site");
			final String warUrlString = warUrl.toExternalForm();
			System.err.println("doc root: " + warUrlString);
			collection.addHandler(new WebAppContext(warUrlString, "/"));

			server.start();
			System.err.println("Server gestartet.");
			server.join();
		} catch (Exception e) {
			throw new JebuException(e);
		}
	}

	/**
	 * Startet den Server auf Port 8080
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		JebuWebsocketServer ws = new JebuWebsocketServer();
		ws.port = 8080;
		ws.eventBus = new EventBusImpl();
		ws.startServer();
	}
}
