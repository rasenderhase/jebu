package de.nikem.jebu.impl.websocket.server.jetty;

import java.net.URL;

import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nikem.jebu.api.JebuException;
import de.nikem.jebu.impl.websocket.server.JebuServerContext;
import de.nikem.jebu.impl.websocket.server.JebuServerEndpoint;

/**
 * Simple Jetty Server to start WebSocket interface of <i>jebu</i> event bus.
 * <br><br>
 * <a href='https://github.com/jetty-project/embedded-jetty-websocket-examples/blob/master/javax.websocket-example/src/main/java/org/eclipse/jetty/demo/EventServer.java'>see also</a>
 * @author andreas
 * 
 */
public class JebuWebsocketServer {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private JebuServerContext actualContext = null;
	private int port;
	private Server server = null;
	
	public static class ServerThread extends Thread {
		private final Logger log = LoggerFactory.getLogger(getClass());
		private final Server server;
		
		public ServerThread(Server server) {
			super("Server-Thread");
			this.server = server;
		}

		@Override
		public void run() {
			try {
				server.join();
				log.info("server stopped");
			} catch (InterruptedException e) {
				log.error("server interrupted", e);
			}
		}
	}
	
	public void startServer() {
		if (server == null) {
			try {
				server = new Server(port);

				ContextHandlerCollection collection = new ContextHandlerCollection();
				server.setHandler(collection);

				ServletContextHandler context = new ServletContextHandler();
				context.setContextPath("/jebu");
				collection.addHandler(context);
				// Initialize javax.websocket layer
				ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
				// Add WebSocket endpoint to javax.websocket layer

				ServerEndpointConfig.Builder configBuilder = ServerEndpointConfig.Builder.create(JebuServerEndpoint.class, "/{path}/");
				ServerEndpointConfig config = configBuilder.build();
				if (!config.getUserProperties().containsKey(JebuServerContext.JEBU_SERVER_CONTEXT)) {
					actualContext = new JebuServerContext();
					config.getUserProperties().put(JebuServerContext.JEBU_SERVER_CONTEXT, actualContext);
				}

				wscontainer.addEndpoint(config);

				//static content
				final URL warUrl = JebuWebsocketServer.class.getClassLoader().getResource("de/nikem/jebu/site");
				final String warUrlString = warUrl.toExternalForm();
				log.debug("doc root: " + warUrlString);
				collection.addHandler(new WebAppContext(warUrlString, "/"));
				server.start();
				
				Thread t = new ServerThread(server);
				t.setDaemon(true);
				t.start();
				log.info("Server started on port {}. Got to http://localhost:{}/ to start Jebu manager", port, port);
			} catch (Exception e) {
				throw new JebuException(e);
			}
		} else {
			log.info("Server already started: {}", server.dump());
		}
	}
	
	public void stopServer() {
		if (server != null) {
			try {
				server.stop();
				server = null;
				actualContext = null;
			} catch (Exception e) {
				log.error("can't stop server", e);
			}
		} else {
			log.info("server already stopped");
		}
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Publish event directly from server jebu (no server client connection needed)
	 * @param eventName
	 * @param data
	 */
	public void publish(String eventName, Object data) {
		if (actualContext != null) {
			actualContext.getJebu().publish(eventName, data);
		} else {
			log.debug("Lost event, because no server context: {}", eventName);
		}
	}
}
