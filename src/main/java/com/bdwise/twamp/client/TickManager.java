package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;

import com.bdwise.twamp.client.event.ServerInfoReceivedEvent;
import com.bdwise.twamp.client.event.TickEvent;

public class TickManager implements SmartLifecycle{
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Timer ticker;
	
	@Value("${twamp.test.delayms}")
	private int sleepTime = 0;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private int packetCount;

	public TickManager(int packetCount) {
		ticker = new Timer();
		this.packetCount = packetCount;
	}

	private class EventGenetator extends TimerTask {
		private InetAddress address;
		private int port;
		private EventGenetator(InetAddress address, int port) {
			this.address = address;
			this.port = port;
		}
		public void run() {
			if(packetCount <= 0) {
				stop();
				return;
			}
//			System.out.println("+++++++++++");
			applicationEventPublisher.publishEvent(new TickEvent(TickManager.this, address, port));
			packetCount--;
		}
	}

	@EventListener
	public void handleServerInfoReceivedEvent(ServerInfoReceivedEvent serverInfoReceivedEvent) throws IOException {
//		System.out.println("handleServerInfoReceivedEvent+++++++++++");

		ticker.scheduleAtFixedRate(new EventGenetator(serverInfoReceivedEvent.getAddress(), serverInfoReceivedEvent.getPort()), 100, sleepTime);
	}

	private volatile boolean running = false;
	
	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		logger.info("Shutting down tickmanager");
		ticker.cancel();
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
