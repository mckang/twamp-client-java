package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import com.bdwise.twamp.client.common.MessageUtil;
import com.bdwise.twamp.client.common.UnixTime;
import com.bdwise.twamp.client.event.TestPacketStopEvent;
import com.bdwise.twamp.client.event.TickEvent;
import com.bdwise.twamp.client.handshake.OutboundMessage;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class UdpChannelManager implements SmartLifecycle{
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	protected volatile boolean running = false;

	private Channel clientChannel;
	private EventLoopGroup group;
	
	private int pPort;
	@Override
	public void start() {

        pPort = 10723;
        
		if (running) return;
		group = new NioEventLoopGroup();
		Bootstrap clientBootstrap = new Bootstrap();

	    clientBootstrap.group(group);
	    clientBootstrap.channel(NioDatagramChannel.class);
	    
	    clientBootstrap.handler(new ChannelInitializer<DatagramChannel>() {
	        protected void initChannel(DatagramChannel datagramChannel) throws Exception {
	        	datagramChannel.pipeline().addLast(reflectHandler);
	        }
	    });
	    ChannelFuture channelFuture;
		try {
			channelFuture = clientBootstrap.bind(pPort).sync();
			clientChannel = channelFuture.channel();
		    running = true;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	    
	}
	

	@Override
	public void stop() {
		if(clientChannel != null && running){
			logger.info("Shutting down client: {} on port {}","0.0.0.0",pPort);
			clientChannel.close().addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					logger.info("clientChannel closed!!!");
				}
			});
			group.shutdownGracefully(1,1,TimeUnit.SECONDS).addListener(new GenericFutureListener<Future<? super Object>>() {

				@Override
				public void operationComplete(Future<? super Object> future) throws Exception {
					logger.info("group closed!!!");
				}
			});
			running = false;
		}	
	}

	@Override
	public boolean isRunning() {
		return running;
	}
	
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
	private Map<Long, Packet> packets;
	private int paddingLength = 0;
	

	private AtomicInteger packetCount ;
	
	private AtomicLong index = new AtomicLong(0);
	
	private ReflectHandler reflectHandler;
	
	public UdpChannelManager(int paddingLength,  Map<Long, Packet> packets, int packetCount, ReflectHandler reflectHandler) {
		this.paddingLength = paddingLength;
		this.packets = packets;
		this.packetCount = new AtomicInteger(packetCount);
		this.reflectHandler = reflectHandler;
	}

	@EventListener
	@Async
	public void handleTickEvent(TickEvent tickEvent) throws IOException {
		if(packetCount.get() <=0 ) {
			return ;
		}
		TestUnauthenticatedMessage testUnauthenticated = new TestUnauthenticatedMessage();
		testUnauthenticated.setSequenceNumber(index.get());
		testUnauthenticated.setTimestamp(MessageUtil.getUnixTime());
		ByteBuf msg = Unpooled.buffer(testUnauthenticated.getSize() + paddingLength, testUnauthenticated.getSize() + paddingLength);
		testUnauthenticated.writeTo(msg);
		msg.writeBytes(new byte[paddingLength]);
		
		InetSocketAddress inetSocketAddress = new InetSocketAddress(tickEvent.getAddress(), tickEvent.getPort());
//		logger.debug(tickEvent.getAddress() + " : " + tickEvent.getPort() + " : " + index);
//		logger.debug("------>" +msg.readableBytes() + " : " + (testUnauthenticated.getSize() + paddingLength));
		
		DatagramPacket packet = new DatagramPacket(msg, inetSocketAddress);
		packets.put(index.getAndIncrement(), new Packet());
		
		clientChannel.writeAndFlush(packet);
		if(packetCount.decrementAndGet() <=0 ) {
			applicationEventPublisher.publishEvent(new TestPacketStopEvent(this));
		}
	}
	
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

}
