package com.bdwise.twamp.client.handshake;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeStartSession implements InboundProcessor<HandshakeStartSession.StartAck> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public StartAck read(ChannelHandlerContext ctx, ByteBuf in) {
		StartAck startAck = new StartAck();
		ByteBuf readBytes = in.readBytes(startAck.getSize());
		startAck.readFrom(readBytes);
		logger.debug("Client received: " + startAck);
		return startAck;
	}

	public static class StartAck implements InboundMessage {
		private byte accept;
		private byte[] mbz = new byte[15];
		private byte[] hwmac = new byte[16];
		
		
		public byte getAccept() {
			return accept;
		}

		public byte[] getMbz() {
			return mbz;
		}

		public byte[] getHwmac() {
			return hwmac;
		}

		@Override
		public String toString() {
			return "StartAck [accept=" + accept + ", mbz=" + Arrays.toString(mbz) + ", hwmac=" + Arrays.toString(hwmac)
					+ "]";
		}

		@Override
		public void readFrom(ByteBuf bytebuf) {
			accept = bytebuf.readByte();
			bytebuf.readBytes(mbz);
			bytebuf.readBytes(hwmac);
		}

		@Override
		public int getSize() {
			return 1 + mbz.length + hwmac.length;
		}

	}

}
