package de.nikem.jebu.impl.websocket.server;

import javax.websocket.Session;

public class JebuWebSocketJsonSubscriber extends JebuWebSocketSubscriber {

	public JebuWebSocketJsonSubscriber(Session session) {
		super(session);
	}

	@Override
	protected void sendData(Object data) {
		String json = JsonUtils.populateMap(data).toString();
		getSession().getAsyncRemote().sendText(json);
	}
}
