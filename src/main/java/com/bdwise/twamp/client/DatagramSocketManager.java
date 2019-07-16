package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import com.bdwise.twamp.client.event.DatagramSessionClosedEvent;
import com.bdwise.twamp.client.event.ReceivePacketStopEvent;
import com.bdwise.twamp.client.event.TestPacketStopEvent;

public class DatagramSocketManager {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
	private DatagramSocket datagramSocket;
	private boolean isTestStopped = false;
	private boolean isReceiveStopped = false;
	
	public DatagramSocketManager() throws SocketException {
		datagramSocket = new DatagramSocket(10723);
	}
	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}
	
	
	@EventListener
	public void  handleTestStopEvent(TestPacketStopEvent testPacketStopEvent) throws IOException {
		logger.info("handleTestStopEvent : preparing stopping udp daemon........");
		isTestStopped = true;
		doStop();
	}
	
	@EventListener
	public void handleReceiveStopEvent(ReceivePacketStopEvent receivePacketStopEvent) throws IOException {
		logger.info("handleReceiveStopEvent : preparing stopping udp daemon........");
		isReceiveStopped = true;
		doStop();
	}
	
	private synchronized void doStop() {
		if(isTestStopped && isReceiveStopped) {
			logger.info("stopping udp daemon........");
			datagramSocket.close();
			applicationEventPublisher.publishEvent(new DatagramSessionClosedEvent(this));

		}
	}
}
