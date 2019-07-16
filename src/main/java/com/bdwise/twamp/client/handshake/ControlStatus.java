package com.bdwise.twamp.client.handshake;

import com.bdwise.twamp.client.HandshakeProcessor;
import com.bdwise.twamp.client.handshake.HandshakeRequestSession.AcceptSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public enum ControlStatus {
	HandshakeConnecting {
		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			HandshakeProcessor.prepareNext(HandshakeServerGreeting, ctx);
		}
	},
	HandshakeServerGreeting {
		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			HandshakeServerGreeting handshakeServerGreeting = new HandshakeServerGreeting();
			handshakeServerGreeting.read(ctx, in);

			handshakeServerGreeting.write(ctx, in);
			HandshakeProcessor.prepareNext(HandshakeSetupResponse, ctx);
		}
	},
	HandshakeSetupResponse {

		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			HandshakeSetupResponse handshakeSetupResponse = new HandshakeSetupResponse();
			handshakeSetupResponse.read(ctx, in);

			handshakeSetupResponse.write(ctx, in);
			HandshakeProcessor.prepareNext(HandshakeRequestSession, ctx);
		}

	},
	HandshakeRequestSession{

		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			HandshakeRequestSession handshakeRequestSession = new HandshakeRequestSession();
			AcceptSession acceptSession = handshakeRequestSession.read(ctx, in);

			ctx.channel().attr(HandshakeProcessor.SERVER_UDP_PORT).set((long)acceptSession.getPort());

			
			handshakeRequestSession.write(ctx, in);
			HandshakeProcessor.prepareNext(HandshakeDone, ctx);
		}
		
	},
	HandshakeDone{

		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			HandshakeStartSession handshakeStartSession = new HandshakeStartSession();
			handshakeStartSession.read(ctx, in);
		}
	}, StopSession {

		@Override
		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
			StopSessionMessage stopSession = new StopSessionMessage();
			stopSession.setThree((byte) 3);
			stopSession.setNumberOfSessions(1);
			ByteBuf msg = Unpooled.buffer(stopSession.getSize(), stopSession.getSize());
			stopSession.writeTo(msg);
			ctx.writeAndFlush(msg);			
			
			ctx.close();
		}
		
	};
		
//	},
//	HandshakeStartSession{
//
//		@Override
//		public void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor) {
//			
//			long port = ctx.channel().attr(HandshakeProcessor.SERVER_UDP_PORT).get();
//			
//			DatagramSocket socket = HandshakeProcessor.getReflector().getSocket();
//			for (int i = 0; i < 10; i++){
//				TestUnauthenticatedMessage testUnauthenticated = new TestUnauthenticatedMessage();
//				testUnauthenticated.setSequenceNumber(i);
//				testUnauthenticated.setTimestamp(MessageUtil.getUnixTime());
//				ByteBuf msg = Unpooled.buffer(testUnauthenticated.getSize(),testUnauthenticated.getSize());
//				testUnauthenticated.writeTo(msg);
//				
//				InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
//
//				System.out.println(address + " : " + port + " : ");
//				
//				DatagramPacket packet = new DatagramPacket(msg.array(), testUnauthenticated.getSize(), address, (int)port);
//				try {
//					socket.send(packet);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			HandshakeProcessor.prepareNext(null, ctx);
//		}	
//	};

	public abstract void process(ChannelHandlerContext ctx, ByteBuf in, HandshakeProcessor HandshakeProcessor);
}
