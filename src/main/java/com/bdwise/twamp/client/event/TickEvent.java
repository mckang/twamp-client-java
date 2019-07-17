package com.bdwise.twamp.client.event;

import java.net.InetAddress;

import org.springframework.context.ApplicationEvent;

public class TickEvent extends ApplicationEvent {

	private int port = 0;
	private InetAddress address;

	public TickEvent(Object source, InetAddress address, int port) {
		super(source);
		this.address = address;
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public InetAddress getAddress() {
		return address;
	}

}
