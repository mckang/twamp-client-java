package com.bdwise.twamp.client.event;

import org.springframework.context.ApplicationEvent;

public class ControlSessionClosedEvent extends ApplicationEvent{

	public ControlSessionClosedEvent(Object source) {
		super(source);
	}

}
