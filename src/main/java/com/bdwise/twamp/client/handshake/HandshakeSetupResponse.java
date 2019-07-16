package com.bdwise.twamp.client.handshake;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdwise.twamp.client.HandshakeProcessor;
import com.bdwise.twamp.client.common.MessageUtil;
import com.bdwise.twamp.client.common.UnixTime;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeSetupResponse implements InboundProcessor<HandshakeSetupResponse.ServerStart>, OutboundProcessor{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public ServerStart read(ChannelHandlerContext ctx, ByteBuf in) {
		ServerStart serverStart = new ServerStart();
		ByteBuf readBytes = in.readBytes(serverStart.getSize());
		serverStart.readFrom(readBytes);
		logger.debug("Client received: " + serverStart);
		return serverStart;
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf in) {
		RequestSession requestSession = new RequestSession();
		ByteBuf msg = Unpooled.buffer(requestSession.getSize(),requestSession.getSize());
		requestSession.getTimeout().setSeconds(5);
		requestSession.getTimeout().setFraction(0);
		requestSession.setStartTime(MessageUtil.getUnixTime());
		requestSession.setFive((byte) 5);
		requestSession.setSenderPort(10723);
		requestSession.setReceiverPort(10723);
		requestSession.setIpvn((byte) 4);
		requestSession.setPaddingLength(ctx.channel().attr(HandshakeProcessor.PACKET_PADDING_LENGTH).get());
		requestSession.setTypePDescriptor(0);

		String serverAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

		requestSession.setReceiverAddress(MessageUtil.ipToLong(serverAddress));
//		requestSession.setSenderAddress(MessageUtil.ipToLong("192.168.99.1"));

		logger.debug("Client sending: " + requestSession.getSize());
		requestSession.writeTo(msg);
		ctx.writeAndFlush(msg);
	}
	
	public static class ServerStart implements InboundMessage{

		private byte[] mbz1 = new byte[15];
		private byte accept;
		private byte[] serverIv = new byte[16];
		private UnixTime startTime = new UnixTime();
		private byte[] mbz2 = new byte[8];
		
		
		public byte[] getMbz1() {
			return mbz1;
		}
		public byte getAccept() {
			return accept;
		}
		public byte[] getServerIv() {
			return serverIv;
		}
		public UnixTime getStartTime() {
			return startTime;
		}
		public byte[] getMbz2() {
			return mbz2;
		}
		@Override
		public String toString() {
			return "ServerStart [mbz=" + new String(mbz1) + ", accept=" + accept + ", server_iv="
					+ new String(serverIv) + ", start_time=" + startTime + ", mbz_=" + new String(mbz2) + "]";
		}
		@Override
		public void readFrom(ByteBuf bytebuf) {
			bytebuf.readBytes(mbz1);
			accept = bytebuf.readByte();
			bytebuf.readBytes(serverIv);
			startTime.readFrom(bytebuf);
			bytebuf.readBytes(mbz2);
		}

		@Override
		public int getSize() {
			return mbz1.length + 1 + serverIv.length + startTime.getSize() + mbz2.length;
		}

		
	}
	
	public static class RequestSession implements OutboundMessage{
		private byte five;
		private byte ipvn;
		private byte confSender;
		private byte confReceiver;

		private long scheduleSlots;
		private long packets;

		private int senderPort;
		private int receiverPort;

		private long senderAddress ;
		private long receiverAddress;

		private byte[] sid = new byte[16];

		private long paddingLength;

		private UnixTime startTime = new UnixTime();
		private UnixTime timeout = new UnixTime();

		private long typePDescriptor;
		private byte[] mbz = new byte[8];

		private byte[] hwmac = new byte[16];

		
		
		public UnixTime getStartTime() {
			return startTime;
		}

		public UnixTime getTimeout() {
			return timeout;
		}

		public void setFive(byte five) {
			this.five = five;
		}

		public void setIpvn(byte ipvn) {
			this.ipvn = ipvn;
		}

		public void setConfSender(byte confSender) {
			this.confSender = confSender;
		}

		public void setConfReceiver(byte confReceiver) {
			this.confReceiver = confReceiver;
		}

		public void setScheduleSlots(long scheduleSlots) {
			this.scheduleSlots = scheduleSlots;
		}

		public void setPackets(long packets) {
			this.packets = packets;
		}

		public void setSenderPort(int senderPort) {
			this.senderPort = senderPort;
		}

		public void setReceiverPort(int receiverPort) {
			this.receiverPort = receiverPort;
		}

		public void setSenderAddress(long senderAddress) {
			this.senderAddress = senderAddress;
		}

		public void setReceiverAddress(long receiverAddress) {
			this.receiverAddress = receiverAddress;
		}

		public void setSid(byte[] sid) {
			this.sid = sid;
		}

		public void setPaddingLength(long paddingLength) {
			this.paddingLength = paddingLength;
		}

		public void setStartTime(UnixTime startTime) {
			this.startTime = startTime;
		}

		public void setTimeout(UnixTime timeout) {
			this.timeout = timeout;
		}

		public void setTypePDescriptor(long typePDescriptor) {
			this.typePDescriptor = typePDescriptor;
		}

		public void setMbz(byte[] mbz) {
			this.mbz = mbz;
		}

		public void setHwmac(byte[] hwmac) {
			this.hwmac = hwmac;
		}

		@Override
		public String toString() {
			return "RequestSession [five=" + five + ", ipvn=" + ipvn + ", mbz=" + null + ", conf_sender=" + confSender
					+ ", conf_receiver=" + confReceiver + ", schedule_slots=" + scheduleSlots + ", packets=" + packets
					+ ", sender_port=" + senderPort + ", receiver_port=" + receiverPort + ", sender_address="
					+ senderAddress + ", receiver_address=" + receiverAddress + ", sid=" + Arrays.toString(sid)
					+ ", padding_length=" + paddingLength + ", start_time=" + startTime + ", timeout=" + timeout
					+ ", type_p_descriptor=" + typePDescriptor + ", mbz_=" + Arrays.toString(mbz) + ", hwmac="
					+ Arrays.toString(hwmac) + "]";
		}
		
		@Override
		public void writeTo(ByteBuf bytebuf) {
			bytebuf.writeByte(five);
			bytebuf.writeByte(ipvn);
			bytebuf.writeByte(confSender);
			bytebuf.writeByte(confReceiver);

			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(scheduleSlots));
			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(packets));

			bytebuf.writeBytes(MessageUtil.fromUnsignedShort(senderPort));
			bytebuf.writeBytes(MessageUtil.fromUnsignedShort(receiverPort));

			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(senderAddress));
			bytebuf.writeZero(12);
			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(receiverAddress));
			bytebuf.writeZero(12);
			

			bytebuf.writeBytes(sid);

			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(paddingLength));
			startTime.writeTo(bytebuf);
			timeout.writeTo(bytebuf);

			bytebuf.writeBytes(MessageUtil.fromUnsignedInt(typePDescriptor));

			bytebuf.writeBytes(mbz);
			bytebuf.writeBytes(hwmac);

		}

		@Override
		public int getSize() {
			return 1 + 1 + 0 + 1 + 1 + 4 + 4 + 2 + 2 + (4*4) + (4*4) + sid.length + 4 + startTime.getSize() + timeout.getSize() + 4
					+ mbz.length + hwmac.length;
		}

		
	}



}
