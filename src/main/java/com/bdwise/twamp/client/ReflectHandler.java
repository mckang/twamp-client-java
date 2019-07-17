package com.bdwise.twamp.client;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.bdwise.twamp.client.common.MessageUtil;
import com.bdwise.twamp.client.common.UnixTime;
import com.bdwise.twamp.client.event.ReceivePacketEvent;
import com.bdwise.twamp.client.event.ReceivePacketStopEvent;
import com.bdwise.twamp.client.handshake.InboundMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class ReflectHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private Map<Long, Packet> packets;
	private AtomicInteger packetCount;

	public ReflectHandler(Map<Long, Packet> packets, int packetCount) {
		this.packets = packets;
		this.packetCount = new AtomicInteger(packetCount);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
		ByteBuf msg = datagramPacket.content();
		String server = datagramPacket.sender().getHostString();
		
		ReflectorUnauthenticatedMessage reflectorUnauthenticated = new ReflectorUnauthenticatedMessage();
		reflectorUnauthenticated.readFrom(msg);

//		logger.debug("reflectorUnauthenticated.getSequenceNumber() " + reflectorUnauthenticated.getSequenceNumber());
		Packet packet = packets.remove(reflectorUnauthenticated.getSequenceNumber()).received();
		double remoteProcessingDelay = MessageUtil.getUnixTimeDiff(reflectorUnauthenticated.getReceiveTimestamp(),
				reflectorUnauthenticated.getTimestamp()) / 1_000_000.0;
//		logger.info("Inside incomming packet handler : " + packet.getDelay(remoteProcessingDelay));
		
		applicationEventPublisher.publishEvent(new ReceivePacketEvent(this, packet.getDelay(remoteProcessingDelay), server));
		
		if (packetCount.decrementAndGet() <= 0) {
			applicationEventPublisher.publishEvent(new ReceivePacketStopEvent(this));
			return;
		}

	}

	private class ReflectorUnauthenticatedMessage implements InboundMessage {
		private long sequenceNumber;
		private UnixTime timestamp = new UnixTime();
		private byte s1;
		private byte z1;
		private byte scale1;
		private byte multiplier1;
		private byte[] mbz1 = new byte[2];
		private UnixTime receiveTimestamp = new UnixTime();
		private long senderSequenceNumber;
		private UnixTime senderTimestamp = new UnixTime();
		private byte s2;
		private byte z2;
		private byte scale2;
		private byte multiplier2;
		private byte[] mbz2 = new byte[2];
		private byte senderTtl;

		@Override
		public String toString() {
			return "ReflectorUnauthenticatedMessage [sequenceNumber=" + sequenceNumber + ", timestamp=" + timestamp
					+ ", s1=" + s1 + ", z1=" + z1 + ", scale1=" + scale1 + ", multiplier1=" + multiplier1 + ", mbz1="
					+ Arrays.toString(mbz1) + ", receiveTimestamp=" + receiveTimestamp + ", senderSequenceNumber="
					+ senderSequenceNumber + ", senderTimestamp=" + senderTimestamp + ", s2=" + s2 + ", z2=" + z2
					+ ", scale2=" + scale2 + ", multiplier2=" + multiplier2 + ", mbz2=" + Arrays.toString(mbz2)
					+ ", senderTtl=" + senderTtl + "]";
		}

		public long getSequenceNumber() {
			return sequenceNumber;
		}

		public UnixTime getTimestamp() {
			return timestamp;
		}

		public byte getS1() {
			return s1;
		}

		public byte getZ1() {
			return z1;
		}

		public byte getScale1() {
			return scale1;
		}

		public byte getMultiplier1() {
			return multiplier1;
		}

		public byte[] getMbz1() {
			return mbz1;
		}

		public UnixTime getReceiveTimestamp() {
			return receiveTimestamp;
		}

		public long getSenderSequenceNumber() {
			return senderSequenceNumber;
		}

		public UnixTime getSenderTimestamp() {
			return senderTimestamp;
		}

		public byte getS2() {
			return s2;
		}

		public byte getZ2() {
			return z2;
		}

		public byte getScale2() {
			return scale2;
		}

		public byte getMultiplier2() {
			return multiplier2;
		}

		public byte[] getMbz2() {
			return mbz2;
		}

		public byte getSenderTtl() {
			return senderTtl;
		}

		@Override
		public void readFrom(ByteBuf bytebuf) {
			sequenceNumber = bytebuf.readUnsignedInt();
			timestamp.readFrom(bytebuf);
			s1 = bytebuf.readByte();
			z1 = bytebuf.readByte();
//			scale1 = bytebuf.readByte();
//			multiplier1 = bytebuf.readByte();

			bytebuf.readBytes(mbz1);

			receiveTimestamp.readFrom(bytebuf);
			senderSequenceNumber = bytebuf.readUnsignedInt();
			senderTimestamp.readFrom(bytebuf);

			s2 = bytebuf.readByte();
			z2 = bytebuf.readByte();
//			scale2 = bytebuf.readByte();
//			multiplier2 = bytebuf.readByte();

			bytebuf.readBytes(mbz2);
			senderTtl = bytebuf.readByte();

		}

		@Override
		public int getSize() {
			return 45;
		}

	}

}
