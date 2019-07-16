package com.bdwise.twamp.client.event;

import org.springframework.context.ApplicationEvent;

public class DatagramSessionClosedEvent extends ApplicationEvent{

	public DatagramSessionClosedEvent(Object source) {
		super(source);
	}

}
