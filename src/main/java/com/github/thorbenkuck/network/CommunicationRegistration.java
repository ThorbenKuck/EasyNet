package com.github.thorbenkuck.network;

import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.datatypes.TriConsumer;

import java.util.HashMap;
import java.util.Map;

public class CommunicationRegistration {

	private final Map<Class, TriConsumer> mapping = new HashMap<>();
	private TriConsumer<Connection, Session, Object> defaultHandler = (c, s, o) -> {
		throw new IllegalStateException("Nothing registered for " + o);
	};

	public <T> void register(Class<T> type, TriConsumer<Connection, Session, T> connectionSessionTTriConsumer) {
		mapping.put(type, connectionSessionTTriConsumer);
	}

	public void setDefaultHandler(TriConsumer<Connection, Session, Object> defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	public <T> void trigger(T t, Connection connection, Session session) {
		TriConsumer<Connection, Session, T> consumer = mapping.get(t.getClass());
		if (consumer != null) {
			consumer.accept(connection, session, t);
		} else {
			defaultHandler.accept(connection, session, t);
		}
	}
}
