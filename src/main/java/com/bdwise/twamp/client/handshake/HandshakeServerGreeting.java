package com.bdwise.twamp.client.handshake;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdwise.twamp.client.common.MessageUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeServerGreeting implements InboundProcessor<HandshakeServerGreeting.ServerGreeting>, OutboundProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf in) {
		SetupResponse setupResponse = new SetupResponse();
		setupResponse.setMode(1);
		ByteBuf msg = Unpooled.buffer(setupResponse.getSize(), setupResponse.getSize());
		setupResponse.writeTo(msg);
		logger.debug("Client sending: " + setupResponse);
		ctx.writeAndFlush(msg);
	}

	@Override
	public HandshakeServerGreeting.ServerGreeting read(ChannelHandlerContext ctx, ByteBuf in) {
		ServerGreeting serverGreeting = new ServerGreeting();
		ByteBuf readBytes = in.readBytes(serverGreeting.getSize());
		serverGreeting.readFrom(readBytes);
		logger.debug("Client received: " + serverGreeting);
		return null;
	}

	public static class ServerGreeting implements InboundMessage{
		private byte[] unused = new byte[12];
		private long modes;
		private byte[] challenge = new byte[16];
		private byte[] salt = new byte[16];
		private long count;
		private byte[] mbz = new byte[12];
		
		
		
		
		public byte[] getUnused() {
			return unused;
		}
		public long getModes() {
			return modes;
		}
		public byte[] getChallenge() {
			return challenge;
		}
		public byte[] getSalt() {
			return salt;
		}
		public long getCount() {
			return count;
		}
		public byte[] getMbz() {
			return mbz;
		}
		@Override
		public String toString() {
			return "ServerGreeting [modes=" + modes + ", challenge=" + new String(challenge) + ", salt="
					+ new String(salt) + ", count=" + count + ", mbz=" + new String(mbz) + "]";
		}
		public int getSize() {
			return 12 + 4 + 16 + 16 + 4 + 12;
		}
		
		public void readFrom(ByteBuf bytebuf) {
			bytebuf.readBytes(unused);
			modes = bytebuf.readUnsignedInt();
			bytebuf.readBytes(challenge);
			bytebuf.readBytes(salt);
			count = bytebuf.readUnsignedInt();
			bytebuf.readBytes(mbz);
		}
		
	}
	
	public static class SetupResponse implements OutboundMessage{
		private long mode;
		private byte[] keyId = new byte[80];
		private byte[] token = new byte[64];
		private byte[] clientIv = new byte[16];
		
		
		
		public void setMode(long mode) {
			this.mode = mode;
		}

		public void setKeyId(byte[] keyId) {
			this.keyId = keyId;
		}

		public void setToken(byte[] token) {
			this.token = token;
		}

		public void setClientIv(byte[] clientIv) {
			this.clientIv = clientIv;
		}

		@Override
		public String toString() {
			return "SetupResponse [mode=" + mode + ", keyid=" + new String(keyId) + ", token=" + Arrays.toString(token)
					+ ", client_iv=" + new String(clientIv) + "]";
		}
		
		@Override
		public void writeTo(ByteBuf bytebuf) {
			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(mode));
			bytebuf.writeBytes(keyId);
			bytebuf.writeBytes(token);
			bytebuf.writeBytes(clientIv);
		}

		@Override
		public int getSize() {
			return 4 + keyId.length + token.length + clientIv.length;
		}
	}
	
}
