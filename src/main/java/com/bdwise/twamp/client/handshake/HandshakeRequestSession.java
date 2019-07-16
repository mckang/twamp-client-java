package com.bdwise.twamp.client.handshake;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeRequestSession implements InboundProcessor<HandshakeRequestSession.AcceptSession>, OutboundProcessor{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf in) {
		StartSession startSession = new StartSession();
		startSession.setTwo((byte) 2);
		ByteBuf msg = Unpooled.buffer(startSession.getSize(), startSession.getSize());
		startSession.writeTo(msg);
		ctx.writeAndFlush(msg);
	}

	@Override
	public AcceptSession read(ChannelHandlerContext ctx, ByteBuf in) {
		AcceptSession acceptSession = new AcceptSession();
		ByteBuf readBytes = in.readBytes(acceptSession.getSize());
		acceptSession.readFrom(readBytes);
		logger.debug("Client received: " + acceptSession);
		return acceptSession;
	}
	
	
	public static class AcceptSession implements InboundMessage{

		private byte accept;
		private byte mbz1;
		private int port;
		private byte[] sid = new byte[16];
		private byte[] mbz2 = new byte[12];
		private byte[] hwmac = new byte[16];
		
		
		public byte getAccept() {
			return accept;
		}

		public byte getMbz1() {
			return mbz1;
		}

		public int getPort() {
			return port;
		}

		public byte[] getSid() {
			return sid;
		}

		public byte[] getMbz2() {
			return mbz2;
		}

		public byte[] getHwmac() {
			return hwmac;
		}

		@Override
		public String toString() {
			return "AcceptSession [accept=" + accept + ", mbz=" + mbz1 + ", port=" + port + ", sid=" + Arrays.toString(sid)
					+ ", mbz_=" + Arrays.toString(mbz2) + ", hwmac=" + Arrays.toString(hwmac) + "]";
		}

		@Override
		public void readFrom(ByteBuf bytebuf) {
			accept = bytebuf.readByte();
			mbz1 = bytebuf.readByte();
			port = bytebuf.readUnsignedShort();
			bytebuf.readBytes(sid);
			bytebuf.readBytes(mbz2);
			bytebuf.readBytes(hwmac);
		}

		@Override
		public int getSize() {
			return 1 + 1 + 2 + sid.length + mbz2.length 
			+ hwmac.length;
		}
		
	}
	
	public static class StartSession implements OutboundMessage{
		private byte two =2;
		private byte[] mbz = new byte[15];
		private byte[] hwmac = new byte[16];
		
		
		
		public void setTwo(byte two) {
			this.two = two;
		}

		public void setMbz(byte[] mbz) {
			this.mbz = mbz;
		}

		public void setHwmac(byte[] hwmac) {
			this.hwmac = hwmac;
		}

		@Override
		public String toString() {
			return "StartSession [two=" + two + ", mbz=" + Arrays.toString(mbz) + ", hwmac=" + Arrays.toString(hwmac) + "]";
		}

		@Override
		public void writeTo(ByteBuf bytebuf) {
			bytebuf.writeByte(two);
			bytebuf.writeBytes(mbz);
			bytebuf.writeBytes(hwmac);
		}

		@Override
		public int getSize() {
			return 1 + mbz.length + hwmac.length;
		}
	}

}
