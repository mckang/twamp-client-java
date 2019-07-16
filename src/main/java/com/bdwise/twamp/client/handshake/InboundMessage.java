package com.bdwise.twamp.client.handshake;

import io.netty.buffer.ByteBuf;

public interface InboundMessage extends Message {
	public abstract void readFrom(ByteBuf bytebuf);
}
