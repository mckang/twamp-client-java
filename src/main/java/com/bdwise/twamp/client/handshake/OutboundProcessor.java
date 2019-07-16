package com.bdwise.twamp.client.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface OutboundProcessor {
	public void write(ChannelHandlerContext ctx, ByteBuf in);
}
