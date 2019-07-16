package com.bdwise.twamp.client.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface InboundProcessor<T extends InboundMessage> {

	public T read(ChannelHandlerContext ctx, ByteBuf in);
}
