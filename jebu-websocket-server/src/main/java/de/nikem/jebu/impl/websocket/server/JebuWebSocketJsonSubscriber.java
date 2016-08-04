package de.nikem.jebu.impl.websocket.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JebuWebSocketJsonSubscriber extends JebuWebSocketSubscriber {
	private static final Logger log = LoggerFactory.getLogger(JebuWebSocketJsonSubscriber.class);

	public JebuWebSocketJsonSubscriber(Session session) {
		super(session);
	}

	@Override
	protected void sendData(Object data) {
		String json = populateMap(data).toString();
		getSession().getAsyncRemote().sendText(json);
	}

	private static JsonObject populateMap(Object bean) {
		Class<?> klass = bean.getClass();
		JsonObjectBuilder builder = Json.createObjectBuilder();

		// If klass is a System class then set includeSuperClass to false.
		boolean includeSuperClass = klass.getClassLoader() != null;

		Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i += 1) {
			try {
				Method method = methods[i];
				if (Modifier.isPublic(method.getModifiers())) {
					String name = method.getName();
					String key = "";
					if (name.startsWith("get")) {
						if ("getClass".equals(name)
								|| "getDeclaringClass".equals(name)) {
							key = "";
						} else {
							key = name.substring(3);
						}
					} else if (name.startsWith("is")) {
						key = name.substring(2);
					}
					if (key.length() > 0
							&& Character.isUpperCase(key.charAt(0))
							&& method.getParameterTypes().length == 0) {
						if (key.length() == 1) {
							key = key.toLowerCase();
						} else if (!Character.isUpperCase(key.charAt(1))) {
							key = key.substring(0, 1).toLowerCase()
									+ key.substring(1);
						}

						Object result = method.invoke(bean, (Object[]) null);
						if (result != null) {
							add(builder, key,  wrap(result));
						}
					}
				}
			} catch (Exception ignore) {
			}
		}
		return builder.build();
	}
	
	private static void add(JsonObjectBuilder builder, String key, Object thing) {
		if (thing == null) {
			builder.addNull(key);
		} else if (thing instanceof BigInteger) {
			builder.add(key, (BigInteger) thing);
		} else if (thing instanceof BigDecimal) {
			builder.add(key, (BigDecimal) thing);
		} else if (thing instanceof Boolean) {
			builder.add(key, (Boolean) thing);
		} else if (thing instanceof Double) {
			builder.add(key, (Double) thing);
		} else if (thing instanceof Integer) {
			builder.add(key, (Integer) thing);
		} else if (thing instanceof JsonArrayBuilder) {
			builder.add(key, (JsonArrayBuilder) thing);
		} else if (thing instanceof JsonObjectBuilder) {
			builder.add(key, (JsonObjectBuilder) thing);
		} else if (thing instanceof JsonValue) {
			builder.add(key, (JsonValue) thing);
		} else if (thing instanceof Long) {
			builder.add(key, (Long) thing);
		} else if (thing instanceof String) {
			builder.add(key, (String) thing);
		} else {
			log.debug("don't know how to handle class " + thing.getClass());
			builder.add(key, thing.toString());
		}
	}
	
	private static void add(JsonArrayBuilder builder, Object thing) {
		if (thing == null) {
			builder.addNull();
		} else if (thing instanceof BigInteger) {
			builder.add((BigInteger) thing);
		} else if (thing instanceof BigDecimal) {
			builder.add((BigDecimal) thing);
		} else if (thing instanceof Boolean) {
			builder.add((Boolean) thing);
		} else if (thing instanceof Double) {
			builder.add((Double) thing);
		} else if (thing instanceof Integer) {
			builder.add((Integer) thing);
		} else if (thing instanceof JsonArrayBuilder) {
			builder.add((JsonArrayBuilder) thing);
		} else if (thing instanceof JsonObjectBuilder) {
			builder.add((JsonObjectBuilder) thing);
		} else if (thing instanceof JsonValue) {
			builder.add((JsonValue) thing);
		} else if (thing instanceof Long) {
			builder.add((Long) thing);
		} else if (thing instanceof String) {
			builder.add((String) thing);
		} else {
			log.debug("don't know how to handle class " + thing.getClass());
			builder.add(thing.toString());
		}
	}
	
	public static Object wrap(Object object) {
        try {
            if (object == null) {
                return null;
            }
            if (object instanceof JsonObjectBuilder 
            		|| object instanceof JsonArrayBuilder
                    || null == object 
                    || object instanceof JsonValue
                    || object instanceof Integer
                    || object instanceof Long || object instanceof Boolean
                    || object instanceof Double
                    || object instanceof String 
                    || object instanceof BigInteger
                    || object instanceof BigDecimal) {
                return object;
            }

            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (Object item : coll) {
                	add(arrayBuilder, wrap(item));
                }
                return arrayBuilder;
            }
            if (object.getClass().isArray()) {
            	JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (Object item : ((Object[]) object)) {
                	add(arrayBuilder, wrap(item));
                }
                return arrayBuilder;
            }
            if (object instanceof Map) {
            	JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            	for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
            		add(objectBuilder, (String) entry.getKey(), wrap(entry.getValue()));
            	}
                return objectBuilder;
            }
            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage
                    .getName() : "";
            if (objectPackageName.startsWith("java.")
                    || objectPackageName.startsWith("javax.")
                    || object.getClass().getClassLoader() == null) {
                return object.toString();
            }
            return populateMap(object);
        } catch (Exception exception) {
            return null;
        }
}
}
