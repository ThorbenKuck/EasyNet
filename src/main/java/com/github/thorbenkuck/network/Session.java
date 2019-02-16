package com.github.thorbenkuck.network;

public interface Session {
	void send(Object o);

	boolean isIdentified();

	void setIdentified(boolean identified);

	String getIdentifier();

	void setIdentifier(String identifier);
}
