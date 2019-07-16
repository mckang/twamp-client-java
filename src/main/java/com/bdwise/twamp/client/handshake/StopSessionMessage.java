package com.bdwise.twamp.client.handshake;

import java.util.Arrays;

import com.bdwise.twamp.client.common.MessageUtil;

import io.netty.buffer.ByteBuf;

public class StopSessionMessage implements OutboundMessage {

	private byte three;
	private byte accept;
	private byte[] mbz1 = new byte[2];
	private long numberOfSessions;
	private byte[] mbz2 = new byte[8];
	private byte[] hwmac = new byte[16];
	
	
	public void setThree(byte three) {
		this.three = three;
	}

	public void setAccept(byte accept) {
		this.accept = accept;
	}

	public void setMbz1(byte[] mbz1) {
		this.mbz1 = mbz1;
	}

	public void setNumberOfSessions(long numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
	}

	public void setMbz2(byte[] mbz2) {
		this.mbz2 = mbz2;
	}

	public void setHwmac(byte[] hwmac) {
		this.hwmac = hwmac;
	}

	@Override
	public void writeTo(ByteBuf bytebuf) {
		bytebuf.writeByte(three);
		bytebuf.writeByte(accept);
		bytebuf.writeBytes(mbz1);
		bytebuf.writeBytes(MessageUtil.fromUnsignedInt(numberOfSessions));
		bytebuf.writeBytes(mbz2);
		bytebuf.writeBytes(hwmac);
	}

	@Override
	public int getSize() {
		return 1 + 1 + 2 + 4 + 8 + 16;
	}	
	
	@Override
	public String toString() {
		return "StopSession [three=" + three + ", accept=" + accept + ", mbz=" + Arrays.toString(mbz1)
				+ ", number_of_sessions=" + numberOfSessions + ", mbz_=" + Arrays.toString(mbz2) + ", hwmac="
				+ Arrays.toString(hwmac) + "]";
	}
}
