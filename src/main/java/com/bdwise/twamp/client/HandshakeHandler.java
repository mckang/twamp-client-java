package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import com.bdwise.twamp.client.event.ControlSessionClosedEvent;
import com.bdwise.twamp.client.event.ReceivePacketStopEvent;
import com.bdwise.twamp.client.event.ServerInfoReceivedEvent;
import com.bdwise.twamp.client.event.TestPacketStopEvent;
import com.bdwise.twamp.client.handshake.ControlStatus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HandshakeHandler extends SimpleChannelInboundHandler<ByteBuf> implements HandshakeProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
	private ControlStatus status = ControlStatus.HandshakeConnecting;

	private int paddingLength = 0;
	
	private CountDownLatch latch = new CountDownLatch(2);
	
	public HandshakeHandler(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	public void prepareNext(ControlStatus status, ChannelHandlerContext ctx) {
		this.status = status;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.channel().attr(HandshakeProcessor.PACKET_PADDING_LENGTH).set((long)paddingLength);
		status.process(ctx, null, this);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {		
		status.process(ctx, in, this);
		
		switch(status) {
		case HandshakeDone:
			long serverUdpPort = ctx.channel().attr(HandshakeProcessor.SERVER_UDP_PORT).get();
			InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
			applicationEventPublisher.publishEvent(new ServerInfoReceivedEvent(this, address, (int)serverUdpPort));
			latch.await();
			ControlStatus.StopSession.process(ctx, null, this);
			applicationEventPublisher.publishEvent(new ControlSessionClosedEvent(this));
			break;
		default:
			break;
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}
	
	@EventListener
	public void  handleTestStopEvent(TestPacketStopEvent testPacketStopEvent) throws IOException {
		logger.info("handleTestStopEvent : preparing stopping udp daemon........");
		latch.countDown();
	}
	
	@EventListener
	public void handleReceiveStopEvent(ReceivePacketStopEvent receivePacketStopEvent) throws IOException {
		logger.info("handleReceiveStopEvent : preparing stopping udp daemon........");
		latch.countDown();
	}

}

