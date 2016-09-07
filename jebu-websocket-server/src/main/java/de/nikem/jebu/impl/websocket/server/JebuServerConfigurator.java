package de.nikem.jebu.impl.websocket.server;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class JebuServerConfigurator extends Configurator {

	private static final Map<Class<?>, Object> endpointCache = new HashMap<Class<?>, Object>();
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		Object instance = endpointCache.get(endpointClass);
		if (instance == null) {
			synchronized (endpointCache) {
				instance = endpointCache.get(endpointClass);
				if (instance == null) {
					instance =  super.getEndpointInstance(endpointClass);
					endpointCache.put(endpointClass, instance);
				}
			}
		}
		return (T) instance;
	}
}
