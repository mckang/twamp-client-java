package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import com.bdwise.twamp.client.common.MessageUtil;
import com.bdwise.twamp.client.common.UnixTime;
import com.bdwise.twamp.client.event.ServerInfoReceivedEvent;
import com.bdwise.twamp.client.event.TestPacketStartEvent;
import com.bdwise.twamp.client.event.TestPacketStopEvent;
import com.bdwise.twamp.client.handshake.OutboundMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestManager implements SmartLifecycle {
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected volatile boolean running = false;
	private DatagramSocket datagramSocket = null;
	private Map<Long, Packet> packets;
	private int paddingLength = 0;
	
	@Value("${twamp.test.count}")
	private int packetCount = 0;
	
	@Value("${twamp.test.delayms}")
	private int sleepTime = 0;
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private class TestUnauthenticatedMessage implements OutboundMessage{

    	private long sequenceNumber;
    	private UnixTime timestamp;
    	private byte s = 1;
    	private byte z = 1;
    	private byte scale = 6;
    	private byte multiplier;
    	
    	
    	public void setSequenceNumber(long sequenceNumber) {
    		this.sequenceNumber = sequenceNumber;
    	}

    	public void setTimestamp(UnixTime timestamp) {
    		this.timestamp = timestamp;
    	}

    	public void setS(byte s) {
    		this.s = s;
    	}

    	public void setZ(byte z) {
    		this.z = z;
    	}

    	public void setScale(byte scale) {
    		this.scale = scale;
    	}

    	public void setMultiplier(byte multiplier) {
    		this.multiplier = multiplier;
    	}

    	@Override
    	public String toString() {
    		return "TestUnauthenticatedMessage [sequenceNumber=" + sequenceNumber + ", timestamp=" + timestamp + ", s=" + s
    				+ ", z=" + z + ", scale=" + scale + ", multiplier=" + multiplier + "]";
    	}

    	@Override
    	public void writeTo(ByteBuf bytebuf) {
    		bytebuf.writeBytes(MessageUtil.fromUnsignedInt(sequenceNumber));
    		timestamp.writeTo(bytebuf);
    		bytebuf.writeByte(s);
    		bytebuf.writeByte(z);
//    		bytebuf.writeByte(scale);
//    		bytebuf.writeByte(multiplier);	
    	}
    	
    	@Override
    	public int getSize() {
    		return 4 + timestamp.getSize() + (1 * 2);
    	}

    }

    
	public TestManager(DatagramSocket datagramSocket, int paddingLength,  Map<Long, Packet> packets) {
		this.datagramSocket = datagramSocket;
		this.paddingLength = paddingLength;
		this.packets = packets;
	}
	

	@EventListener
	public void handleServerInfoReceivedEvent(ServerInfoReceivedEvent serverInfoReceivedEvent) throws IOException {
		sendPackets(serverInfoReceivedEvent.getAddress(), serverInfoReceivedEvent.getPort());
	}

	@Async
	private void sendPackets(InetAddress address, int port) throws IOException{
		
		DatagramPacket packet = null;
		long index = 0;
		applicationEventPublisher.publishEvent(new TestPacketStartEvent(this));
		while(index < packetCount && running) {
			TestUnauthenticatedMessage testUnauthenticated = new TestUnauthenticatedMessage();
			testUnauthenticated.setSequenceNumber(index);
			testUnauthenticated.setTimestamp(MessageUtil.getUnixTime());
			ByteBuf msg = Unpooled.buffer(testUnauthenticated.getSize() + paddingLength, testUnauthenticated.getSize() + paddingLength);
			testUnauthenticated.writeTo(msg);
			msg.writeBytes(new byte[paddingLength]);
			
			logger.debug(address + " : " + port + " : " + index);

			logger.debug("------>" +msg.readableBytes() + " : " + (testUnauthenticated.getSize() + paddingLength));
			packet = new DatagramPacket(msg.array(), testUnauthenticated.getSize() + paddingLength, address, (int) port);
			packets.put(index++, new Packet());
			
			datagramSocket.send(packet);
			sleep();
		}
		applicationEventPublisher.publishEvent(new TestPacketStopEvent(this));
	}
	
	private void sleep() {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
//		System.out.println("++++++++++++++++");
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
