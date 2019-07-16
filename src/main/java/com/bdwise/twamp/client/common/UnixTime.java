package com.bdwise.twamp.client.common;

import com.bdwise.twamp.client.handshake.InboundMessage;
import com.bdwise.twamp.client.handshake.OutboundMessage;

import io.netty.buffer.ByteBuf;

public class UnixTime implements InboundMessage, OutboundMessage {
	private static final long BASE_TIME_OFFSET = 2208988800l;
	private static final double FLOAT_DENOM = 4294.967296;
			
	private long seconds;
	private long fraction;

	public long getSeconds() {
		return seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	public long getFraction() {
		return fraction;
	}

	public void setFraction(long fraction) {
		this.fraction = fraction;
	}

	public long toTimeValue() {
		return seconds << 32 | fraction;
	}
	
	@Override
	public String toString() {
		return "UnixTime [seconds=" + seconds + ", fraction=" + fraction + "]";
	}

	@Override
	public int getSize() {
		return 8;
	}
	

	@Override
	public void writeTo(ByteBuf bytebuf) {
//		System.out.println("[W]" + seconds + ":" +fraction);
		bytebuf.writeBytes(MessageUtil.fromUnsignedInt(seconds));
		bytebuf.writeBytes(MessageUtil.fromUnsignedInt(fraction));
	}

	@Override
	public void readFrom(ByteBuf bytebuf) {
		seconds = bytebuf.readUnsignedInt();
		fraction = bytebuf.readUnsignedInt();
//		System.out.println("[R]" + seconds + ":" +fraction);
	}

}
