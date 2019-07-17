package com.bdwise.twamp.client.event;

import org.springframework.context.ApplicationEvent;

public class StopClientEvent extends ApplicationEvent{

	public StopClientEvent(Object source) {
		super(source);
	}

}
