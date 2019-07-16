package com.bdwise.twamp.client.handshake;

import io.netty.buffer.ByteBuf;

public interface OutboundMessage extends Message{
	public abstract void writeTo(ByteBuf bytebuf);
}
