package com.bdwise.twamp.client;

import com.bdwise.twamp.client.handshake.ControlStatus;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public interface HandshakeProcessor {
	public final static AttributeKey<Long> SERVER_UDP_PORT = AttributeKey.valueOf("SERVER_UDP_PORT");
	public final static AttributeKey<Long> PACKET_PADDING_LENGTH = AttributeKey.valueOf("PACKET_PADDING_LENGTH");
	
	public void prepareNext(ControlStatus status, ChannelHandlerContext ctx);
}
