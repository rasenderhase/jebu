package de.nikem.jebu.impl.websocket;

import java.util.Base64;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/")
public class JebuWebSocket {
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Jemand hat sich verbunden!");
	}

	@OnMessage
	public void onMessage(byte[] message) {
		System.out.println("Nachricht: " + new String(message));
	}
	
	@OnMessage
	public void onMessage(String message) {
		System.out.println("String-Nachricht: " + new String(Base64.getDecoder().decode(message)));
	}

	@OnClose
	public void onClose(CloseReason reason) {
		System.out.println("Socket Closed: " + reason);
	}

	@OnError
	public void onError(Throwable cause) {
		cause.printStackTrace(System.err);
	}
}
