package com.bdwise.twamp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class HandshakeManager implements SmartLifecycle{
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	protected volatile boolean running = false;
	
	@Value("${twamp.server.address}")
	private String server = null;
	
	@Value("${twamp.server.port}")
	private int port = 0;
	
	
	private Channel clientChannel;
	private EventLoopGroup group;
	
    
    private HandshakeHandler handshakeHandler;
    
    public HandshakeManager(HandshakeHandler handshakeHandler) {
    	this.handshakeHandler = handshakeHandler;
    }

	@Override
	public void start() {
		if (running) return;
		group = new NioEventLoopGroup();
		Bootstrap clientBootstrap = new Bootstrap();

	    clientBootstrap.group(group);
	    clientBootstrap.channel(NioSocketChannel.class);
	    clientBootstrap.remoteAddress(new InetSocketAddress(server, port));
	    clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
	        protected void initChannel(SocketChannel socketChannel) throws Exception {
	            socketChannel.pipeline().addLast(handshakeHandler);
	        }
	    });
	    ChannelFuture channelFuture;
		try {
			channelFuture = clientBootstrap.connect().sync();
			clientChannel = channelFuture.channel();
		    running = true;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	    
	}

	@Override
	public void stop() {
		if(clientChannel != null && running){
			logger.info("Shutting down client: {} on port {}",server,port);
			clientChannel.close();
			group.shutdownGracefully(1,1,TimeUnit.SECONDS);
			running = false;
		}	
	}

	@Override
	public boolean isRunning() {
		return running;
	}
}
