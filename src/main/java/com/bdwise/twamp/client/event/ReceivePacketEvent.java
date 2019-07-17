package com.bdwise.twamp.client.event;

import org.springframework.context.ApplicationEvent;

public class ReceivePacketEvent extends ApplicationEvent{

	private double delay;
	private String server;
	public ReceivePacketEvent(Object source, double delay, String server) {
		super(source);
		this.delay = delay;
		this.server = server;
	}
	public double getDelay() {
		return delay;
	}
	public String getServer() {
		return server;
	}
	
	

}
