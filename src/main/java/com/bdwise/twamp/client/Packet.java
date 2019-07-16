package com.bdwise.twamp.client;

import com.bdwise.twamp.client.common.MessageUtil;
import com.bdwise.twamp.client.common.UnixTime;

public class Packet {
	private long sendTimestamp = -1;
	private long recvTimestamp = -1;
	private UnixTime sendUnixTimestamp ;
	
	public Packet(){
		sendTimestamp = System.nanoTime();
		sendUnixTimestamp = MessageUtil.getUnixTime();
	}
	
	public Packet received() {
		recvTimestamp = System.nanoTime();
		return this;
	}

	
	public UnixTime getSendUnixTimestamp() {
		return sendUnixTimestamp;
	}

	long getSendTimestamp() {
		return sendTimestamp;
	}
	
	double getDelay(double serverProcessingTime) {
		return (recvTimestamp - sendTimestamp) / 1_000_000.0 - serverProcessingTime;
	}

	@Override
	public String toString() {
		return "Packet [delay] " + (recvTimestamp - sendTimestamp) / 1_000_000.0 ;
	}
}
