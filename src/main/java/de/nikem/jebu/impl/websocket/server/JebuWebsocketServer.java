package de.nikem.jebu.impl.websocket.server;

import java.net.URL;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.JebuException;

/**
 * Simple Jetty Server to start WebSocket interface of <i>jebu</i> event bus.
 * 
 * @author andreas
 * @see https://github.com/jetty-project/embedded-jetty-websocket-examples/blob/
 *      master/javax.websocket-example/src/main/java/org/eclipse/jetty/demo/
 *      EventServer.java
 */
public class JebuWebsocketServer {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private int port;

	public void startServer() {
		try {
			final Server server = new Server(port);

			ContextHandlerCollection collection = new ContextHandlerCollection();
			server.setHandler(collection);

			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/jebu");
			collection.addHandler(context);

			// Initialize javax.websocket layer
			ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
			
			// Add WebSocket endpoint to javax.websocket layer
			wscontainer.addEndpoint(JebuServerEndpoint.class);

			final URL warUrl = JebuWebsocketServer.class.getClassLoader().getResource("de/nikem/jebu/site");
			final String warUrlString = warUrl.toExternalForm();
			log.debug("doc root: " + warUrlString);
			collection.addHandler(new WebAppContext(warUrlString, "/"));

			server.start();
			log.info("Server started on port {}", port);
			server.join();
		} catch (Exception e) {
			throw new JebuException(e);
		}
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Startet den Server auf Port 8080
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		JebuWebsocketServer ws = new JebuWebsocketServer();
		ws.setPort(8080);
		ws.startServer();
	}
}
